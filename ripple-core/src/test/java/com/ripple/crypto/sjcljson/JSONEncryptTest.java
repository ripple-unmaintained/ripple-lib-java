package com.ripple.crypto.sjcljson;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.ripple.bouncycastle.crypto.InvalidCipherTextException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JSONEncryptTest {
    @Test
    public void testDecryption() throws JSONException, InvalidCipherTextException {
        String fixture = "{\"key\" : \"user|pass\",   \"raw\":{\"one\":\"two\",\"three\":4}," +
                           "\"encrypted\":{\"iv\":\"OPiRt308ZbENmwzdDjffZQ==\"," +
                                          "\"v\":1," +
                                          "\"iter\":1000," +
                                          "\"ks\":256," +
                                          "\"ts\":64," +
                                          "\"mode\":\"ccm\"," +
                                          "\"adata\":\"\"," +
                                          "\"cipher\":\"aes\"," +
                                          "\"salt\":\"NFPNycJ3ea0=\"," +
                                          "\"ct\":\"sZo58l4VRX2KR9xAsbP/dIVc9QJ+0VCmTZ3jIMbO1w==\"}}";

        JSONObject parsed    = new JSONObject(fixture),
                   raw       = parsed.getJSONObject("raw"),
                   encrypted = parsed.getJSONObject("encrypted"),
                   decrypted,
                   reencrypted;

        int ks   =  256,
            iter = 1000,
            ts   =   64;

        JSONEncrypt jsonEncrypt = new JSONEncrypt(ks, iter, ts);

        String key = parsed.getString("key");
        decrypted  =  jsonEncrypt.decrypt(key, encrypted);

        assertEquals(decrypted.getString("one"), raw.getString("one"));
        assertEquals(decrypted.getInt("three"), raw.getInt("three"));


        reencrypted =  jsonEncrypt.encrypt(key, raw, "sucks to be you ...x!!!");
        jsonEncrypt.decrypt(key, reencrypted);

        reencrypted.put("adata", "0000" + reencrypted.getString("adata"));

        boolean thrown = false;
        try {
            jsonEncrypt.decrypt(key, reencrypted);
        } catch (InvalidCipherTextException e) {
            thrown = true;
        }
        assertTrue(thrown);

    }

    @Test
    public void testDecryption128Macsize() throws JSONException, InvalidCipherTextException, UnsupportedEncodingException {
        String fixture = "{" +
                            "\"key\" : \"user|pass\", " +
                            "\"raw\":{\"one\":\"two\",\"three\":4}," +
                            "\"encrypted\":" +
                            "{" +
                               "\"iv\":\"lgd/ZDGHEZOnbIXpViykXg==\"," +
                               "\"v\":1," +
                               "\"iter\":1000," +
                               "\"ks\":256," +
                               "\"ts\":128," +
                               "\"mode\":" +
                               "\"ccm\"," +
                               "\"adata\":" +
                               "\"wtf%20bbq%3F\"," +
                               "\"cipher\":\"aes\"," +
                               "\"salt\":\"NFPNycJ3ea0=\"," +
                               "\"ct\":\"GTvZENQJ97HTZp2UvW1C9Bxf7KBVlfKiOaR82njTMk45L/dP+tEG\"" +
                            "}" +
                         "}";

        JSONObject parsed    = new JSONObject(fixture),
                   raw       = parsed.getJSONObject("raw"),
                   encrypted = parsed.getJSONObject("encrypted"),
                   decrypted;

        int ks   =   256,
            iter =  1000,
            ts   =   128;

        String key = parsed.getString("key");
        JSONEncrypt jsonEncrypt = new JSONEncrypt(ks, iter, ts);
        decrypted  =  jsonEncrypt.decrypt(key, encrypted);

        assertEquals(decrypted.getString("one"), raw.getString("one"));
        assertEquals(decrypted.getInt("three"), raw.getInt("three"));

        String adata = URLDecoder.decode(encrypted.getString("adata"), "utf8");
        assertEquals(adata, "wtf bbq?");
    }

}
