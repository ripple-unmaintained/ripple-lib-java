package org.ripple.bouncycastle.crypto.generators;

import org.ripple.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.ripple.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.ripple.bouncycastle.crypto.EphemeralKeyPair;
import org.ripple.bouncycastle.crypto.KeyEncoder;

public class EphemeralKeyPairGenerator
{
    private AsymmetricCipherKeyPairGenerator gen;
    private KeyEncoder keyEncoder;

    public EphemeralKeyPairGenerator(AsymmetricCipherKeyPairGenerator gen, KeyEncoder keyEncoder)
    {
        this.gen = gen;
        this.keyEncoder = keyEncoder;
    }

    public EphemeralKeyPair generate()
    {
        AsymmetricCipherKeyPair eph = gen.generateKeyPair();

        // Encode the ephemeral public key
         return new EphemeralKeyPair(eph, keyEncoder);
    }
}
