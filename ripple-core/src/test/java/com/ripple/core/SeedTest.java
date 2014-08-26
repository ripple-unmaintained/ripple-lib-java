package com.ripple.core;

import static com.ripple.config.Config.getB58IdentiferCodecs;
import static org.junit.Assert.assertEquals;
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.crypto.ecdsa.Seed;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: nick
 * Date: 8/12/13
 * Time: 3:56 PM
 */
public class SeedTest {

    private static final String[] ADDRESS_ARRAY = new String[] {
            "masterpassphrase", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", /*INDEX*/ "0", "0330E7FC9D56BB25D6893BA3F317AE5BCF33B3291BD63DB32654A313222F7FD020",
            "masterpassphrase", "r4bYF7SLUMD7QgSLLpgJx38WJSY12ViRjP", /*INDEX*/ "1", "02CD8C4CE87F86AAD1D9D18B03DE28E6E756F040BD72A9C127862833EB90D60BAD",
            "masterpassphrase", "rLpAd4peHUMBPbVJASMYK5GTBUSwXRD9nx", /*INDEX*/ "2", "0259A57642A6F4AEFC9B8062AF453FDEEEAC5572BA602BB1DBD5EF011394C6F9FC",
            "otherpassphrase", "rpe3YWSVwGU2PmUzebAPg2deBXHtmba7hJ", /*INDEX*/ "0", "022235A3DB2CAE57C60B7831929611D58867F86D28C0AD3C82473CC4A84990D01B",
            "otherpassphrase", "raAPC2gALSmsTkXR4wUwQcPgX66kJuLv2S", /*INDEX*/ "5", "03F0619AFABE08D22D98C8721895FE3673B6174168949976F2573CE1138C124994",
            "yetanotherpassphrase", "rKnM44fS48qrGiDxB5fB5u64vHVJwjDPUo", /*INDEX*/ "0", "0385AD049327EF7E5EC429350A15CEB23955037DE99660F6E70C11C5ABF4407036",
            "yetanotherpassphrase", "rMvkT1RHPfsZwTFbKDKBEisa5U4d2a9V8n", /*INDEX*/ "1", "023A2876EA130CBE7BBA0573C2DB4C4CEB9A7547666915BD40366CDC6150CF54DC"
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
    public void testCreateKeypairFromAddress() {
        for (int i = 0; i < ADDRESS_ARRAY.length; i=i+ADDRESS_LINE_SIZE) {
            IKeyPair keyPair = Seed.createKeyPairFromAddress(ADDRESS_ARRAY[i], ADDRESS_ARRAY[i+1]);
            assertEquals(ADDRESS_ARRAY[i+3], keyPair.pubHex());
        }
    }

    public static String phraseToFamilySeed(String passphrase) {
        return getB58IdentiferCodecs().encodeFamilySeed(Seed.passPhraseToSeedBytes(passphrase));
    }
}
