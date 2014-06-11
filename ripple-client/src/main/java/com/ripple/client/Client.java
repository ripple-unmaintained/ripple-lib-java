package com.ripple.client;

import com.ripple.client.enums.Command;
import com.ripple.client.enums.Message;
import com.ripple.client.enums.RPCErr;
import com.ripple.client.pubsub.Publisher;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.subscriptions.AccountRoot;
import com.ripple.client.subscriptions.TransactionSubscriptionManager;
import com.ripple.client.subscriptions.ServerInfo;
import com.ripple.client.subscriptions.SubscriptionManager;
import com.ripple.core.types.known.tx.result.TransactionResult;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.client.transport.TransportEventHandler;
import com.ripple.client.transport.WebSocketTransport;
import com.ripple.client.wallet.Wallet;
import com.ripple.core.coretypes.*;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.crypto.ecdsa.Seed;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client extends Publisher<Client.events> implements TransportEventHandler {
    public static final Logger logger = Logger.getLogger(Client.class.getName());

    private int reconnectDormantAfter = 20000; // ms
    public void setReconnectDormantAfter(int reconnectDormantAfter) {
        this.reconnectDormantAfter = reconnectDormantAfter;
    }

    private long lastConnection = -1; // -1 means null

    public static void log(Level level, String fmt, Object... args) {
        if (logger.isLoggable(level)) {
            logger.log(level, fmt, args);
        }
    }

    public static abstract class events<T>      extends Publisher.Callback<T> {}
    public abstract static class OnLedgerClosed extends events<ServerInfo> {}
    public abstract static class OnConnected    extends events<Client> {}
    public abstract static class OnDisconnected extends events<Client> {}
    public abstract static class OnSubscribed   extends events<ServerInfo> {}
    public abstract static class OnMessage extends events<JSONObject> {}
    public abstract static class OnSendMessage extends events<JSONObject> {}
    public abstract static class OnStateChange extends events<Client> {}
    public abstract static class OnPathFind extends events<JSONObject> {}
    public abstract static class OnValidatedTransaction extends events<TransactionResult> {}

    private boolean manuallyDisconnected = false;

    public boolean isManuallyDisconnected() {
        return manuallyDisconnected;
    }

    public void disconnect() {
        manuallyDisconnected = true;
        ws.disconnect();
    }

    public void dispose() {
        ws = null;
    }

    protected ScheduledExecutorService service;
    public Thread clientThread;

    public static abstract class ThrowingRunnable implements Runnable {
        public abstract void throwingRun() throws Exception;
        @Override
        public void run() {
            try {
                throwingRun();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    public boolean runningOnClientThread() {
        return clientThread != null && Thread.currentThread().getId() == clientThread.getId();
    }

    protected void prepareExecutor() {
        service = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                clientThread = new Thread(r);
                return clientThread;
            }
        });
    }
    public void run(Runnable runnable) {
        // What if we are already in the client thread?? What happens then ?
        if (runningOnClientThread()) {
            runnable.run();
        } else {
            service.submit(errorHandling(runnable));
        }
    }

    private Runnable errorHandling(final Runnable runnable) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    onException(e);
                }
            }
        };
    }

    protected void onException(Exception e) {
        log(Level.WARNING, e.getLocalizedMessage(), e);
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public void schedule(int ms, Runnable runnable) {
        service.schedule(errorHandling(runnable), ms, TimeUnit.MILLISECONDS);
    }

    public boolean connected = false;
    private HashMap<AccountID, Account> accounts = new HashMap<AccountID, Account>();
    public SubscriptionManager subscriptions = new SubscriptionManager();

    public Client(WebSocketTransport ws) {
//        once(OnConnected.class, new OnConnected() {
//            @Override
//            public void called(Client client) {
//                ;
//            }
//        });


        this.ws = ws;
        ws.setHandler(this);


        prepareExecutor();
        // requires executor, so call after prepareExecutor
        pollLastConnectionTimeAndReconnectWhenIDLE();

        on(OnLedgerClosed.class, new OnLedgerClosed() {
            @Override
            public void called(ServerInfo serverInfo) {
                log(Level.INFO, "Requests: {0}", requests.size());
                Iterator<LedgerClosedCallback> iterator = ledgerClosedCallbacks.iterator();

                while (iterator.hasNext()) {
                    LedgerClosedCallback next = iterator.next();
                    if (serverInfo.ledger_index >= next.anyLedgerGreaterOrEqual) {
                        iterator.remove();
                        next.callback.run();
                    }
                }
            }
        });

        subscriptions.on(SubscriptionManager.OnSubscribed.class, new SubscriptionManager.OnSubscribed() {
            @Override
            public void called(JSONObject subscription) {
                if (!connected) return;
                subscribe(subscription);
            }
        });
    }

    /**
     * This will detect stalled connections
     * When connected we are subscribed to a ledger, and ledgers should be at most
     * 20 seconds apart.
     */
    private void pollLastConnectionTimeAndReconnectWhenIDLE() {
        final int ms = reconnectDormantAfter;

        schedule(ms, new Runnable() {
            @Override
            public void run() {
                int defaultValue = -1;

                if (!manuallyDisconnected) {
                    if (connected && lastConnection != defaultValue) {
                        long time = new Date().getTime();
                        long msSince = time - lastConnection;
                        if (msSince > ms) {
                            // we don't call disconnect, cause that will set the
                            lastConnection = defaultValue;
                            ws.disconnect();
                            connect(previousUri);
                        }
                    }
                }

                pollLastConnectionTimeAndReconnectWhenIDLE();
            }
        });
    }

    ArrayList<LedgerClosedCallback> ledgerClosedCallbacks = new ArrayList<LedgerClosedCallback>();
    public static class LedgerClosedCallback {
        public long anyLedgerGreaterOrEqual;

        public Runnable callback;
        public LedgerClosedCallback(long anyLedgerGreaterOrEqual, Runnable callback) {
            this.anyLedgerGreaterOrEqual = anyLedgerGreaterOrEqual;
            this.callback = callback;
        }

    }

    public void onceOnFirstLedgerClosedGreaterThan(long ledgerIndex, Runnable runnable) {
        ledgerClosedCallbacks.add(new LedgerClosedCallback(ledgerIndex, runnable));
    }


    public Request requestBookOffers(Issue get, Issue pay) {
        Request request = newRequest(Command.book_offers);
        request.json("taker_gets", get.toJSON());
        request.json("taker_pays", pay.toJSON());
        return request;
    }


    public Request subscribeBookOffers(Issue get, Issue pay) {
        Request request = newRequest(Command.subscribe);
        JSONObject book = new JSONObject();
        JSONArray books = new JSONArray(Arrays.asList(book));
        try {
            book.put("snapshot", true);
            book.put("taker_gets", get.toJSON());
            book.put("taker_pays", pay.toJSON());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        request.json("books", books);
        return request;
    }

    public Account account(final AccountID id, IKeyPair keyPair) {
        if (accounts.containsKey(id)) {
            return accounts.get(id);
        }
        else {
            AccountRoot accountRoot = accountRoot(id);
            Account account = new Account(
                    id,
                    keyPair,
                    accountRoot,
                    new Wallet(),
                    new TransactionManager(this, accountRoot, id, keyPair)
            );
            accounts.put(id, account);
            subscriptions.addAccount(id);

            return account;
        }
    }
    public Account accountFromSeed(String masterSeed) {
        IKeyPair kp = Seed.getKeyPair(masterSeed);
        return account(AccountID.fromKeyPair(kp), kp);
    }

    private AccountRoot accountRoot(AccountID id) {
        AccountRoot accountRoot = new AccountRoot();
        requestAccountRoot(id, accountRoot, 0);
        return accountRoot;
    }

    private void requestAccountRoot(final AccountID id, final AccountRoot accountRoot, final int attempt) {
        Request req = newRequest(Command.ledger_entry);
        req.json("account_root", id);

        req.once(Request.OnResponse.class, new Request.OnResponse() {
            @Override
            public void called(Response response) {

                try {
                    if (response.succeeded) {
                        accountRoot.setFromJSON(response.result.getJSONObject("node"));
                    } else if (response.rpcerr == RPCErr.entryNotFound) {
                        log(Level.INFO, "Unfunded account: {0}", response.message);
                        accountRoot.setUnfundedAccount(id);
                    } else {
                        if (attempt < 5) {
                            requestAccountRoot(id, accountRoot, attempt + 1);
                        } else {
                            // TODO //
                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        req.request();
    }

    public ServerInfo serverInfo = new ServerInfo();
    // TODO: clean up timedout requests
    public TreeMap<Integer, Request> requests = new TreeMap<Integer, Request>();

    WebSocketTransport ws;
    private int cmdIDs;


    String previousUri;

    public void doConnect(String uri) {
        log(Level.INFO, "Connecting to " + uri);
        // XXX: connect to other uris ... just parameterise connect here ??
        previousUri = uri;
        ws.connect(URI.create(uri));
    }

    /**
     * After calling this method, all subsequent interaction with the api
     * should be called via posting Runnable() run blocks to the Executor
     * Essentially, all ripple-lib-java api interaction should happen on
     * the one thread.
     *
     * @see #onMessage(org.json.JSONObject)
     */
    public void connect(final String uri) {
        manuallyDisconnected = false;

        run(new Runnable() {
            @Override
            public void run() {
                doConnect(uri);
            }
        });
    }
    /**
     * This is to ensure we run everything on the one HandlerThread
     */
    @Override
    public void onMessage(final JSONObject msg) {
        resetReconnectStatus();
        run(new Runnable() {
            @Override
            public void run() {
                onMessageInClientThread(msg);
            }
        });
    }

    private void resetReconnectStatus() {
        lastConnection = new Date().getTime();
//        reconnectIndex = 0;
    }


    //    @Override
    public void onMessageInClientThread(JSONObject msg) {
        Message type = Message.valueOf(msg.optString("type", null));

        try {
            emit(OnMessage.class, msg);
            log (Level.FINE, "Receive `{0}`: {1}", type, msg);

            switch (type) {
                case serverStatus:
                    updateServerInfo(msg);
                    break;
                case ledgerClosed:
                    updateServerInfo(msg);
                    // TODO
                    emit(OnLedgerClosed.class, serverInfo);
                    break;
                case response:
                    onResponse(msg);
                    break;
                case transaction:
                    onTransaction(msg);
                    break;
                case path_find:
                    emit(OnPathFind.class, msg);
                    break;
                default:
                    unhandledMessage(msg);
                    break;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            // This seems to be swallowed higher up, (at least by the Java-WebSocket transport implementation)
            throw new RuntimeException(e);
        } finally {
            emit(OnStateChange.class, this);
        }
    }

    public void setTransactionSubscriptionManager(TransactionSubscriptionManager transactionSubscriptionManager) {
        this.transactionSubscriptionManager = transactionSubscriptionManager;
    }

    TransactionSubscriptionManager transactionSubscriptionManager;

    void onTransaction(JSONObject msg) {
        TransactionResult tr = new TransactionResult(msg, TransactionResult
                                                            .Source
                                                            .transaction_subscription_notification);
        if (tr.validated) {
            if (transactionSubscriptionManager != null) {
                transactionSubscriptionManager.notifyTransactionResult(tr);
            } else {
                onTransactionResult(tr);
            }
        }
    }

    public void onTransactionResult(TransactionResult tr) {
        log(Level.INFO, "Transaction {0} is validated", tr.hash);
        Map<AccountID, STObject> affected = tr.modifiedRoots();

        if (affected != null) {
            Hash256 transactionHash = tr.hash;
            UInt32 transactionLedgerIndex = tr.ledgerIndex;

            for (Map.Entry<AccountID, STObject> entry : affected.entrySet()) {
                Account account = accounts.get(entry.getKey());
                if (account != null) {
                    STObject rootUpdates = entry.getValue();
                    account.getAccountRoot()
                           .updateFromTransaction(
                                   transactionHash, transactionLedgerIndex, rootUpdates);
                }
            }
        }

        Account initator = accounts.get(tr.initiatingAccount());
        if (initator != null) {
            log(Level.INFO, "Found initiator {0}, notifying transactionManager", initator);
            initator.transactionManager().notifyTransactionResult(tr);
        } else {
            log(Level.INFO, "Can't find initiating account!");
        }
        emit(OnValidatedTransaction.class, tr);
    }

    void unhandledMessage(JSONObject msg) {
        log(Level.WARNING, "Unhandled message: " + msg);
    }

    void onResponse(JSONObject msg) {
        Request request = requests.remove(msg.optInt("id", -1));

        if (request == null) {
            log(Level.WARNING, "Response without a request: {0}",  msg);
            return;
        }

        switch (request.cmd) {
            case subscribe:
                break;

            case account_info:
            case account_lines:
            case account_offers:
            case account_tx:
            case book_offers:
            case connect:
            case data_delete:
            case data_fetch:
            case data_sign:
            case data_store:
            case data_verify:
            case json:
            case ledger:
            case ledger_accept:
            case ledger_closed:
            case ledger_current:
            case ledger_entry:
            case log_level:
            case logrotate:
            case path_find:
            case peers:
            case ping:
            case proof_create:
            case proof_solve:
            case proof_verify:
            case random:
            case ripple_path_find:
            case server_info:
            case server_state:
            case sign:
            case sms:
            case stop:
            case submit:
            case transaction_entry:
            case tx:
            case tx_history:
            case unl_add:
            case unl_delete:
            case unl_list:
            case unl_load:
            case unl_network:
            case unl_reset:
            case unsubscribe:
            case validation_create:
            case validation_seed:
            case wallet_accounts:
            case wallet_propose:
            case wallet_seed:
                break;
        }
        request.handleResponse(msg);
    }

    private void updateServerInfo(JSONObject msg) {
        serverInfo.update(msg);
    }

    @Override
    public void onConnecting(int attempt) {
    }

    @Override
    public void onError(Exception error) {
    }

    @Override
    public void onDisconnected(boolean willReconnect) {
        run(new Runnable() {
            @Override
            public void run() {
                doOnDisconnected();
            }
        });
    }

    private void doOnDisconnected() {
        logger.entering(getClass().getName(), "doOnDisconnected");
        connected = false;

        if (!manuallyDisconnected) {
            // Reconnect in 50ms
            schedule(reconnectDelay(), new Runnable() {
                @Override
                public void run() {
                    connect(previousUri);
                }
            });
        } else {
            logger.fine("Currently disconnecting, so will not reconnect");
        }

        emit(OnDisconnected.class, this);
        logger.entering(getClass().getName(), "doOnDisconnected");
    }


    private int reconnectDelay() {
            return 1000;
    }

    @Override
    public void
    onConnected() {
        run(new Runnable() {
            public void run() {
                doOnConnected();
            }
        });
    }

    private void doOnConnected() {
        resetReconnectStatus();

//        logger.entering(getClass().getName(), "doOnConnected");
        connected = true;
        emit(OnConnected.class, this);
        subscribe(prepareSubscription());
//        logger.exiting(getClass().getName(), "doOnConnected");
    }

    private void subscribe(JSONObject subscription) {
        Request request = newRequest(Command.subscribe);

        request.json(subscription);
        request.on(Request.OnSuccess.class, new Request.OnSuccess() {
            @Override
            public void called(Response response) {
                // TODO ... make sure this isn't just an account subscription
                serverInfo.update(response.result);
                emit(OnSubscribed.class, serverInfo);
            }
        });
        request.request();
    }

    private JSONObject prepareSubscription() {
        subscriptions.addStream(SubscriptionManager.Stream.ledger);
        subscriptions.addStream(SubscriptionManager.Stream.server);
        return subscriptions.allSubscribed();
    }

    public Request newRequest(Command cmd) {
        return new Request(cmd, cmdIDs++, this);
    }

    public static JSONObject parseJSON(String s) {
        try {
            return new JSONObject(s);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(JSONObject object) {
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "Send: {0}", prettyJSON(object));
        }
        emit(OnSendMessage.class, object);
        ws.sendMessage(object);
    }

    private String prettyJSON(JSONObject object)  {
        try {
            return object.toString(4);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
