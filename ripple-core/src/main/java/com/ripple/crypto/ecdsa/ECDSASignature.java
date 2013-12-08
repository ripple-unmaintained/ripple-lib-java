package com.ripple.crypto.ecdsa;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import org.ripple.bouncycastle.asn1.ASN1InputStream;
import org.ripple.bouncycastle.asn1.DERInteger;
import org.ripple.bouncycastle.asn1.DERSequenceGenerator;
import org.ripple.bouncycastle.asn1.DLSequence;

public class ECDSASignature {
    /** The two components of the signature. */
    public BigInteger r, s;

    /** Constructs a signature with the given components. */
    public ECDSASignature(BigInteger r, BigInteger s) {
        this.r = r;
        this.s = s;
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
            DERInteger r, s;
            try {
                r = (DERInteger) seq.getObjectAt(0);
                s = (DERInteger) seq.getObjectAt(1);
            } catch (ClassCastException e) {
                return null;
            }
            decoder.close();
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
        seq.addObject(new DERInteger(r));
        seq.addObject(new DERInteger(s));
        seq.close();
        return bos;
    }
}
