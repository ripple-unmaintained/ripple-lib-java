package com.ripple.core.known.sle;

import com.ripple.core.types.Amount;
import com.ripple.core.types.Quality;
import com.ripple.core.types.STObject;

import java.math.BigDecimal;
import java.util.Comparator;

public class Offer extends STObject {

    public static Comparator<Offer> qualityAscending = new Comparator<Offer>() {
        @Override
        public int compare(Offer lhs, Offer rhs) {
            return lhs.directoryAskQuality().compareTo(rhs.directoryAskQuality());
        }
    };

    public BigDecimal directoryAskQuality() {
        return Quality.fromOfferBookDirectory(this);
    }

    // TODO: these methods would be useful on an OfferCreate transaction too
    public BigDecimal askQuality() {
        return get(Amount.TakerPays).computeQuality(get(Amount.TakerGets));
    }

    public BigDecimal bidQuality() {
        return get(Amount.TakerGets).computeQuality(get(Amount.TakerPays));
    }

    public Amount getsOne() {
        return get(Amount.TakerGets).one();
    }

    public Amount paysOne() {
        return get(Amount.TakerPays).one();
    }

    public String getPayCurrencyPair() {
        return get(Amount.TakerGets).currencyString() + "/" +
               get(Amount.TakerPays).currencyString();
    }

    public STObject inOut(STObject finals) {
        STObject inOut = new STObject();
        inOut.put(Amount.TakerPays, finals.get(Amount.TakerPays).subtract(get(Amount.TakerPays)).abs());
        inOut.put(Amount.TakerGets, finals.get(Amount.TakerGets).subtract(get(Amount.TakerGets)).abs());
        return inOut;
    }

    public Amount takerPays() {
        return get(Amount.TakerPays);
    }

    public Amount takerGets() {
        return get(Amount.TakerGets);
    }
}
