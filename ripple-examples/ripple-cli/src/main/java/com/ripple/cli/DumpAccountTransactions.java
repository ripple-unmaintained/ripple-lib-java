package com.ripple.cli;

import com.ripple.client.Client;
import com.ripple.client.ClientLogger;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.enums.Command;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import com.sun.org.apache.xpath.internal.SourceTree;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

public class DumpAccountTransactions {
    public static String outputFile = "binary-transactions.json";
//    private static String theAccount = "rMTzGg7nPPEMJthjgEBfiPZGoAM7MEVa1r";
//    private static String theAccount = "rwVJd5YWGhvhhDEPaJR3hjN5feGk6bQXmY";
    private static String theAccount = "rHTLdR8F4v9gJmLoW6Fk6zmBvCM2bqzqLP";

    public static void main(String[] args) throws Exception {
        File file = new File(outputFile);
        if (file.exists()) {
            boolean delete = file.delete();
        }

        ClientLogger.quiet = true;
        Client c = new Client(new JavaWebSocketTransportImpl());
        c.connect("wss://s1.ripple.com");
        walkAccountTx(c, null, 5); // a null marker to start with and 5 pages of 500 txn per page
    }

    private static void walkAccountTx(final Client c, final Object marker, final int pages) {
        Request request = c.newRequest(Command.account_tx);
        request.json("binary", true);
        request.json("account", theAccount);

        if (marker != null) {
            request.json("marker", marker);
        }
        request.json("ledger_index_max", -1);

        request.once(Request.OnSuccess.class, new Request.OnSuccess() {
            @Override
            public void called(Response response) {
                JSONObject result = response.result;
                try {
                    JSONArray transactions = result.getJSONArray("transactions");
                    System.out.printf("Found %d (more) transactions %n",transactions.length());
                    appendTo(outputFile, result.toString());

                    Object newMarker = result.opt("marker");
                    System.out.printf("Marker %s%n", newMarker);
                    if (marker != null && newMarker != null && marker.toString().equals(newMarker.toString())) {
                        // This shouldn't happen since Stef's patch but who knows how
                        // pervasively it's been deployed ?
//                        return;
                        newMarker = null;
                    }
                    if ((newMarker != null) && (pages - 1 > 0) && transactions.length() > 0) {
                        System.out.printf("Found new marker %s%n", newMarker);
                        walkAccountTx(c, newMarker, pages - 1);
                    }
                    else {
                        System.out.printf("Found all transactions");
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        });

        request.request();
    }

    private static void appendTo(String outputFile, String result) throws IOException {
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, true)));
        writer.write(result);
        writer.write("\n");
        writer.close();
    }

}
