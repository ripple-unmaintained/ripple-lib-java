
package com.ripple.service;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.ripple.bouncycastle.crypto.InvalidCipherTextException;

import com.ripple.android.profile.User;
import com.ripple.client.Account;
import com.ripple.client.Client;
import com.ripple.client.blobvault.BlobVault;
import com.ripple.client.subscriptions.AccountRoot;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;

public class RippleService {
    private static final String BLOBVAULT_SERVER = "https://blobvault.payward.com/";

    private static final String TAG = RippleService.class.getName();

    private static ObjectMapper mObjectMapper = new ObjectMapper().configure(
            JsonParser.Feature.ALLOW_COMMENTS, true).configure(
            JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

    private BlobVault blobVault = new BlobVault(BLOBVAULT_SERVER);

    private Client rippleClient = new Client(new JavaWebSocketTransportImpl());

    public User login(String userName, String password) {
        try {
            connectServer("wss://s1.ripple.com");
            User user = convertToUser(getBlob(userName, password));
            rippleClient.accountFromSeed(user.getMasterSeed());
            user.setBalance(getAccountFromSeed(user.getMasterSeed()).getAccountRoot().getBalance()
                    .value());
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    public void connectServer(String uri) {
        rippleClient.connect(uri);
    }

    public JSONObject getBlob(String userName, String password) throws InvalidCipherTextException,
            IOException {
        return blobVault.getBlob(userName, password);
    }

    public Account getAccountFromSeed(String masterSeed) {
        Account account = rippleClient.accountFromSeed(masterSeed);
        return account;
    }

    public static User convertToUser(JSONObject jo) {
        try {
            return mObjectMapper.readValue(jo.toString(), User.class);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

    }

    public static User convertToUser(InputStream is) {
        try {
            return mObjectMapper.readValue(is, User.class);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

    }
}
