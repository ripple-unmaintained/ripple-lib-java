
package com.ripple.service;

import org.json.JSONObject;


import com.ripple.client.blobvault.BlobVault;

public class RippleService {
    private static final String BLOBVAULT_SERVER = "https://blobvault.payward.com/";

    private static final String TAG = RippleService.class.getName();

    private BlobVault blobVault = new BlobVault(BLOBVAULT_SERVER);

    public JSONObject login(String userName, String password) {
        try {
            return blobVault.getBlob(userName, password);
        } catch (Exception e) {
            return null;
        }
    }
}
