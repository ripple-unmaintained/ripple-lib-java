package com.ripple.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ripple.bouncycastle.util.encoders.Hex;

import com.ripple.core.types.AccountID;
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.crypto.ecdsa.Seed;

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
}
