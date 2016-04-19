package com.ripple.core;

import com.ripple.config.Config;
import com.ripple.crypto.ecdsa.EDKeyPair;
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.crypto.ecdsa.Seed;
import com.ripple.encodings.B58IdentiferCodecs;
import com.ripple.encodings.common.B16;
import net.i2p.crypto.eddsa.math.FieldElement;
import net.i2p.crypto.eddsa.math.GroupElement;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.ripple.bouncycastle.util.Arrays;

import static org.junit.Assert.*;

public class SeedTest {
    static {
        Config.initBouncy();
    }
    private static final String[] ADDRESS_ARRAY = new String[] {
            "masterpassphrase", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", "0", "0330E7FC9D56BB25D6893BA3F317AE5BCF33B3291BD63DB32654A313222F7FD020",
            "masterpassphrase", "r4bYF7SLUMD7QgSLLpgJx38WJSY12ViRjP", "1", "02CD8C4CE87F86AAD1D9D18B03DE28E6E756F040BD72A9C127862833EB90D60BAD",
            "masterpassphrase", "rLpAd4peHUMBPbVJASMYK5GTBUSwXRD9nx", "2", "0259A57642A6F4AEFC9B8062AF453FDEEEAC5572BA602BB1DBD5EF011394C6F9FC",
            "otherpassphrase", "rpe3YWSVwGU2PmUzebAPg2deBXHtmba7hJ", "0", "022235A3DB2CAE57C60B7831929611D58867F86D28C0AD3C82473CC4A84990D01B",
            "otherpassphrase", "raAPC2gALSmsTkXR4wUwQcPgX66kJuLv2S", "5", "03F0619AFABE08D22D98C8721895FE3673B6174168949976F2573CE1138C124994",
            "yetanotherpassphrase", "rKnM44fS48qrGiDxB5fB5u64vHVJwjDPUo", "0", "0385AD049327EF7E5EC429350A15CEB23955037DE99660F6E70C11C5ABF4407036",
            "yetanotherpassphrase", "rMvkT1RHPfsZwTFbKDKBEisa5U4d2a9V8n", "1", "023A2876EA130CBE7BBA0573C2DB4C4CEB9A7547666915BD40366CDC6150CF54DC"
            // seedStr, addressBytes, accountNumber, keypair pub hex
    };

    private static final int ADDRESS_LINE_SIZE = ADDRESS_ARRAY.length / 7;

    @Test
    public void testPassphraseParsing() {
        assertEquals(TestFixtures.master_seed, phraseToFamilySeed("masterpassphrase"));
        // These were taken from rippled wallet propose
        assertEquals("ssbTMHrmEJP7QEQjWJH3a72LQipBM", phraseToFamilySeed("alice"));
        assertEquals("spkcsko6Ag3RbCSVXV2FJ8Pd4Zac1", phraseToFamilySeed("bob"));
        assertEquals("snzb83cV8zpLPTE4nQamoLP9pbhB7", phraseToFamilySeed("carol"));
        assertEquals("snczogzwPXNMFq6YPBE7SUwqzkWih", phraseToFamilySeed("dan"));
        assertEquals("snsBvdoBXhMYYUnabGieeBFWEdqRM", phraseToFamilySeed("bitstamp"));
        assertEquals("saDGZcfdL21t9gtoa3JiNUmMVReaS", phraseToFamilySeed("mtgox"));
        assertEquals("spigbKN5chn5wWyE8dvTN9wH36Ff1", phraseToFamilySeed("amazon"));
    }

    @Test
    public void testCreateRootKeyPair()
    {
        B58IdentiferCodecs b58 = Config.getB58IdentiferCodecs();
        Seed seed = Seed.fromPassPhrase("N4");
        IKeyPair rootPair = seed.rootKeyPair();
        String s = b58.encodeNodePublic(rootPair.canonicalPubBytes());
        assertEquals("n9JvnFJFCrdeRonKkuQHzE4VxT1QaG8Zo1VoG5okiTZ2S9B7ihsx", s);
    }

    @Test
    public void testsEd25519Seeds() {
        Seed seed = Seed.fromPassPhrase("N4").setEd25519();
        String sEdPrefixed = seed.toString();
        assertEquals("sEd", sEdPrefixed.substring(0, 3));
        Seed fromBase58 = Seed.fromBase58(sEdPrefixed);
        assertArrayEquals(seed.bytes(), fromBase58.bytes());
        assertArrayEquals(Seed.VER_ED25519, fromBase58.version());
        assertEquals(16, fromBase58.bytes().length);
        IKeyPair iKeyPair = seed.keyPair();
        assertTrue(iKeyPair instanceof EDKeyPair);
    }

    @Test
    public void testCreateKeypairFromAccountNumber() {
        for (int i = 0; i < ADDRESS_ARRAY.length; i=i+ADDRESS_LINE_SIZE) {
            IKeyPair keyPair = Seed.fromPassPhrase(ADDRESS_ARRAY[i]).keyPair(Integer.valueOf(ADDRESS_ARRAY[i+2]));
            assertEquals(ADDRESS_ARRAY[i+3], keyPair.canonicalPubHex());
        }
    }
    private static String phraseToFamilySeed(String passphrase) {
        return Seed.fromPassPhrase(passphrase).toString();
    }
}
