package com.ripple.client.payments;

import com.ripple.client.Client;
import com.ripple.client.enums.Command;
import com.ripple.client.pubsub.Publisher;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.transactions.ManagedTxn;
import com.ripple.core.types.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class PaymentFlow extends Publisher<PaymentFlow.events> {
    public Amount destinationAmount;

    public static abstract class events<T> extends Publisher.Callback<T> {}
    abstract static public class OnDestInfo extends events<STObject>{}
    abstract static public class OnAlternatives extends events<Alternatives> {}

    Client client;

    public PaymentFlow(Client client) {
        this.client = client;

        client.on(Client.OnPathFind.class, new Client.OnPathFind() {
            @Override
            public void called(JSONObject jsonObject) {
                try {
                    int id = jsonObject.getInt("id");
                    if (pathFindRequest != null && id == pathFindRequest.id) {
                        emit(OnAlternatives.class, new Alternatives(jsonObject.getJSONArray("alternatives")));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        on(OnAlternatives.class, new OnAlternatives() {
            @Override
            public void called(Alternatives alts) {
                alternatives = alts;
            }
        });
    }

    AccountID src, dest;
    STObject srcInfo, destInfo;
    Alternatives alternatives = null;

    Currency destAmountCurrency;
    BigDecimal destAmountValue;

    // We store these guys here so we can
    Request pathFindRequest;

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
        requestAccountInfo(id);
        if (src == null || !src.equals(id)) {
            src = id;
            cancelPendingRequest();
        }
    }

    public void setDestination(final AccountID id) {
        requestAccountInfo(id);
        if (dest == null || !dest.equals(id)) {
            dest = id;
            cancelPendingRequest();
        }
    }

    public void setDestinationAmountValue(final BigDecimal amt) {
        destAmountValue = amt;
        makePathFindRequestIfCan();
    }

    private void makePathFindRequestIfCan() {
        if (destAmountValue == null || destAmountCurrency == null) {
            return;
        }  else {
            cancelPendingRequest();
        }

        if (destAmountCurrency.equals(Currency.XRP)) {
            destinationAmount = Issue.XRP.amount(destAmountValue);
        } else {
            destinationAmount = new Amount(destAmountValue, destAmountCurrency, dest, false);
        }

        pathFindRequest = client.newRequest(Command.path_find);
        pathFindRequest.json("subcommand", "create");
        pathFindRequest.json("source_account", src);
        pathFindRequest.json("destination_account", dest);
        pathFindRequest.json("destination_amount", Amount.translate.toJSONObject(destinationAmount));
        pathFindRequest.request();

        pathFindRequest.once(Request.OnResponse.class, new Request.OnResponse() {
            @Override
            public void called(Response response) {
                if (response.succeeded && response.request == pathFindRequest) {
                    try {
                        JSONArray alternatives = response.result.getJSONArray("alternatives");
                        emit(OnAlternatives.class, new Alternatives(alternatives));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void cancelPendingRequest() {
        pathFindRequest = null;
    }

    public void setDestinationAmountCurrency(final Currency currency) {
        destAmountCurrency = currency;
        makePathFindRequestIfCan();
    }

    public void abort() {
        abortPathFind();
    }

    public ManagedTxn createPayment(Alternative alternative, BigDecimal sendMaxMultiplier) {
        cancelPendingRequest();

        // Cancel the path finding request.
        abortPathFind();

        ManagedTxn payment = client.account(src).transactionManager().payment();
        payment.put(AccountID.Destination, dest);

        payment.put(PathSet.Paths, alternative.paths);
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
