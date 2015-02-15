package com.ripple.crypto.sjcljson;

import org.json.JSONObject;
import org.ripple.bouncycastle.crypto.InvalidCipherTextException;
import org.ripple.bouncycastle.crypto.PBEParametersGenerator;
import org.ripple.bouncycastle.crypto.digests.SHA256Digest;
import org.ripple.bouncycastle.crypto.engines.AESFastEngine;
import org.ripple.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.ripple.bouncycastle.crypto.modes.CCMBlockCipher;
import org.ripple.bouncycastle.crypto.params.AEADParameters;
import org.ripple.bouncycastle.crypto.params.KeyParameter;
import org.ripple.bouncycastle.util.encoders.Base64;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Arrays;

public class JSONEncrypt {
    int ks   = 256;
    int iter = 1000;
    int ts   = 64;
    String mode = "ccm";

    /**
     *  Much credit for this class goes to Matthew Fettig
     *  https://github.com/AurionFinancial/AndroidWallet/blob/master/src/com/ripple/Blobvault.java
     *
     *  This supports ccm mode encrypted data.
     *
     */
    public JSONEncrypt(int ks, int iter, int ts) {
        this.ks = ks;
        this.iter = iter;
        this.ts = ts;
    }

    public JSONEncrypt() {
    }

    public JSONObject encrypt(String key, JSONObject blob, String adata) {
        JSONObject result = new JSONObject();
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[32],
               salt = new byte[8];

        random.nextBytes(salt);
        random.nextBytes(iv);

        try {
            byte[] plainBytes = blob.toString().getBytes("UTF-8");
            byte[] adataBytes = adata.getBytes("utf8");
            byte[] nonce = computeNonce(iv, plainBytes);

            KeyParameter keyParam = this.createKey(key, salt, iter, ks);
            AEADParameters ccm = new AEADParameters(
                    keyParam,
                    macSize(ts),
                    nonce,
                    adataBytes);

            CCMBlockCipher aes = new CCMBlockCipher(new AESFastEngine());
            aes.init(true, ccm);

            byte[] enc = new byte[aes.getOutputSize(plainBytes.length)];

            int res = aes.processBytes(
                    plainBytes,
                    0,
                    plainBytes.length,
                    enc,
                    0);

            aes.doFinal(enc, res);

            result.put("ct", Base64.toBase64String(enc));
            result.put("iv", Base64.toBase64String(iv));
            result.put("salt", Base64.toBase64String(salt));
            result.put("adata", encodeAdata(adata));
            result.put("mode", mode);
            result.put("ks", ks);
            result.put("iter", iter);
            result.put("ts", ts);
            return result;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    private int macSize(int ms) {
        return ts;
    }

    public JSONObject decrypt(String key, String json) throws InvalidCipherTextException {
        return decrypt(key, new JSONObject(json));
    }

    public JSONObject decrypt(String key, JSONObject json) throws InvalidCipherTextException {
        try {

            byte[] iv = Base64.decode(json.getString("iv"));
            byte[] cipherText = Base64.decode(json.getString("ct"));
            byte[] adataBytes = decodeAdataBytes(json.getString("adata"));
            byte[] nonce = computeNonce(iv, cipherText);

            if (!json.getString("mode").equals("ccm")) {
                throw new RuntimeException("Can only decrypt ccm mode encrypted data");
            }

            KeyParameter keyParam = this.createKey(
                    key,
                    Base64.decode(json.getString("salt")),
                    json.getInt("iter"),
                    json.getInt("ks"));

            AEADParameters ccm = new AEADParameters(
                    keyParam,
                    macSize(json.getInt("ts")),
                    nonce,
                    adataBytes);

            CCMBlockCipher aes = new CCMBlockCipher(new AESFastEngine());
            aes.init(false, ccm);

            byte[] plainBytes = new byte[aes.getOutputSize(cipherText.length)];

            int res = aes.processBytes(
                    cipherText,
                    0,
                    cipherText.length,
                    plainBytes,
                    0);

            aes.doFinal(plainBytes, res);
            String text = new String(plainBytes, "UTF-8");
            return new JSONObject(text);
        } catch (InvalidCipherTextException e ) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String encodeAdata(String adata) {
        return JSEscape.escape(adata);
    }

    private byte[] decodeAdataBytes(String adata) {
        try {
            return JSEscape.unescape(adata).getBytes("utf8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private KeyParameter createKey(String password, byte[] salt,
                                   int iterations, int keySizeInBits) {

        PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(
                new SHA256Digest());
        generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(password.toCharArray()),
                       salt,
                       iterations);
        return (KeyParameter) generator.generateDerivedMacParameters(keySizeInBits);
    }

    private byte[] computeNonce(byte[] iv, byte[] plainBytes) {
        int ivl = iv.length;
        int ol = plainBytes.length  - (ts / 8);
        int l =2;
        while (l <4 && (ol >>> 8* l) != 0) l++;
        if (l < 15 - ivl) { l = 15-ivl; }
        int newLength = 15 - l;
        return Arrays.copyOf(iv, newLength);
    }
}
