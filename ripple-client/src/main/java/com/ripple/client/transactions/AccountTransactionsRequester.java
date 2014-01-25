package com.ripple.client.transactions;

import com.ripple.client.Client;
import com.ripple.client.enums.Command;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.core.coretypes.AccountID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AccountTransactionsRequester {
    public AccountTransactionsRequester(Client client, AccountID account, OnPage onPage) {
        this(client, account, onPage, -1, -1);
    }
    public AccountTransactionsRequester(Client client, AccountID account, OnPage onPage, long ledgerMin) {
        this(client, account, onPage, ledgerMin, -1);
    }

    public AccountTransactionsRequester(Client client, AccountID account, OnPage onPage, long ledgerMin, long ledgerMax) {
        this.ledgerMax = ledgerMax;
        this.ledgerMin = ledgerMin;
        this.account = account;
        this.client = client;
        this.onPage = onPage;
    }

    public void request() {
        walkAccountTx(null);
    }

    private long ledgerMax;
    private long ledgerMin;

    private boolean aborted = false;

    public void abort() {
        aborted = true;
    }

    public interface Page {
        boolean hasNext();
        void requestNext();
        long ledgerMax();
        long ledgerMin();
        ArrayList<TransactionResult> transactionResults();
        JSONArray transactionsJSON();
    }

    public interface OnPage {
        void onPage(Page page);
    }

    AccountID account;
    Client client;
    OnPage onPage;

    private void walkAccountTx(final Object marker) {
        Request request = client.newRequest(Command.account_tx);
        request.json("binary", true);
        request.json("account", account);

        if (marker != null) {
            request.json("marker", marker);
        }
        request.json("ledger_index_max", ledgerMax);
        request.json("ledger_index_min", ledgerMin);

        request.once(Request.OnSuccess.class, new Request.OnSuccess() {
            @Override
            public void called(Response response) {
                if (aborted) {
                    return;
                }

                final JSONObject result = response.result;
                try {
                    final JSONArray transactions = result.getJSONArray("transactions");

                    Object newMarker = result.opt("marker");
                    // Fix for ancient servers before Stef's patch
                    if (marker != null && newMarker != null && marker.toString().equals(newMarker.toString())) {
                        newMarker = null;
                    }
                    final Object finalNewMarker = newMarker;
                    onPage.onPage(new Page() {
                        ArrayList<TransactionResult> txns = null;

                        @Override
                        public boolean hasNext() {
                            return finalNewMarker != null;
                        }

                        @Override
                        public void requestNext() {
                            if (hasNext()) {
                                walkAccountTx(finalNewMarker);
                            }
                        }

                        @Override
                        public long ledgerMax() {
                            try {
                                return result.getInt("ledger_index_max");
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        @Override
                        public long ledgerMin() {
                            try {
                                return result.getInt("ledger_index_min");
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
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
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        });
        request.request();
    }
}
