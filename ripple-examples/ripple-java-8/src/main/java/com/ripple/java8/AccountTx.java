package com.ripple.java8;

import com.ripple.client.Client;
import com.ripple.client.responses.Response;
import com.ripple.client.transactions.AccountTxPager;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.types.known.tx.result.TransactionResult;

public class AccountTx {
    static final AccountID bitStamp =
            AccountID.fromAddress("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B");

    public static void main(String[] args) {
        new Client(new JavaWebSocketTransportImpl())
                .connect("wss://s-east.ripple.com", AccountTx::new);
    }

    public AccountTx(Client client) {
        client.accountTxPager(bitStamp)
                .maxRetriesPerPage(5)
                .onPage(this::onPage)
                .onError(this::onError)
                .forward(true)
                .minLedger(6000000)
                .maxLedger(6001000)
                .pageSize(200)
                .request();
    }

    private void onError(Response response) {
        log("Oh noes! We had an error");
        log(response.message.toString(4));
        System.exit(1);
    }

    private void onPage(AccountTxPager.Page page) {
        // There was a rippled bug at time of writing, where each page's
        // ledger span wasn't set properly. Hopefully fixed by `now` :)
        log("Found %d transactions between %d and %d",
             page.size(), page.ledgerMin(), page.ledgerMax());

        page.transactionResults().forEach(this::onTransaction);

        if (page.hasNext()) {
            log("requesting next page!");
            page.requestNext();
        } else {
            log("got all transactions!");
            System.exit(0);
        }
    }

    private void onTransaction(TransactionResult result) {
        log("Found a transaction!%n%s", result.toJSON().toString(2));
    }

    private static void log(String fmt, Object... args) {
        System.out.println(String.format(fmt + "\n", args));
    }
}
