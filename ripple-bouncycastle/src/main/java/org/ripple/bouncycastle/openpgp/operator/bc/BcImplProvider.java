package org.ripple.bouncycastle.openpgp.operator.bc;

import org.ripple.bouncycastle.bcpg.HashAlgorithmTags;
import org.ripple.bouncycastle.bcpg.PublicKeyAlgorithmTags;
import org.ripple.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.ripple.bouncycastle.crypto.AsymmetricBlockCipher;
import org.ripple.bouncycastle.crypto.BlockCipher;
import org.ripple.bouncycastle.crypto.Digest;
import org.ripple.bouncycastle.crypto.Signer;
import org.ripple.bouncycastle.crypto.digests.MD2Digest;
import org.ripple.bouncycastle.crypto.digests.MD5Digest;
import org.ripple.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.ripple.bouncycastle.crypto.digests.SHA1Digest;
import org.ripple.bouncycastle.crypto.digests.SHA224Digest;
import org.ripple.bouncycastle.crypto.digests.SHA256Digest;
import org.ripple.bouncycastle.crypto.digests.SHA384Digest;
import org.ripple.bouncycastle.crypto.digests.SHA512Digest;
import org.ripple.bouncycastle.crypto.digests.TigerDigest;
import org.ripple.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.ripple.bouncycastle.crypto.engines.AESEngine;
import org.ripple.bouncycastle.crypto.engines.BlowfishEngine;
import org.ripple.bouncycastle.crypto.engines.CAST5Engine;
import org.ripple.bouncycastle.crypto.engines.DESEngine;
import org.ripple.bouncycastle.crypto.engines.DESedeEngine;
import org.ripple.bouncycastle.crypto.engines.ElGamalEngine;
import org.ripple.bouncycastle.crypto.engines.RSABlindedEngine;
import org.ripple.bouncycastle.crypto.engines.TwofishEngine;
import org.ripple.bouncycastle.crypto.signers.DSADigestSigner;
import org.ripple.bouncycastle.crypto.signers.DSASigner;
import org.ripple.bouncycastle.crypto.signers.RSADigestSigner;
import org.ripple.bouncycastle.openpgp.PGPException;
import org.ripple.bouncycastle.openpgp.PGPPublicKey;

class BcImplProvider
{
    static Digest createDigest(int algorithm)
        throws PGPException
    {
        switch (algorithm)
        {
        case HashAlgorithmTags.SHA1:
            return new SHA1Digest();
        case HashAlgorithmTags.SHA224:
            return new SHA224Digest();
        case HashAlgorithmTags.SHA256:
            return new SHA256Digest();
        case HashAlgorithmTags.SHA384:
            return new SHA384Digest();
        case HashAlgorithmTags.SHA512:
            return new SHA512Digest();
        case HashAlgorithmTags.MD2:
            return new MD2Digest();
        case HashAlgorithmTags.MD5:
            return new MD5Digest();
        case HashAlgorithmTags.RIPEMD160:
            return new RIPEMD160Digest();
        case HashAlgorithmTags.TIGER_192:
            return new TigerDigest();
        default:
            throw new PGPException("cannot recognise digest");
        }
    }

    static Signer createSigner(int keyAlgorithm, int hashAlgorithm)
        throws PGPException
    {
        switch(keyAlgorithm)
        {
        case PublicKeyAlgorithmTags.RSA_GENERAL:
        case PublicKeyAlgorithmTags.RSA_SIGN:
            return new RSADigestSigner(createDigest(hashAlgorithm));
        case PublicKeyAlgorithmTags.DSA:
            return new DSADigestSigner(new DSASigner(), createDigest(hashAlgorithm));
        default:
            throw new PGPException("cannot recognise keyAlgorithm");
        }
    }

    static BlockCipher createBlockCipher(int encAlgorithm)
        throws PGPException
    {
        BlockCipher engine;

        switch (encAlgorithm)
        {
        case SymmetricKeyAlgorithmTags.AES_128:
        case SymmetricKeyAlgorithmTags.AES_192:
        case SymmetricKeyAlgorithmTags.AES_256:
            engine = new AESEngine();
            break;
        case SymmetricKeyAlgorithmTags.BLOWFISH:
            engine = new BlowfishEngine();
            break;
        case SymmetricKeyAlgorithmTags.CAST5:
            engine = new CAST5Engine();
            break;
        case SymmetricKeyAlgorithmTags.DES:
            engine = new DESEngine();
            break;
        case SymmetricKeyAlgorithmTags.TWOFISH:
            engine = new TwofishEngine();
            break;
        case SymmetricKeyAlgorithmTags.TRIPLE_DES:
            engine = new DESedeEngine();
            break;
        default:
            throw new PGPException("cannot recognise cipher");
        }

        return engine;
    }

    static AsymmetricBlockCipher createPublicKeyCipher(int encAlgorithm)
        throws PGPException
    {
        AsymmetricBlockCipher c;

        switch (encAlgorithm)
        {
        case PGPPublicKey.RSA_ENCRYPT:
        case PGPPublicKey.RSA_GENERAL:
            c = new PKCS1Encoding(new RSABlindedEngine());
            break;
        case PGPPublicKey.ELGAMAL_ENCRYPT:
        case PGPPublicKey.ELGAMAL_GENERAL:
            c = new PKCS1Encoding(new ElGamalEngine());
            break;
        case PGPPublicKey.DSA:
            throw new PGPException("Can't use DSA for encryption.");
        case PGPPublicKey.ECDSA:
            throw new PGPException("Can't use ECDSA for encryption.");
        default:
            throw new PGPException("unknown asymmetric algorithm: " + encAlgorithm);
        }

        return c;
    }
}
