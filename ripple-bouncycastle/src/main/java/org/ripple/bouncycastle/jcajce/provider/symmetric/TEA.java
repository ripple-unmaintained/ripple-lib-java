package org.ripple.bouncycastle.jcajce.provider.symmetric;

import org.ripple.bouncycastle.crypto.CipherKeyGenerator;
import org.ripple.bouncycastle.crypto.engines.TEAEngine;
import org.ripple.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import org.ripple.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher;
import org.ripple.bouncycastle.jcajce.provider.symmetric.util.BaseKeyGenerator;
import org.ripple.bouncycastle.jcajce.provider.symmetric.util.IvAlgorithmParameters;
import org.ripple.bouncycastle.jcajce.provider.util.AlgorithmProvider;

public final class TEA
{
    private TEA()
    {
    }
    
    public static class ECB
        extends BaseBlockCipher
    {
        public ECB()
        {
            super(new TEAEngine());
        }
    }

    public static class KeyGen
        extends BaseKeyGenerator
    {
        public KeyGen()
        {
            super("TEA", 128, new CipherKeyGenerator());
        }
    }

    public static class AlgParams
        extends IvAlgorithmParameters
    {
        protected String engineToString()
        {
            return "TEA IV";
        }
    }

    public static class Mappings
        extends AlgorithmProvider
    {
        private static final String PREFIX = TEA.class.getName();

        public Mappings()
        {
        }

        public void configure(ConfigurableProvider provider)
        {

            provider.addAlgorithm("Cipher.TEA", PREFIX + "$ECB");
            provider.addAlgorithm("KeyGenerator.TEA", PREFIX + "$KeyGen");
            provider.addAlgorithm("AlgorithmParameters.TEA", PREFIX + "$AlgParams");

        }
    }
}
