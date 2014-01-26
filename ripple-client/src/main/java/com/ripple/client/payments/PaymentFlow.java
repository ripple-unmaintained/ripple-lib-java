package com.ripple.client.payments;

import com.ripple.client.Client;
import com.ripple.client.enums.Command;
import com.ripple.client.pubsub.Publisher;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.transactions.ManagedTxn;
import com.ripple.core.coretypes.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class PaymentFlow extends Publisher<PaymentFlow.events> {
    public Amount destinationAmount;

    public static abstract class events<T> extends Publisher.Callback<T> {}
    abstract static public class OnDestInfo extends events<STObject>{}
    abstract static public class OnAlternatives extends events<Alternatives> {}
    abstract static public class OnAlternativesStale extends events<Alternatives> {}
    abstract static public class OnPathFind extends events<Request> {}

    Client client;

    private final Client.OnPathFind onPathFind = new Client.OnPathFind() {
        @Override
        public void called(JSONObject jsonObject) {
            try {
                int id = jsonObject.getInt("id");
                if (pathFind != null && id == pathFind.id) {
                    emit(OnAlternatives.class, new Alternatives(jsonObject.getJSONArray("alternatives"), null));
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    };
    private final Client.OnConnected onConnected = new Client.OnConnected() {
        @Override
        public void called(Client client) {
            if (pathFind != null) {
                makePathFindRequestIfCan();
            }
        }
    };

    public PaymentFlow(Client client) {
        this.client = client;

        client.on(Client.OnConnected.class, onConnected);
        client.on(Client.OnPathFind.class,  onPathFind);

        on(OnAlternatives.class, new OnAlternatives() {
            @Override
            public void called(Alternatives alts) {
                alternatives = alts;
            }
        });
    }

    public void unsubscribeFromClientEvents() {
        client.removeListener(Client.OnConnected.class, onConnected);
        client.removeListener(Client.OnPathFind.class, onPathFind);
    }

    AccountID src, dest;
    STObject srcInfo, destInfo; // AccountRoot objects
    Alternatives alternatives;

    Currency destAmountCurrency;
    BigDecimal destAmountValue;

    // We store these guys here so we can know if they have become stale
    Request pathFind;

    public Request requestAccountInfo(final AccountID id) {
        // TODO try from cache

        Request request;
        request = client.newRequest(Command.account_info);
        request.json("account", id);
        request.once(Request.OnResponse.class, new Request.OnResponse() {
            @Override
            public void called(Response response) {
                if (response.succeeded) {
                    JSONObject accountJSON = response.result.optJSONObject("account_data");
                    STObject accountData = STObject.translate.fromJSONObject(accountJSON);
                    if (PaymentFlow.this.src == id) {
                        srcInfo = accountData;
                    } else if (PaymentFlow.this.dest == id) {
                        destInfo = accountData;
                        emit(OnDestInfo.class, destInfo);
                    }
                }
            }
        });
        request.request();
        return request;
    }

    public void setSource(final AccountID id) {
        if (src == null || !src.equals(id)) {
            requestAccountInfo(id);
            src = id;
            makePathFindRequestIfCan();
        }
    }

    public void setDestination(final AccountID id) {
        if (dest == null || !dest.equals(id)) {
            requestAccountInfo(id);
            dest = id;
            makePathFindRequestIfCan();
        }
    }

    public void setDestinationAmountValue(final BigDecimal amt) {
        if (destAmountValue == null || amt == null || amt.compareTo(destAmountValue) != 0) {
            destAmountValue = amt;
            makePathFindRequestIfCan();
        }
    }
    public void makePathFindRequestIfNoneAlready() {
        if (pathFind == null) {
            makePathFindRequestIfCan();
        }
    }

    public void makePathFindRequestIfCan() {
        // TODO: ...
        cancelPendingRequest();

        if (tooLittleInfoForPathFindRequest()) {
            return;
        }

        if (destAmountCurrency.equals(Currency.XRP)) {
            // TODO: some way of ...
            destinationAmount = Issue.XRP.amount(destAmountValue);
        } else {
            destinationAmount = new Amount(destAmountValue, destAmountCurrency, dest, false);
        }

        pathFind = client.newRequest(Command.path_find);
        pathFind.json("subcommand", "create");
        pathFind.json("source_account", src);
        pathFind.json("destination_account", dest);

        // toJSON will give us what we want ;) drops string if native, else an {} if IOU
        pathFind.json("destination_amount", destinationAmount.toJSON());


        pathFind.once(Request.OnResponse.class, new Request.OnResponse() {
            @Override
            public void called(Response response) {
                if (response.succeeded && response.request == pathFind) {
                    try {
                        JSONArray alternatives = response.result.getJSONArray("alternatives");
                        emit(OnAlternatives.class, new Alternatives(alternatives, PaymentFlow.this.alternatives));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        pathFind.request();

        emit(OnPathFind.class, pathFind);
    }

    private boolean tooLittleInfoForPathFindRequest() {
        return destAmountValue == null || destAmountCurrency == null || src == null || dest == null;
    }

    private void cancelPendingRequest() {
        pathFind = null;
        if (alternatives != null) {
            emit(OnAlternativesStale.class, alternatives);
        }
        // TODO invalidate existing alternatives
    }

    public void setDestinationAmountCurrency(final Currency currency) {
        if (destAmountCurrency == null || !currency.equals(destAmountCurrency)) {
            destAmountCurrency = currency;
            makePathFindRequestIfCan();
        }
    }

    public void abort() {
        abortPathFind();
        cancelPendingRequest();
    }

    public ManagedTxn createPayment(Alternative alternative, BigDecimal sendMaxMultiplier) {
        cancelPendingRequest();

        // Cancel the path finding request.
        abortPathFind();

        ManagedTxn payment = client.account(src).transactionManager().payment();
        payment.put(AccountID.Destination, dest);

        if (alternative.paths.size() > 0) {
            payment.put(PathSet.Paths, alternative.paths);
        }
        payment.put(Amount.SendMax, alternative.sourceAmount.multiply(sendMaxMultiplier));
        payment.put(Amount.Amount, destinationAmount);

        return payment;
    }

    public void abortPathFind() {
        Request request = client.newRequest(Command.path_find);
        request.json("subcommand", "close");
        request.request();
    }
}