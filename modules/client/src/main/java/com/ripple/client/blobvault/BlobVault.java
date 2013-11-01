package com.ripple.client.blobvault;

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

public class BlobVault {
    String baseUrl;
    public JSONEncrypt sjcl = new JSONEncrypt();

    public BlobVault(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public JSONObject getBlob(String user, String pass) throws IOException, InvalidCipherTextException {
        String userPassUrl = userPassHash(user, pass);
        URL blobUrl = new URL(baseUrl + userPassUrl);
        String data = readAllFromConnection(createGETRequestConnection(blobUrl));
        String utf8 = base64decodeUTF8(data);
        String decryptionKey;
        try {
            decryptionKey = userPassDerivedDecryptionKey(user, pass);
            return sjcl.decrypt(decryptionKey, utf8);
        } catch (InvalidCipherTextException e) {
            decryptionKey = userPassDerivedDecryptionKeyOLD(user, pass);
            return sjcl.decrypt(decryptionKey, utf8);
        }
    }

    private String userPassDerivedDecryptionKey(String user, String pass) {
        return user.length() + "|" + user + pass;
    }

    private String userPassDerivedDecryptionKeyOLD(String user, String pass) {
        return user + pass;
    }

    private HttpURLConnection createGETRequestConnection(URL website) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) website.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        return connection;
    }

    private String readAllFromConnection(HttpURLConnection connection) throws IOException {
        BufferedReader buf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder text = new StringBuilder();
        while((line = buf.readLine()) != null)
            text.append(line);
        return text.toString();
    }

    private String base64decodeUTF8(String data) throws UnsupportedEncodingException {
        return new String(Base64.decode(data), "utf8");
    }

    public String userPassHash(String user, String pass) {
        String toHash = user.toLowerCase() + pass;
        try {
            byte[] toHashBytes = toHash.getBytes("utf8");
            byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(toHashBytes);
            return B16.toString(sha256).toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}