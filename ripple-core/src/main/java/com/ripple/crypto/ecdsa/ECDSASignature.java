package com.ripple.crypto.ecdsa;

import org.ripple.bouncycastle.asn1.ASN1InputStream;
import org.ripple.bouncycastle.asn1.ASN1Integer;
import org.ripple.bouncycastle.asn1.DERSequenceGenerator;
import org.ripple.bouncycastle.asn1.DLSequence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class ECDSASignature {
    /** The two components of the signature. */
    public BigInteger r, s;

    /** Constructs a signature with the given components. */
    public ECDSASignature(BigInteger r, BigInteger s) {
        this.r = r;
        this.s = s;
    }

    public static boolean isStrictlyCanonical(byte[] sig) {
        return checkIsCanonical(sig, true);
    }

    public static boolean checkIsCanonical(byte[] sig, boolean strict) {
        // Make sure signature is canonical
        // To protect against signature morphing attacks

        // Signature should be:
        // <30> <len> [ <02> <lenR> <R> ] [ <02> <lenS> <S> ]
        // where
        // 6 <= len <= 70
        // 1 <= lenR <= 33
        // 1 <= lenS <= 33

        int sigLen = sig.length;

        if ((sigLen < 8) || (sigLen > 72))
            return false;

        if ((sig[0] != 0x30) || (sig[1] != (sigLen - 2)))
            return false;

        // Find R and check its length
        int rPos = 4, rLen = sig[rPos - 1];

        if ((rLen < 1) || (rLen > 33) || ((rLen + 7) > sigLen))
            return false;

        // Find S and check its length
        int sPos = rLen + 6, sLen = sig[sPos - 1];
        if ((sLen < 1) || (sLen > 33) || ((rLen + sLen + 6) != sigLen))
            return false;

        if ((sig[rPos - 2] != 0x02) || (sig[sPos - 2] != 0x02))
            return false; // R or S have wrong type

        if ((sig[rPos] & 0x80) != 0)
            return false; // R is negative

        if ((sig[rPos] == 0) && rLen == 1)
            return false; // R is zero

        if ((sig[rPos] == 0) && ((sig[rPos + 1] & 0x80) == 0))
            return false; // R is padded

        if ((sig[sPos] & 0x80) != 0)
            return false; // S is negative

        if ((sig[sPos] == 0) && sLen == 1)
            return false; // S is zero

        if ((sig[sPos] == 0) && ((sig[sPos + 1] & 0x80) == 0))
            return false; // S is padded


        byte[] rBytes = new byte[rLen];
        byte[] sBytes = new byte[sLen];

        System.arraycopy(sig, rPos, rBytes, 0, rLen);
        System.arraycopy(sig, sPos, sBytes, 0, sLen);

        BigInteger r = new BigInteger(1, rBytes), s = new BigInteger(1, sBytes);

        BigInteger order = SECP256K1.order();

        if (r.compareTo(order) != -1 || s.compareTo(order) != -1) {
            return false; // R or S greater than modulus
        }
        if (strict) {
            return order.subtract(s).compareTo(s) != -1;
        } else {
            return true;
        }

    }

    /**
     * DER is an international standard for serializing data structures which is widely used in cryptography.
     * It's somewhat like protocol buffers but less convenient. This method returns a standard DER encoding
     * of the signature, as recognized by OpenSSL and other libraries.
     */
    public byte[] encodeToDER() {
        try {
            return derByteStream().toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    public static ECDSASignature decodeFromDER(byte[] bytes) {
        try {
            ASN1InputStream decoder = new ASN1InputStream(bytes);
            DLSequence seq = (DLSequence) decoder.readObject();
            ASN1Integer r, s;
            try {
                r = (ASN1Integer) seq.getObjectAt(0);
                s = (ASN1Integer) seq.getObjectAt(1);
            } catch (ClassCastException e) {
                return null;
            } finally {
                decoder.close();
            }
            // OpenSSL deviates from the DER spec by interpreting these values as unsigned, though they should not be
            // Thus, we always use the positive versions. See: http://r6.ca/blog/20111119T211504Z.html
            return new ECDSASignature(r.getPositiveValue(), s.getPositiveValue());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected ByteArrayOutputStream derByteStream() throws IOException {
        // Usually 70-72 bytes.
        ByteArrayOutputStream bos = new ByteArrayOutputStream(72);
        DERSequenceGenerator seq = new DERSequenceGenerator(bos);
        seq.addObject(new ASN1Integer(r));
        seq.addObject(new ASN1Integer(s));
        seq.close();
        return bos;
    }
}
