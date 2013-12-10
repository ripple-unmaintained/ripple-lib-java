package com.ripple.core.known.sle;

import com.ripple.core.types.Amount;
import com.ripple.core.types.Quality;
import com.ripple.core.types.STObject;

import java.math.BigDecimal;

public class Offer extends STObject {
    BigDecimal quality() {
        return Quality.fromOfferBookDirectory(this);
    }

    public BigDecimal askQuality() {
        return get(Amount.TakerPays).computeQuality(get(Amount.TakerGets));
    }

    public BigDecimal bidQuality() {
        return get(Amount.TakerGets).computeQuality(get(Amount.TakerPays));
    }

    public Amount getsOne() {
        return get(Amount.TakerGets).oneAtXRPScale();
    }
    public Amount paysOne() {
        return get(Amount.TakerPays).oneAtXRPScale();
    }

}
