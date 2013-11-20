package com.ripple.cli;

import com.ripple.client.Client;
import com.ripple.client.ClientLogger;
import com.ripple.client.Request;
import com.ripple.client.Response;
import com.ripple.client.enums.Command;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

public class DumpAccountTransactions {
    public static String outputFile = "binary-transactions.json";

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
        request.json("account", "rMTzGg7nPPEMJthjgEBfiPZGoAM7MEVa1r");

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
                    System.out.printf("Found %d transactions %n",transactions.length());
                    appendTo(result.toString(), outputFile);

                    Object newMarker = result.get("marker");
                    if (marker != null && marker.toString().equals(newMarker.toString())) {
                        // This shouldn't happen since Stef's patch but who knows how
                        // pervasively it's been deployed ?
                        return;
                    }
                    if ((newMarker != null) && (pages - 1 > 0) && transactions.length() > 0) {
                        System.out.printf("Found new marker %s%n", newMarker);
                        walkAccountTx(c, newMarker, pages - 1);
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        request.request();
    }

    private static void appendTo(String result, String outputFile) throws IOException {
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, true)));
        writer.write(result);
        writer.write("\n");
        writer.close();
    }

}
