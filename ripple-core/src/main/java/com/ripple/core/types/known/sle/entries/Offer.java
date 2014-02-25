package com.ripple.core.types.known.sle.entries;

import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Quality;
import com.ripple.core.coretypes.STObject;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.coretypes.uint.UInt64;
import com.ripple.core.enums.LedgerEntryType;
import com.ripple.core.fields.Field;
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

    public Offer() {
        super(LedgerEntryType.Offer);
    }

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

    public UInt32 sequence() {return get(UInt32.Sequence);}
    public UInt32 expiration() {return get(UInt32.Expiration);}
    public UInt64 bookNode() {return get(UInt64.BookNode);}
    public UInt64 ownerNode() {return get(UInt64.OwnerNode);}
    public Hash256 bookDirectory() {return get(Hash256.BookDirectory);}
    public Amount takerPays() {return get(Amount.TakerPays);}
    public Amount takerGets() {return get(Amount.TakerGets);}
    public AccountID account() {return get(AccountID.Account);}
    public void sequence(UInt32 val) {put(Field.Sequence, val);}
    public void expiration(UInt32 val) {put(Field.Expiration, val);}
    public void bookNode(UInt64 val) {put(Field.BookNode, val);}
    public void ownerNode(UInt64 val) {put(Field.OwnerNode, val);}
    public void bookDirectory(Hash256 val) {put(Field.BookDirectory, val);}
    public void takerPays(Amount val) {put(Field.TakerPays, val);}
    public void takerGets(Amount val) {put(Field.TakerGets, val);}
    public void account(AccountID val) {put(Field.Account, val);}

}
