package com.ripple.java8.example;

import com.ripple.client.Client;
import com.ripple.client.responses.Response;
import com.ripple.client.transactions.AccountTxPager;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.types.known.tx.result.TransactionResult;

import static com.ripple.java8.utils.Print.print;
import static com.ripple.java8.utils.Print.printErr;

/**
 * This example shows how to page through some old transactions
 * affecting the BitStamp account.
 */
public class AccountTx {
    static final AccountID bitStamp =
            AccountID.fromAddress("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B");

    public static void main(String[] args) {
        new Client(new JavaWebSocketTransportImpl())
                .connect("wss://s-east.ripple.com",
                        AccountTx::new);
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
        printErr("Oh noes! We had an error");
        // MessageFormat gets confused by the json `{`
        printErr("{0}", response.message.toString(2));
        System.exit(1);
    }

    private void onPage(AccountTxPager.Page page) {
        // There was a rippled bug at time of writing, where each page's
        // ledger span wasn't set properly. Hopefully fixed by `now` :)
        print("Found {0} transactions between {1} and {2}",
                page.size(), page.ledgerMin(), page.ledgerMax());

        page.transactionResults().forEach(this::onTransaction);

        if (page.hasNext()) {
            print("requesting next page!");
            page.requestNext();
        } else {
            print("got all transactions!");
            System.exit(0);
        }
    }

    private void onTransaction(TransactionResult result) {
        print("Found a transaction!\n{0}", result.toJSON().toString(2));
    }
}
