package com.ripple.cli;


import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.enums.EngineResult;
import com.ripple.core.enums.TransactionType;
import com.ripple.core.types.known.tx.result.TransactionResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

import static com.ripple.cli.log.Log.log;

public class AnalyzeDump {
    public static void main(String[] args) throws IOException, JSONException {
        analyzeDump();
    }

    public static class MinMaxTracker {
        Integer min = null, max = null;
        public void record(Integer n) {
            if (min == null || n < min) min = n;
            if (max == null || n > max) max = n;
        }
    }

    public static class Counter extends TreeMap<BigDecimal, Integer> {
        public void count(BigDecimal value) {
            Integer existing = get(value);
            if (existing == null) existing = 1;
            put(value, existing  + 1);
        }
    }

    private static void analyzeDump() throws IOException, JSONException {
        BufferedReader bufferedReader = openDumpReader();
        AccountID giveAwayAccount = AccountID.fromAddress("rMTzGg7nPPEMJthjgEBfiPZGoAM7MEVa1r");

        int successful = 0, accountsFunded = 0, total = 0;
        String line;

        Counter payments = new Counter();
        Counter createdStats = new Counter();
        MinMaxTracker minMaxTracker = new MinMaxTracker();

        while ((line = bufferedReader.readLine()) != null && line.length() > 0) {
            JSONObject json = new JSONObject(line);
            JSONArray transactions = json.getJSONArray("transactions");
            minMaxTracker.record(json.getInt("ledger_index_max"));
            minMaxTracker.record(json.getJSONObject("marker").getInt("ledger"));

            for (int i = 0; i < transactions.length(); i++) {
                total ++;
                JSONObject tx = transactions.getJSONObject(i);
                TransactionResult tr;
                tr = new TransactionResult(tx, TransactionResult.Source.request_account_tx_binary);

                if (tr.engineResult      == EngineResult.tesSUCCESS &&
                    tr.transactionType() == TransactionType.Payment            &&
                    tr.initiatingAccount().equals(giveAwayAccount)) {
                    Amount amount = tr.txn.get(Amount.Amount);

                    if (amount.isNative()) {
                        successful++;
                        BigDecimal value = amount.value();
                        payments.count(value);
                        if (tr.createdAccount() != null) {
                            createdStats.count(value);
                            accountsFunded++;
                        }
                    }
                }
            }
        }
        log("Aggregates: ");
        log("    Total transactions:            %d%n" +
                "    Min ledger:                    %d%n" +
                "    Max ledger:                    %d%n" +
                "    Successful outbound payments:  %d%n" +
                "    Newly funded accounts:         %d%n",
                total,
                minMaxTracker.min,
                minMaxTracker.max,
                successful,
                accountsFunded);

        log("Payments: ");
        for (Map.Entry<BigDecimal, Integer> entry : payments.entrySet()) {
            BigDecimal key = entry.getKey();
            Integer integer = createdStats.get(key);
            integer = integer == null ? 0 : integer;
            log("    %7s/XRP: total: %4d, funding: %d", key.toPlainString(),
                    entry.getValue(),
                    integer);
        }
    }

    private static BufferedReader openDumpReader() throws FileNotFoundException {
        FileReader reader = new FileReader("binary-transactions.json");
        return new BufferedReader(reader);
    }
}
