package com.ripple.encodings;

import com.ripple.core.TestFixtures;
import org.junit.Assert;
import org.junit.Test;

import static com.ripple.config.Config.getB58IdentiferCodecs;
import static org.junit.Assert.assertEquals;

public class B58IdentifierTest {
    @Test
    public void testDecodeFamilySeed() {
        Assert.assertArrayEquals(TestFixtures.master_seed_bytes,
                getB58IdentiferCodecs().decodeFamilySeed(TestFixtures.master_seed));
    }
    @Test
    public void testEncodeFamilySeed() {
        String masterSeedStringRebuilt = getB58IdentiferCodecs().encodeFamilySeed(TestFixtures.master_seed_bytes);
        assertEquals(TestFixtures.master_seed, masterSeedStringRebuilt);
    }
}
