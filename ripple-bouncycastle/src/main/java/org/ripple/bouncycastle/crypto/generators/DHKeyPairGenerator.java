package org.ripple.bouncycastle.crypto.generators;

import org.ripple.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.ripple.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.ripple.bouncycastle.crypto.KeyGenerationParameters;
import org.ripple.bouncycastle.crypto.params.DHKeyGenerationParameters;
import org.ripple.bouncycastle.crypto.params.DHParameters;
import org.ripple.bouncycastle.crypto.params.DHPrivateKeyParameters;
import org.ripple.bouncycastle.crypto.params.DHPublicKeyParameters;

import java.math.BigInteger;

/**
 * a Diffie-Hellman key pair generator.
 *
 * This generates keys consistent for use in the MTI/A0 key agreement protocol
 * as described in "Handbook of Applied Cryptography", Pages 516-519.
 */
public class DHKeyPairGenerator
    implements AsymmetricCipherKeyPairGenerator
{
    private DHKeyGenerationParameters param;

    public void init(
        KeyGenerationParameters param)
    {
        this.param = (DHKeyGenerationParameters)param;
    }

    public AsymmetricCipherKeyPair generateKeyPair()
    {
        DHKeyGeneratorHelper helper = DHKeyGeneratorHelper.INSTANCE;
        DHParameters dhp = param.getParameters();

        BigInteger x = helper.calculatePrivate(dhp, param.getRandom()); 
        BigInteger y = helper.calculatePublic(dhp, x);

        return new AsymmetricCipherKeyPair(
            new DHPublicKeyParameters(y, dhp),
            new DHPrivateKeyParameters(x, dhp));
    }
}
