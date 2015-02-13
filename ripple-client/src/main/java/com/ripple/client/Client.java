package com.ripple.client;

import com.ripple.client.enums.Command;
import com.ripple.client.enums.Message;
import com.ripple.client.enums.RPCErr;
import com.ripple.client.pubsub.Publisher;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.subscriptions.TrackedAccountRoot;
import com.ripple.client.subscriptions.ServerInfo;
import com.ripple.client.subscriptions.SubscriptionManager;
import com.ripple.client.subscriptions.TransactionSubscriptionManager;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.client.transport.TransportEventHandler;
import com.ripple.client.transport.WebSocketTransport;
import com.ripple.client.types.AccountLine;
import com.ripple.core.coretypes.*;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.sle.LedgerEntry;
import com.ripple.core.types.known.sle.entries.AccountRoot;
import com.ripple.core.types.known.sle.entries.Offer;
import com.ripple.core.types.known.tx.result.TransactionResult;
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.crypto.ecdsa.Seed;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ripple.client.requests.Request.Manager;

public class Client extends Publisher<Client.events> implements TransportEventHandler {
    public static final Logger logger = Logger.getLogger(Client.class.getName());

    private long reconnectDormantAfter = 20000; // ms

    public void setReconnectDormantAfter(long reconnectDormantAfter) {
        this.reconnectDormantAfter = reconnectDormantAfter;
    }

    private long lastConnection = -1; // -1 means null

    public static void log(Level level, String fmt, Object... args) {
        if (logger.isLoggable(level)) {
            logger.log(level, fmt, args);
        }
    }

    public void requestLedgerEntry(final Hash256 index, final Number ledger_index, final Manager<LedgerEntry> cb) {
        makeManagedRequest(Command.ledger_entry, cb, new Request.Builder<LedgerEntry>() {
            @Override
            public void beforeRequest(Request request) {
                request.json("ledger_index", ledger_index.longValue());
                request.json("index", index.toJSON());
            }
            @Override
            public LedgerEntry buildTypedResponse(Response response) {
                String node_binary = response.result.optString("node_binary");
                STObject node = STObject.translate.fromHex(node_binary);
                node.put(Hash256.index, index);
                return (LedgerEntry) node;
            }
        });
    }

    public Request requestLedger(Number ledger_index, final Manager<JSONObject> cb) {
        Request request = newRequest(Command.ledger);
        request.json("ledger_index", ledger_index.longValue());
        request.once(Request.OnResponse.class, new Request.OnResponse() {
            @Override
            public void called(Response response) {
                try {
                    if (response.succeeded) {
                        cb.cb(response, response.result.optJSONObject("ledger"));
                    } else {
                        cb.cb(response, null);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        cb.beforeRequest(request);
        request.request();
        return request;
    }

    public <T> Request makeManagedRequest(final Command cmd, final Manager<T> manager, final Request.Builder<T> builder) {
        Request request = newRequest(cmd);
        request.once(Request.OnResponse.class, new Request.OnResponse() {
            @Override
            public void called(Response response) {
                try {
                    if (response.succeeded) {
                        T t = builder.buildTypedResponse(response);
                        manager.cb(response, t);

                    } else {
                        if (manager.retryOnUnsuccessful(response)) {
                            makeManagedRequest(cmd, manager, builder);
                        } else {
                            manager.cb(response, null);
                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        builder.beforeRequest(request);
        manager.beforeRequest(request);
        request.request();
        return request;
    }

    public Request requestAccountInfo(final AccountID addy, final Manager<AccountRoot> manager) {
        return makeManagedRequest(Command.account_info, manager, new Request.Builder<AccountRoot>() {
            @Override
            public void beforeRequest(Request request) {
                request.json("account", addy);
            }

            @Override
            public AccountRoot buildTypedResponse(Response response) {
                JSONObject root = response.result.optJSONObject("account_data");
                return (AccountRoot) STObject.fromJSONObject(root);
            }
        });
    }

    public void requestLedgerData(final long ledger_index, final Manager<ArrayList<LedgerEntry>> manager) {
        makeManagedRequest(Command.ledger_data, manager, new Request.Builder<ArrayList<LedgerEntry>>() {
            @Override
            public void beforeRequest(Request request) {
                request.json("ledger_index", ledger_index);
                request.json("binary", true);
            }

            @Override
            public ArrayList<LedgerEntry> buildTypedResponse(Response response) {
                JSONArray state = response.result.getJSONArray("state");
                ArrayList<LedgerEntry> result = new ArrayList<LedgerEntry>();
                for (int i = 0; i < state.length(); i++) {
                    JSONObject stateObject = state.getJSONObject(i);
                    LedgerEntry le = (LedgerEntry) STObject.fromHex(stateObject.getString("data"));
                    le.index(Hash256.fromHex(stateObject.getString("index")));
                    result.add(le);
                }
                return result;
            }
        });
    }

    public Request requestAccountLines(final AccountID addy, final Manager<ArrayList<AccountLine>> manager) {
        return makeManagedRequest(Command.account_lines, manager, new Request.Builder<ArrayList<AccountLine>>() {
            @Override
            public void beforeRequest(Request request) {
                request.json("account", addy);
            }

            @Override
            public ArrayList<AccountLine> buildTypedResponse(Response response) {
                ArrayList<AccountLine> lines = new ArrayList<AccountLine>();
                JSONArray array = response.result.optJSONArray("lines");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject line = array.optJSONObject(i);
                    lines.add(AccountLine.fromJSON(addy, line));
                }
                return lines;
            }
        });
    }

    public void connect(String s, OnConnected onConnected) {
        connect(s);
        once(OnConnected.class, onConnected);
    }

    public static interface events<T> extends Publisher.Callback<T> {
    }

    public static interface OnLedgerClosed extends events<ServerInfo> {
    }

    public static interface OnConnected extends events<Client> {
    }

    public static interface OnDisconnected extends events<Client> {
    }

    public static interface OnSubscribed extends events<ServerInfo> {
    }

    public static interface OnMessage extends events<JSONObject> {
    }

    public static interface OnSendMessage extends events<JSONObject> {
    }

    public static interface OnStateChange extends events<Client> {
    }

    public static interface OnPathFind extends events<JSONObject> {
    }

    public static interface OnValidatedTransaction extends events<TransactionResult> {
    }

    private boolean manuallyDisconnected = false;

    public boolean isManuallyDisconnected() {
        return manuallyDisconnected;
    }

    public void disconnect() {
        manuallyDisconnected = true;
        ws.disconnect();
        emit(OnDisconnected.class, Client.this); 
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

    @SuppressWarnings("unused")
    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public void schedule(long ms, Runnable runnable) {
        service.schedule(errorHandling(runnable), ms, TimeUnit.MILLISECONDS);
    }

    public boolean connected = false;
    private HashMap<AccountID, Account> accounts = new HashMap<AccountID, Account>();
    public SubscriptionManager subscriptions = new SubscriptionManager();

    public Client(WebSocketTransport ws) {
        this.ws = ws;
        ws.setHandler(this);

        prepareExecutor();
        // requires executor, so call after prepareExecutor
        pollLastConnectionTimeAndReconnectWhenIDLE();

        subscriptions.on(SubscriptionManager.OnSubscribed.class, new SubscriptionManager.OnSubscribed() {
            @Override
            public void called(JSONObject subscription) {
                if (!connected)
                    return;
                subscribe(subscription);
            }
        });
    }

    /**
     * This will detect stalled connections When connected we are subscribed to
     * a ledger, and ledgers should be at most 20 seconds apart.
     */
    private void pollLastConnectionTimeAndReconnectWhenIDLE() {
        final long ms = reconnectDormantAfter;

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
                            emit(OnDisconnected.class, Client.this); // otherwise the "onDisconnect" callback would only be called after the first failed reconnect attempt. This added line keeps the symmetry of disconnect->reconnect 
                            connect(previousUri);
                        }
                    }
                }

                pollLastConnectionTimeAndReconnectWhenIDLE();
            }
        });
    }

    public Request requestBookOffers(Issue get, Issue pay) {
        Request request = newRequest(Command.book_offers);
        request.json("taker_gets", get.toJSON());
        request.json("taker_pays", pay.toJSON());
        return request;
    }

    public Request requestBookOffers(Number ledger_index, Issue get, Issue pay, final Manager<ArrayList<Offer>> cb) {
        Request request = newRequest(Command.book_offers);
        request.json("taker_gets", get.toJSON());
        request.json("taker_pays", pay.toJSON());

        if (ledger_index != null) {
            request.json("ledger_index", ledger_index);
        }

        request.once(Request.OnResponse.class, new Request.OnResponse() {
            @Override
            public void called(Response response) {
                try {
                    if (response.succeeded) {
                        ArrayList<Offer> offers = new ArrayList<Offer>();
                        JSONArray offersJson = response.result.getJSONArray("offers");
                        for (int i = 0; i < offersJson.length(); i++) {
                            JSONObject jsonObject = offersJson.getJSONObject(i);
                            STObject object = STObject.fromJSONObject(jsonObject);
                            offers.add((Offer) object);
                        }
                        cb.cb(response, offers);
                    } else {
                        cb.cb(response, null);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        request.request();
        return request;
    }

    public Request requestTransaction(Hash256 hash, final Manager<TransactionResult> cb) {
        return requestTransaction(hash.toHex(), cb);
    }

    public Request requestTransaction(String hash, final Manager<TransactionResult> cb) {
        final Request request = newRequest(Command.tx);
        request.json("binary", true);
        request.json("transaction", hash);
        request.once(Request.OnResponse.class, new Request.OnResponse() {
            @Override
            public void called(Response response) {
                try {
                    if (response.succeeded) {
                        TransactionResult tr = new TransactionResult(response.result, TransactionResult.Source.request_tx_binary);
                        cb.cb(response, tr);
                    } else {
                        // you can look at request.response if this is null ;)
                        cb.cb(response, null);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        request.request();
        return request;
    }

    public Request ping() {
        Request request = newRequest(Command.ping);
        return request;
    }

    public Request subscribeAccount(AccountID... accounts) {
        Request request = newRequest(Command.subscribe);
        JSONArray accounts_arr = new JSONArray();
        for (AccountID acc : accounts) {
            accounts_arr.put(acc);
        }
        request.json("accounts", accounts_arr);
        return request;
    }

    public Request subscribeBookOffers(Issue get, Issue pay) {
        Request request = newRequest(Command.subscribe);
        JSONObject book = new JSONObject();
        JSONArray books = new JSONArray(new Object[] { book });
        book.put("snapshot", true);
        book.put("taker_gets", get.toJSON());
        book.put("taker_pays", pay.toJSON());
        request.json("books", books);
        return request;
    }

    public Account account(final AccountID id, IKeyPair keyPair) {
        if (accounts.containsKey(id)) {
            return accounts.get(id);
        } else {
            TrackedAccountRoot accountRoot = accountRoot(id);
            Account account = new Account(id, keyPair, accountRoot, new TransactionManager(this, accountRoot, id, keyPair));
            accounts.put(id, account);
            subscriptions.addAccount(id);

            return account;
        }
    }

    public Account accountFromSeed(String masterSeed) {
        IKeyPair kp = Seed.fromBase58(masterSeed).keyPair();
        return account(AccountID.fromKeyPair(kp), kp);
    }

    private TrackedAccountRoot accountRoot(AccountID id) {
        TrackedAccountRoot accountRoot = new TrackedAccountRoot();
        requestAccountRoot(id, accountRoot, 0);
        return accountRoot;
    }

    private void requestAccountRoot(final AccountID id, final TrackedAccountRoot accountRoot, final int attempt) {
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

    public Request accountLines(AccountID account) {
        Request req = newRequest(Command.account_lines);
        req.json("account", account.address);
        return req;

    }

    public Request accountInfo(AccountID account) {
        Request req = newRequest(Command.account_info);
        req.json("account", account.address);
        return req;

    }

    public ServerInfo serverInfo = new ServerInfo();
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
     * After calling this method, all subsequent interaction with the api should
     * be called via posting Runnable() run blocks to the Executor.
     *
     * Essentially, all ripple-lib-java api interaction
     * should happen on the one thread.
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
    }

    // @Override
    public void onMessageInClientThread(JSONObject msg) {
        Message type = Message.valueOf(msg.optString("type", null));

        try {
            emit(OnMessage.class, msg);
            if (logger.isLoggable(Level.FINER)) {
                log(Level.FINER, "Receive `{0}`: {1}", type, prettyJSON(msg));
            }

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
            // This seems to be swallowed higher up, (at least by the
            // Java-WebSocket transport implementation)
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
        TransactionResult tr = new TransactionResult(msg, TransactionResult.Source.transaction_subscription_notification);
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
            log(Level.WARNING, "Response without a request: {0}", msg);
            return;
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
        
        emit(OnDisconnected.class, this); //this ensures that the callback method onDisconnect is called before a new connection is established this keeps the symmetry of connect-> disconnect -> reconnect 

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

        logger.entering(getClass().getName(), "doOnDisconnected");
    }

    private int reconnectDelay() {
        return 1000;
    }

    @Override
    public void onConnected() {
        run(new Runnable() {
            public void run() {
                doOnConnected();
            }
        });
    }

    private void doOnConnected() {
        resetReconnectStatus();

        logger.entering(getClass().getName(), "doOnConnected");
        connected = true;
        emit(OnConnected.class, this);

        subscribe(prepareSubscription());
        logger.exiting(getClass().getName(), "doOnConnected");
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
        subscriptions.pauseEventEmissions();
        subscriptions.addStream(SubscriptionManager.Stream.ledger);
        subscriptions.addStream(SubscriptionManager.Stream.server);
        subscriptions.unpauseEventEmissions();
        return subscriptions.allSubscribed();
    }

    public Request submit(String tx_blob, boolean fail_hard) {
        Request req = newRequest(Command.submit);
        req.json("tx_blob", tx_blob);
        req.json("fail_hard", fail_hard);
        return req;
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

    private String prettyJSON(JSONObject object) {
        try {
            return object.toString(4);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
