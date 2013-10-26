package com.ripple.cli;


import com.ripple.cli.log.Log;
import com.ripple.client.transactions.TransactionMessage.TransactionResult;
import com.ripple.core.enums.TransactionEngineResult;
import com.ripple.core.enums.TransactionType;
import com.ripple.core.types.AccountID;
import com.ripple.core.types.Amount;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class AnalyzeDump {
    public static void main(String[] args) throws IOException, JSONException {
        analyzeDump();
    }
    private static void analyzeDump() throws IOException, JSONException {
        FileReader reader = new FileReader("dump.json");
        BufferedReader bufferedReader = new BufferedReader(reader);
        AccountID giveAwayAccount = AccountID.fromAddress("rMTzGg7nPPEMJthjgEBfiPZGoAM7MEVa1r");

        Amount giveAwayAmount = Amount.fromString("888.0");

        int successful = 0, created = 0;
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            if (line.length() > 0) {
                JSONObject json = new JSONObject(line);
                JSONArray transactions = json.getJSONArray("transactions");
                for (int i = 0; i < transactions.length(); i++) {
                    JSONObject tx = transactions.getJSONObject(i);
                    TransactionResult tr;
                    tr = new TransactionResult(tx, TransactionResult.Source.request_account_tx);

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
        Log.LOG("Successful outbound 888xrp payments: %d, creating: %d, paid out %s",
                successful, created, giveAwayAmount.multiply(successful));
    }
}
