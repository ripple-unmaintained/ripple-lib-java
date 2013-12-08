package org.ripple.bouncycastle.openpgp.operator.bc;

import org.ripple.bouncycastle.crypto.BufferedBlockCipher;
import org.ripple.bouncycastle.crypto.InvalidCipherTextException;
import org.ripple.bouncycastle.openpgp.PGPException;
import org.ripple.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.ripple.bouncycastle.openpgp.operator.PGPDigestCalculatorProvider;

public class BcPBESecretKeyDecryptorBuilder
{
    private PGPDigestCalculatorProvider calculatorProvider;

    public BcPBESecretKeyDecryptorBuilder(PGPDigestCalculatorProvider calculatorProvider)
    {
        this.calculatorProvider = calculatorProvider;
    }

    public PBESecretKeyDecryptor build(char[] passPhrase)
    {
        return new PBESecretKeyDecryptor(passPhrase, calculatorProvider)
        {
            public byte[] recoverKeyData(int encAlgorithm, byte[] key, byte[] iv, byte[] keyData, int keyOff, int keyLen)
                throws PGPException
            {
                try
                {
                    BufferedBlockCipher c = BcUtil.createSymmetricKeyWrapper(false, BcImplProvider.createBlockCipher(encAlgorithm), key, iv);

                    byte[] out = new byte[keyLen];
                    int    outLen = c.processBytes(keyData, keyOff, keyLen, out, 0);

                    outLen += c.doFinal(out, outLen);

                    return out;
                }
                catch (InvalidCipherTextException e)
                {
                    throw new PGPException("decryption failed: " + e.getMessage(), e);
                }
            }
        };
    }
}
