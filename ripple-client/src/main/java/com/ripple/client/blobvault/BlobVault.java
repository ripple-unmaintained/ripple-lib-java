
package com.ripple.client.blobvault;

import com.ripple.crypto.sjcljson.JSONEncrypt;
import com.ripple.encodings.common.B16;
import org.ripple.bouncycastle.crypto.InvalidCipherTextException;
import org.ripple.bouncycastle.util.encoders.Base64;
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

    class BlobNotFound extends RuntimeException {
        public BlobNotFound(String ast) {
            super(ast);
        }
    }

    public JSONObject getBlob(String user, String pass) throws IOException,
            InvalidCipherTextException {
        user = user.toLowerCase();
        String userPassUrl = userPassHash(user, pass);
        System.out.println("User " + user);

        System.out.println(userPassUrl);
        //0bdf63ac4ff8639dc4c0e411ab4d4be8f2a17a592db483836a779c8e60fc3ee2

        URL blobUrl = new URL(baseUrl + userPassUrl);
//        HttpURLConnection getRequest = createGETRequestConnection(blobUrl);
//        int responseCode = getRequest.getResponseCode();
//        System.out.println(responseCode);
//        String data = readAllFromConnection(getRequest);
//
//        if (responseCode == 404 || data.length() == 0) {
//            // We won't log the pass
//            throw new BlobNotFound("No blob found for user: " + user);
//        }
        String data = "eyJpdiI6IlU3eWwvQ2RqUEVQekNYL1gzSXlabWc9PSIsInYiOjEsIml0ZXIiOjEwMDAsImtzIjoyNTYsInRzIjo2NCwibW9kZSI6ImNjbSIsImFkYXRhIjoiJTVCJTVEIiwiY2lwaGVyIjoiYWVzIiwic2FsdCI6Im8rSFFwaG1RZWkwPSIsImN0IjoiVmFuTjMvR3M3UmNoUHJtMU5kV0wrYzliYjdXQjNaQ29xR0cvOXk1QVlqU2pNSVVhRjFLWUdsQ2V0S2FjYnpRUHkzak04TnFGMW9GaDMzLzltOU56bXZZNTVldU1vUkNYeFd1ZXQvQllPMFFYZG1XMUtubjBsZzl6ejBTZlhacHZqRzhBY3ZncEUzT0Y1bFBndnNGMm5vbkhPL2J3Q0dZYytEVGRTUlVOSHZxZllUOGlWditJenB5MC85SEdhTGhVUzl4TzZidkVJNHhJVVFxVEY3aFdDaDdEeDRiZWtkMzgxSWhucUV6d0Zlei9Ub1JUYXFZTFd2eFI2aDlyeENReW4xVGdyMDFvS2w0cG9rK0QvRXZKZDh0Y2VUT2huVXRVQ2hrai9VeGVZZVN1NUtGL2VLTXpsWTFTUGc9PSJ9";
        String utf8 = base64decodeUTF8(data);

        System.out.println(utf8);
        String decryptionKey;
        try {
            decryptionKey = userPassDerivedDecryptionKey(user, pass);
            System.out.println(decryptionKey);
            return sjcl.decrypt(decryptionKey, utf8);
        } catch (InvalidCipherTextException e) {
            decryptionKey = userPassDerivedDecryptionKeyOLD(user, pass);
            System.out.println(decryptionKey);
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
        while ((line = buf.readLine()) != null)
            text.append(line);
        return text.toString();
    }

    private String base64decodeUTF8(String data) throws UnsupportedEncodingException {
        return new String(Base64.decode(data), "utf8");
    }

    public String userPassHash(String user, String pass) {
        String toHash = user + pass;
        try {
            byte[] toHashBytes = toHash.getBytes("utf8");
            byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(toHashBytes);
            return B16.toString(sha256);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
