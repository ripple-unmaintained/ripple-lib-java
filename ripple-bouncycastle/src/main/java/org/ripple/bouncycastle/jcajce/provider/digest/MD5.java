package org.ripple.bouncycastle.jcajce.provider.digest;

import org.ripple.bouncycastle.asn1.iana.IANAObjectIdentifiers;
import org.ripple.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.ripple.bouncycastle.crypto.CipherKeyGenerator;
import org.ripple.bouncycastle.crypto.digests.MD5Digest;
import org.ripple.bouncycastle.crypto.macs.HMac;
import org.ripple.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import org.ripple.bouncycastle.jcajce.provider.symmetric.util.BaseKeyGenerator;
import org.ripple.bouncycastle.jcajce.provider.symmetric.util.BaseMac;

public class MD5
{
    private MD5()
    {

    }

    /**
     * MD5 HashMac
     */
    public static class HashMac
        extends BaseMac
    {
        public HashMac()
        {
            super(new HMac(new MD5Digest()));
        }
    }

    public static class KeyGenerator
        extends BaseKeyGenerator
    {
        public KeyGenerator()
        {
            super("HMACMD5", 128, new CipherKeyGenerator());
        }
    }

    static public class Digest
        extends BCMessageDigest
        implements Cloneable
    {
        public Digest()
        {
            super(new MD5Digest());
        }

        public Object clone()
            throws CloneNotSupportedException
        {
            Digest d = (Digest)super.clone();
            d.digest = new MD5Digest((MD5Digest)digest);

            return d;
        }
    }

    public static class Mappings
        extends DigestAlgorithmProvider
    {
        private static final String PREFIX = MD5.class.getName();

        public Mappings()
        {
        }

        public void configure(ConfigurableProvider provider)
        {
            provider.addAlgorithm("MessageDigest.MD5", PREFIX + "$Digest");
            provider.addAlgorithm("Alg.Alias.MessageDigest." + PKCSObjectIdentifiers.md5, "MD5");

            addHMACAlgorithm(provider, "MD5", PREFIX + "$HashMac", PREFIX + "$KeyGenerator");
            addHMACAlias(provider, "MD5", IANAObjectIdentifiers.hmacMD5);
        }
    }
}
