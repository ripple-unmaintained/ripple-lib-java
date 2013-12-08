package org.ripple.bouncycastle.crypto.tls;

import org.ripple.bouncycastle.crypto.AsymmetricBlockCipher;
import org.ripple.bouncycastle.crypto.CipherParameters;
import org.ripple.bouncycastle.crypto.CryptoException;
import org.ripple.bouncycastle.crypto.Digest;
import org.ripple.bouncycastle.crypto.Signer;
import org.ripple.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.ripple.bouncycastle.crypto.engines.RSABlindedEngine;
import org.ripple.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.ripple.bouncycastle.crypto.params.ParametersWithRandom;
import org.ripple.bouncycastle.crypto.params.RSAKeyParameters;
import org.ripple.bouncycastle.crypto.signers.GenericSigner;
import org.ripple.bouncycastle.crypto.signers.RSADigestSigner;
import org.ripple.bouncycastle.util.Arrays;

public class TlsRSASigner
    extends AbstractTlsSigner
{

    public byte[] generateRawSignature(AsymmetricKeyParameter privateKey, byte[] md5AndSha1)
        throws CryptoException
    {

        AsymmetricBlockCipher engine = createRSAImpl();
        engine.init(true, new ParametersWithRandom(privateKey, this.context.getSecureRandom()));
        return engine.processBlock(md5AndSha1, 0, md5AndSha1.length);
    }

    public boolean verifyRawSignature(byte[] sigBytes, AsymmetricKeyParameter publicKey, byte[] md5AndSha1)
        throws CryptoException
    {

        AsymmetricBlockCipher engine = createRSAImpl();
        engine.init(false, publicKey);
        byte[] signed = engine.processBlock(sigBytes, 0, sigBytes.length);
        return Arrays.constantTimeAreEqual(signed, md5AndSha1);
    }

    public Signer createSigner(AsymmetricKeyParameter privateKey)
    {
        return makeSigner(new CombinedHash(), true,
            new ParametersWithRandom(privateKey, this.context.getSecureRandom()));
    }

    public Signer createVerifyer(AsymmetricKeyParameter publicKey)
    {
        return makeSigner(new CombinedHash(), false, publicKey);
    }

    public boolean isValidPublicKey(AsymmetricKeyParameter publicKey)
    {
        return publicKey instanceof RSAKeyParameters && !publicKey.isPrivate();
    }

    protected Signer makeSigner(Digest d, boolean forSigning, CipherParameters cp)
    {
        Signer s;
        if (ProtocolVersion.TLSv12.isEqualOrEarlierVersionOf(context.getServerVersion().getEquivalentTLSVersion()))
        {
            /*
             * RFC 5246 4.7. In RSA signing, the opaque vector contains the signature generated
             * using the RSASSA-PKCS1-v1_5 signature scheme defined in [PKCS1].
             */
            s = new RSADigestSigner(d);
        }
        else
        {
            /*
             * RFC 5246 4.7. Note that earlier versions of TLS used a different RSA signature scheme
             * that did not include a DigestInfo encoding.
             */
            s = new GenericSigner(createRSAImpl(), d);
        }
        s.init(forSigning, cp);
        return s;
    }

    protected AsymmetricBlockCipher createRSAImpl()
    {
        /*
         * RFC 5264 7.4.7.1. Implementation note: It is now known that remote timing-based attacks
         * on TLS are possible, at least when the client and server are on the same LAN.
         * Accordingly, implementations that use static RSA keys MUST use RSA blinding or some other
         * anti-timing technique, as described in [TIMING].
         */
        return new PKCS1Encoding(new RSABlindedEngine());
    }
}
