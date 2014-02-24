package com.ripple.client;

import com.ripple.client.enums.Command;
import com.ripple.client.enums.Message;
import com.ripple.client.enums.RPCErr;
import com.ripple.client.pubsub.Publisher;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.subscriptions.AccountRoot;
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
import com.ripple.core.types.known.tx.result.TransactionResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

public class Client extends Publisher<Client.events> implements TransportEventHandler {
    public static abstract class events<T>      extends Publisher.Callback<T> {}
    public abstract static class OnLedgerClosed extends events<ServerInfo> {}
    public abstract static class OnConnected    extends events<Client> {}
    public abstract static class OnDisconnected extends events<Client> {}
    public abstract static class OnSubscribed   extends events<ServerInfo> {}
    public abstract static class OnMessage extends events<JSONObject> {}
    public abstract static class OnSendMessage extends events<JSONObject> {}
    public abstract static class OnStateChange extends events<Client> {}
    public abstract static class OnPathFind extends events<JSONObject> {}

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
        String stackTrace = getStackTrace(e);
        ClientLogger.log(stackTrace);
        // TODO: exit on exceptions ?
        System.out.println(stackTrace);
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
    SubscriptionManager subscriptions = new SubscriptionManager();

    public Client(WebSocketTransport ws) {
        prepareExecutor();

        this.ws = ws;
        ws.setHandler(this);

        on(OnLedgerClosed.class, new OnLedgerClosed() {
            @Override
            public void called(ServerInfo serverInfo) {
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
        request.json("taker_pays", pay.toJSON());
        request.json("taker_gets", get.toJSON());
        return request;
    }

    public Account account(final AccountID id) {
        if (accounts.containsKey(id)) {
            return accounts.get(id);
        }
        else {
            AccountRoot accountRoot = accountRoot(id);
            Account account = new Account(
                    id,
                    accountRoot,
                    new Wallet(),
                    new TransactionManager(this, accountRoot, id, id.getKeyPair())
            );
            accounts.put(id, account);
            subscriptions.addAccount(id);

            return account;
        }
    }
    public Account accountFromSeed(String masterSeed) {
        return account(AccountID.fromSeedString(masterSeed));
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
                        ClientLogger.log("Unfunded account: %s", response.message);
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
    public TreeMap<Integer, Request> requests = new TreeMap<Integer, Request>();

    WebSocketTransport ws;
    private int cmdIDs;


    String previousUri;
    // TODO: reconnect if we go 60s without any message from the server

    public void doConnect(String uri) {
        ClientLogger.log("Connecting to " + uri);
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
        run(new Runnable() {
            @Override
            public void run() {
                onMessageInClientThread(msg);
            }
        });
    }


//    @Override
    public void onMessageInClientThread(JSONObject msg) {
        try {
            emit(OnMessage.class, msg);
            ClientLogger.log("Receive: %s", prettyJSON(msg));

            switch (Message.valueOf(msg.optString("type", null))) {
                case serverStatus:
                    updateServerInfo(msg);
                    break;
                case ledgerClosed:
                    updateServerInfo(msg);
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
            e.printStackTrace();
            // This seems to be swallowed higher up, (at least by the Java-WebSocket transport implementation)
            throw new RuntimeException(e); // TODO
        } finally {
            emit(OnStateChange.class, this);
        }
    }

    void onTransaction(JSONObject msg) {
        TransactionResult tr = new TransactionResult(msg, TransactionResult
                                                            .Source
                                                            .transaction_subscription_notification);

        if (tr.validated) {
            ClientLogger.log("Transaction %s is validated", tr.hash);
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
                ClientLogger.log("Found initiator %s, notifying transactionManager", initator);
                initator.transactionManager().notifyTransactionResult(tr);
            } else {
                ClientLogger.log("Can't find initiating account!");
            }

        }
    }

    void unhandledMessage(JSONObject msg) {
        throw new RuntimeException("Unhandled message: " + msg);
    }

    void onResponse(JSONObject msg) {
        Request request = requests.get(msg.optInt("id", -1));
        if (request == null) {
            // TODO: should warn?
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
        connected = false;
        ClientLogger.log("onDisconnected");
        emit(OnDisconnected.class, this);
        // TODO: scheduled reconnect ;)
        // Client abstract (Runable run, int ms) badboy
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        connect(previousUri);
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
        connected = true;
        ClientLogger.log("onConnected");
        emit(OnConnected.class, this);
        subscribe(prepareSubscription());
        subscriptions.on(SubscriptionManager.OnSubscribed.class, new SubscriptionManager.OnSubscribed() {
            @Override
            public void called(JSONObject subscription) {
                if (!connected) return;
                subscribe(subscription);
            }
        });
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
        ClientLogger.log("Send: %s", prettyJSON(object));
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
