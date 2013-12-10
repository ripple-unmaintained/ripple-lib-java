package com.ripple.core.known.sle;

import com.ripple.core.types.Amount;
import com.ripple.core.types.Quality;
import com.ripple.core.types.STObject;

import java.math.BigDecimal;

public class Offer extends STObject {
    BigDecimal quality() {
        return Quality.fromOfferBookDirectory(this);
    }

    public BigDecimal computeQuality() {
        return get(Amount.TakerPays).computeQuality(get(Amount.TakerGets));
    }

    public Amount getsOne() {
        return get(Amount.TakerGets).oneAtXRPScale();
    }
    public Amount paysOne() {
        return get(Amount.TakerPays).oneAtXRPScale();
    }

}
