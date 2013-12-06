package com.ripple.cli;


import com.ripple.client.transactions.TransactionResult;
import com.ripple.core.enums.TransactionEngineResult;
import com.ripple.core.enums.TransactionType;
import com.ripple.core.types.AccountID;
import com.ripple.core.types.Amount;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static com.ripple.cli.log.Log.LOG;

public class AnalyzeDump {
    public static void main(String[] args) throws IOException, JSONException {
        analyzeDump();
    }
    private static void analyzeDump() throws IOException, JSONException {
        BufferedReader bufferedReader = openDumpReader();

        AccountID giveAwayAccount = AccountID.fromAddress("rMTzGg7nPPEMJthjgEBfiPZGoAM7MEVa1r");
        Amount    giveAwayAmount  = Amount.fromString("1000.0");

        int successful = 0, created = 0;
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            if (line.length() > 0) {
                JSONObject json = new JSONObject(line);
                JSONArray transactions = json.getJSONArray("transactions");
                for (int i = 0; i < transactions.length(); i++) {
                    JSONObject tx = transactions.getJSONObject(i);
                    TransactionResult tr;
                    tr = new TransactionResult(tx, TransactionResult.Source.request_account_tx_binary);

                    if (tr.engineResult      == TransactionEngineResult.tesSUCCESS &&
                        tr.transactionType() == TransactionType.Payment            &&
                        tr.initiatingAccount().equals(giveAwayAccount)             &&
                        tr.transaction.get(Amount.Amount).equals(giveAwayAmount)) {
                        if (tr.createdAccount() != null) created++;
                        successful++;
                    }
                }
            }
        }

        LOG("Successful outbound %s payments: %d, creating: %d, paid out %s",
             giveAwayAmount, successful, created, giveAwayAmount.multiply(successful));
    }

    private static BufferedReader openDumpReader() throws FileNotFoundException {
        FileReader reader = new FileReader("binary-transactions.json");
        return new BufferedReader(reader);
    }
}
