package com.ripple.core.coretypes;

import com.ripple.core.coretypes.hash.Hash256;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Quality  {
    /**
     * Finds the quality (TakerPays/TakerGets) ratio packed into the last 64
     * bits of root DirectoryNode ledger indexes.
     */
    public static BigDecimal fromBookDirectory(Hash256 bookDirectory,
                                               boolean payIsNative,
                                               boolean getIsNative) {
        // The last 7 bytes contains the mantissa
        byte[] mantissa  = bookDirectory.slice(-7);
        // Most significant byte has the exponent packed
        int exponent = ( bookDirectory.get(-8) & 0xFF) - 100;
        // Return the value in XRP scale, rather than drops, as stored.
        int scale = -(payIsNative ? exponent - 6 :
                      getIsNative ? exponent + 6 : exponent);
        BigInteger unsignedBig = new BigInteger(1, mantissa);
        return new BigDecimal(unsignedBig, scale);
    }
}
