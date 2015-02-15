package com.ripple.client.transactions;

import com.ripple.client.Client;
import com.ripple.client.enums.Command;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.types.known.tx.result.TransactionResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AccountTxPager {
    Client client;
    AccountID account;

    private Request.OnError onError;
    OnPage onPage;

    private long ledgerMax;
    private long ledgerMin;
    private boolean aborted = false;
    private boolean forward = false;
    private int limit = 2000;
    private int maxRetries;

    public interface Page {
        boolean hasNext();
        void requestNext();
        long ledgerMax();
        long ledgerMin();
        int size();
        ArrayList<TransactionResult> transactionResults();
        JSONArray transactionsJSON();
    }

    public interface OnPage {
        void onPage(Page page);
    }

    public AccountTxPager(Client client, AccountID account, OnPage onPage) {
        this(client, account, onPage, -1, -1);
    }
    public AccountTxPager(Client client, AccountID account, OnPage onPage, long ledgerMin) {
        this(client, account, onPage, ledgerMin, -1);
    }

    public AccountTxPager(Client client, AccountID account, OnPage onPage, long ledgerMin, long ledgerMax) {
        this.ledgerMax = ledgerMax;
        this.ledgerMin = ledgerMin;
        this.account = account;
        this.client = client;
        //
        if (onPage == null) {
            onPage = new OnPage() {
                @Override
                public void onPage(Page page) {

                }
            };
        }
        this.onPage = onPage;
        this.onError = null;
        maxRetries = 10;
    }

    public void request() {
        if (onPage == null) {
            throw new IllegalStateException("Forgot to set OnPage!");
        }
        walkAccountTx(null);
    }

    public AccountTxPager maxRetriesPerPage(int retries) {
        maxRetries = retries;
        return this;
    }

    public AccountTxPager onPage(OnPage page) {
        onPage = page;
        return this;
    }
    public AccountTxPager onError(Request.OnError onError) {
        this.onError = onError;
        return this;
    }

    public void abort() {
        aborted = true;
    }

    public AccountTxPager pageSize(int i)
    {
        limit = i;
        return this;
    }

    public AccountTxPager minLedger(long i) {
        ledgerMin = i;
        return this;
    }

    public AccountTxPager maxLedger(long i) {
        ledgerMax = i;
        return this;
    }

    public AccountTxPager forward(boolean fwd)
    {
        forward = fwd;
        return this;
    }

    private void walkAccountTx(final Object marker) {
        client.makeManagedRequest(Command.account_tx, new Request.Manager<JSONArray>() {
            int retries = 0;

            @Override
            public boolean retryOnUnsuccessful(Response r) {
                return maxRetries == -1 || (++retries) <= maxRetries;
            }

            @Override
            public void cb(Response response, JSONArray ignored) throws JSONException {
                if (aborted) {
                    return;
                }
                if (response.succeeded) {
                    onTransactions(response.result);
                } else if (onError != null) {
                    onError.called(response);
                }
            }
        }, new Request.Builder<JSONArray>() {

            @Override
            public void beforeRequest(Request request) {
                configureRequest(request, marker);
            }

            @Override
            public JSONArray buildTypedResponse(Response response) {
                return null;
            }
        });
    }

    private void onTransactions(JSONObject responseResult) {
        final JSONArray transactions = responseResult.getJSONArray("transactions");
        final int ledger_index_max = responseResult.optInt("ledger_index_max");
        final int ledger_index_min = responseResult.optInt("ledger_index_min");
        final Object newMarker = responseResult.opt("marker");

        onPage.onPage(new Page() {
            ArrayList<TransactionResult> txns = null;

            @Override
            public boolean hasNext() {
                return newMarker != null;
            }

            @Override
            public void requestNext() {
                if (hasNext()) {
                    walkAccountTx(newMarker);
                }
            }

            @Override
            public long ledgerMax() {
                return ledger_index_max;
            }

            @Override
            public long ledgerMin() {
                return ledger_index_min;
            }

            @Override
            public int size() {
                return transactions.length();
            }

            @Override
            public ArrayList<TransactionResult> transactionResults() {
                if (txns == null) {
                    txns = new ArrayList<TransactionResult>();
                    for (int i = 0; i < transactions.length(); i++) {
                        JSONObject jsonObject = transactions.optJSONObject(i);
                        txns.add(new TransactionResult(jsonObject,
                                TransactionResult.Source.request_account_tx_binary));
                    }
                }
                return txns;
            }

            @Override
            public JSONArray transactionsJSON() {
                return transactions;
            }
        });
    }

    private void configureRequest(Request request, Object marker) {
        request.json("binary", true);
        request.json("account", account);

        if (marker != null) {
            request.json("marker", marker);
        }
        request.json("ledger_index_max", ledgerMax);
        request.json("ledger_index_min", ledgerMin);
        request.json("limit", limit);
        if (forward) {
            request.json("forward", true);
        }
    }
}
