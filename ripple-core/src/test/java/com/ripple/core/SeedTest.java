package com.ripple.core;

import com.ripple.crypto.ecdsa.Seed;
import org.junit.Test;

import static com.ripple.config.Config.getB58IdentiferCodecs;
import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: nick
 * Date: 8/12/13
 * Time: 3:56 PM
 */
public class SeedTest {
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

    public static String phraseToFamilySeed(String passphrase) {
        return getB58IdentiferCodecs().encodeFamilySeed(Seed.passPhraseToSeedBytes(passphrase));
    }
}
