package com.ripple.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.ripple.crypto.ecdsa.KeyPair;
import com.ripple.encodings.common.B16;
import org.json.JSONArray;
import org.junit.Test;
import org.ripple.bouncycastle.util.encoders.Hex;

import com.ripple.core.coretypes.AccountID;
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.crypto.ecdsa.Seed;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class KeyPairTest {
    IKeyPair keyPair = Seed.createKeyPair(TestFixtures.master_seed_bytes);

    @Test
    public void testVerify() {
        assertTrue(keyPair.verify(TestFixtures.master_seed_bytes,
                Hex.decode(TestFixtures.singed_master_seed_bytes)));
    }

    @Test
    public void sanityTestSignAndVerify() {
        assertTrue(keyPair.verify(TestFixtures.master_seed_bytes,
                keyPair.sign(TestFixtures.master_seed_bytes)));
    }

    @Test
    public void testDerivationFromSeedBytes() {
        assertEquals("0330E7FC9D56BB25D6893BA3F317AE5BCF33B3291BD63DB32654A313222F7FD020", keyPair.pubHex());
        assertEquals("1ACAAEDECE405B2A958212629E16F2EB46B153EEE94CDD350FDEFF52795525B7", keyPair.privHex());
    }

    @Test
    public void testDerivationFromString() {
        IKeyPair keyPairFromSeed = AccountID.keyPairFromSeedString(TestFixtures.master_seed);
        assertEquals("0330E7FC9D56BB25D6893BA3F317AE5BCF33B3291BD63DB32654A313222F7FD020", keyPairFromSeed.pubHex());
        assertEquals("1ACAAEDECE405B2A958212629E16F2EB46B153EEE94CDD350FDEFF52795525B7", keyPairFromSeed.privHex());
    }

    static public String getFileText(String filename) throws IOException {
        FileReader f = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(f);
        StringBuilder b = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            b.append(line);
        }
        return b.toString();
    }

//    @Test
    public void testRippleLibGarbage() throws Exception {
        String text = getFileText("/home/nick/ripple-lib/dumps.json");
        JSONArray array = new JSONArray(text);

        AccountID root = AccountID.fromString("root");
        IKeyPair kp = root.getKeyPair();
        byte[] zeros = new byte[32];

        for (int i = 0; i < array.length(); i++) {
            String sig = array.getString(i);
            byte[] sigBytes = B16.decode(sig);
            assertTrue(KeyPair.isStrictlyCanonical(sigBytes));
            assertTrue(kp.verify(zeros, sigBytes));
        }
    }
}
