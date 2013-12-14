
package com.ripple.service;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.ripple.bouncycastle.crypto.InvalidCipherTextException;

import com.ripple.android.client.AndroidClient;
import com.ripple.android.profile.User;
import com.ripple.client.Account;
import com.ripple.client.blobvault.BlobVault;

public class RippleService {
    private static final String BLOBVAULT_SERVER = "https://blobvault.payward.com/";

    private static final String TAG = RippleService.class.getName();

    private static ObjectMapper mObjectMapper = new ObjectMapper().configure(
            JsonParser.Feature.ALLOW_COMMENTS, true).configure(
            JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

    private BlobVault blobVault = new BlobVault(BLOBVAULT_SERVER);

    private AndroidClient rippleClient = new AndroidClient();

    public User login(String userName, String password) {
        try {
            // connectServer("wss://s1.ripple.com");
            User user = convertToUser(getBlob(userName, password));
            user.setUserName(userName);
            // rippleClient.accountFromSeed(user.getMasterSeed());
            // user.setBalance(getAccountFromSeed(user.getMasterSeed()).getAccountRoot().getBalance()
            // .value());
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    private void connectServer(String uri) {
        rippleClient.connect(uri);
    }

    private boolean isConnected() {
        return rippleClient.connected;
    }

    private JSONObject getBlob(String userName, String password) throws InvalidCipherTextException,
            IOException {
        return blobVault.getBlob(userName, password);
    }

    public Account getAccountFromSeed(String masterSeed) {
        if (!isConnected()) {
            connectServer("wss://s1.ripple.com");
        }
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

    public AndroidClient getClient() {
        return rippleClient;
    }
}
