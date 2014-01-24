package com.ripple.core.coretypes;

import com.ripple.core.coretypes.hash.Hash256;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Quality  {
    public static BigDecimal fromBookDirectory(Hash256 bookDirectory, boolean payIsNative, boolean getIsNative) {
        byte[] value  = bookDirectory.slice(-7);
        int offset = bookDirectory.get(-8) - 100;
        return new BigDecimal(new BigInteger(1, value), -( payIsNative ? offset - 6 :
                                                           getIsNative ? offset + 6 :
                                                           offset ));
    }
    public static BigDecimal fromOfferBookDirectory(STObject offer) {
        return fromBookDirectory(offer.get(Hash256.BookDirectory),
                                 offer.get(Amount.TakerPays).isNative(),
                                 offer.get(Amount.TakerGets).isNative());
    }
}
