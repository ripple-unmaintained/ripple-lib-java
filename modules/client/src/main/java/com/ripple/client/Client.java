package com.ripple.client;

import com.ripple.client.enums.Command;
import com.ripple.client.enums.Message;
import com.ripple.client.pubsub.Publisher;
import com.ripple.client.subscriptions.AccountRoot;
import com.ripple.client.subscriptions.ServerInfo;
import com.ripple.client.subscriptions.SubscriptionManager;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.client.transactions.TransactionMessage.TransactionResult;
import com.ripple.client.transport.TransportEventHandler;
import com.ripple.client.transport.WebSocketTransport;
import com.ripple.client.wallet.Wallet;
import com.ripple.core.types.AccountID;
import com.ripple.core.types.STObject;
import com.ripple.core.types.hash.Hash256;
import com.ripple.core.types.uint.UInt32;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Client extends Publisher<Client.events> implements TransportEventHandler {
    public boolean connected = false;

    public static abstract class events<T>      extends Publisher.Callback<T> {}

    public abstract static class OnLedgerClosed extends events<ServerInfo> {}
    public abstract static class OnConnected    extends events<Client> {}
    public abstract static class OnDisconnected extends events<Client> {}
    public abstract static class OnSubscribed   extends events<ServerInfo> {}
    public abstract static class OnMessage extends events<JSONObject> {}

    private HashMap<AccountID, Account> accounts = new HashMap<AccountID, Account>();
    SubscriptionManager subscriptions = new SubscriptionManager();

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

    private AccountRoot accountRoot(final AccountID id) {
        final AccountRoot accountRoot = new AccountRoot();
        Request req = newRequest(Command.ledger_entry);
        req.json("account_root", id);

        req.on(Request.OnResponse.class, new Request.OnResponse() {
            @Override
            public void called(Response response) {

                try {
                    if (response.succeeded) {
                        accountRoot.setFromJSON(response.result.getJSONObject("node"));
                    } else {
                        accountRoot.setUnfundedAccount(id);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        req.request();
        return accountRoot;
    }

    public ServerInfo serverInfo = new ServerInfo();
    public TreeMap<Integer, Request> requests = new TreeMap<Integer, Request>();

    WebSocketTransport ws;
    private int cmdIDs;

    public Client(WebSocketTransport ws) {
        this.ws = ws;
        ws.setHandler(this);
    }

    public void connect(String uri) {
        // XXX: connect to other uris ... just parameterise connect here ??
        ws.connect(URI.create(uri));
    }

    @Override
    public void onMessage(JSONObject msg) {
        try {
            emit(OnMessage.class, msg);
            ClientLogger.log("Receive: %s", prettyJSON(msg));

            switch (Message.valueOf(msg.optString("type", null))) {
                case serverStatus:
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
                default:
                    unhandledMessage(msg);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // This seems to be swallowed higher up, (at least by the Java-WebSocket transport implementation)
            throw new RuntimeException(e);
        }
    }

    void onTransaction(JSONObject msg) {
        TransactionResult tm = new TransactionResult(msg, TransactionResult.Source
                                                                       .transaction_subscription_notification);

        if (tm.validated) {
            ClientLogger.log("Transaction %s is validated", tm.hash);
            Map<AccountID, STObject> affected = tm.modifiedRoots();

            if (affected != null) {
                Hash256 transactionHash = tm.hash;
                UInt32 transactionLedgerIndex = tm.ledgerIndex;

                for (Map.Entry<AccountID, STObject> entry : affected.entrySet()) {
                    Account account = accounts.get(entry.getKey());
                    if (account != null) {
                        STObject rootUpdates = entry.getValue();
                        account.root.updateFromTransaction(transactionHash, transactionLedgerIndex, rootUpdates);
                    }
                }
            }

            Account initator = accounts.get(tm.initiatingAccount());
            if (initator != null) {
                ClientLogger.log("Found initiator %s, notifying transactionManager", initator);
                initator.transactionManager().onTransactionResultMessage(tm);
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
        connected = false;
        ClientLogger.log("onDisconnected");
        emit(OnDisconnected.class, this);
    }

    @Override
    public void
    onConnected() {
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
