package com.ripple.client.payward;

import com.ripple.crypto.sjcljson.JSONEncrypt;
import com.ripple.encodings.common.B16;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.util.encoders.Base64;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

public class PayWard {
    public static JSONEncrypt sjcl = new JSONEncrypt();

    public static JSONObject getBlob(String user, String pass) throws IOException, InvalidCipherTextException {
        String userPassUrl = userPassHash(user, pass);
        URL blobUrl = new URL("https://blobvault.payward.com/" + userPassUrl);
        String data = readAllFromConnection(createGETRequestConnection(blobUrl));
        String utf8 = base64decodeUTF8(data);
        String decryptionKey = user.length() + "|" + user + pass;
        return sjcl.decrypt(decryptionKey, utf8);
    }

    private static HttpURLConnection createGETRequestConnection(URL website) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) website.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        return connection;
    }

    private static String readAllFromConnection(HttpURLConnection connection) throws IOException {
        BufferedReader buf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder text = new StringBuilder();
        while((line = buf.readLine()) != null)
            text.append(line);
        return text.toString();
    }

    private static String base64decodeUTF8(String data) throws UnsupportedEncodingException {
        return new String(Base64.decode(data), "utf8");
    }

    private static String userPassHash(String user, String pass) {
        String toHash = user + pass;
        try {
            byte[] toHashBytes = toHash.getBytes("utf8");
            byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(toHashBytes);
            return B16.toString(sha256).toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
