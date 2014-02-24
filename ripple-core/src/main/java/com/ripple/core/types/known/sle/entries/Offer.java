package com.ripple.core.types.known.sle.entries;

import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Quality;
import com.ripple.core.coretypes.STObject;
import com.ripple.core.types.known.sle.ThreadedLedgerEntry;

import java.math.BigDecimal;
import java.util.Comparator;

public class Offer extends ThreadedLedgerEntry {

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
        return takerPays().computeQuality(takerGets());
    }

    public BigDecimal bidQuality() {
        return takerGets().computeQuality(takerPays());
    }

    public Amount getsOne() {
        return takerGets().one();
    }

    public Amount paysOne() {
        return takerPays().one();
    }

    public String getPayCurrencyPair() {
        return takerGets().currencyString() + "/" +
               takerPays().currencyString();
    }

    public STObject executed(STObject finalFields) {
        // where `this` is an AffectedNode nodeAsPrevious
        STObject executed = new STObject();
        executed.put(Amount.TakerPays, finalFields.get(Amount.TakerPays).subtract(takerPays()).abs());
        executed.put(Amount.TakerGets, finalFields.get(Amount.TakerGets).subtract(takerGets()).abs());
        return executed;
    }

    public Amount takerPays() {
        return get(Amount.TakerPays);
    }

    public Amount takerGets() {
        return get(Amount.TakerGets);
    }
}
