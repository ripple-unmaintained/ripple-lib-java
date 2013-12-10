package com.ripple.core.types;

import com.ripple.core.types.hash.Hash256;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Quality  {
    public static BigDecimal fromBookDirectory(Hash256 bookDirectory, boolean isNative) {
        byte[] value  = bookDirectory.slice(-7);
        int offset = bookDirectory.get(-8) - 100;
        return new BigDecimal(new BigInteger(1, value), -(isNative ? offset - 6 : offset));
    }
    public static BigDecimal fromOfferBookDirectory(STObject offer) {
        boolean isNative  = offer.get(Amount.TakerGets).isNative() ||
                            offer.get(Amount.TakerPays).isNative();
        return fromBookDirectory(offer.get(Hash256.BookDirectory), isNative);
    }
}
