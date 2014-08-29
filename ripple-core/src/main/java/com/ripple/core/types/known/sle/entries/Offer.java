package com.ripple.core.types.known.sle.entries;

import com.ripple.core.coretypes.*;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.Index;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.coretypes.uint.UInt64;
import com.ripple.core.enums.LedgerEntryType;
import com.ripple.core.fields.Field;
import com.ripple.core.types.known.sle.ThreadedLedgerEntry;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

public class Offer extends ThreadedLedgerEntry {
    public Offer() {
        super(LedgerEntryType.Offer);
    }

    /**
     * Use the BookDirectory field
     *
     * @return how much must `pay` to `get` one.
     *
     */
    public BigDecimal directoryAskQuality() {
        return Quality.fromBookDirectory(bookDirectory(),
                takerPays().isNative(),
                takerGets().isNative());
    }

    /**
     * @return how much must `pay` to `get` one.
     */
    public BigDecimal askQuality() {
        return takerPays().computeQuality(takerGets());
    }

    /**
     * @return how much `get` if `pay` one.
     */
    public BigDecimal bidQuality() {
        return takerGets().computeQuality(takerPays());
    }

    /**
     *
     * @return One of TakerGets issue, eg 1/USD/BITSTAMP or 1/EUR/SNAPSWAP
     */
    public Amount getsOne() {
        return takerGets().one();
    }
    /**
     *
     * @return One of TakerPays issue, eg 1/USD/BITSTAMP or 1/EUR/SNAPSWAP
     */
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
        executed.put(Amount.TakerPays, finalFields.get(Amount.TakerPays).subtract(takerPays()));
        executed.put(Amount.TakerGets, finalFields.get(Amount.TakerGets).subtract(takerGets()));
        return executed;
    }

    private Hash256 lineIndex(Amount amt) {
        Issue issue = amt.issue();
        if (amt.isNative()) throw new AssertionError();
        return Index.rippleState(account(), issue.issuer(), issue.currency());
    }

    public Vector256 lineIndexes() {
        Vector256 ret = new Vector256();
        for (Amount amt : new Amount[]{takerGets(), takerPays()}) {
            if (!amt.isNative()){
                ret.add(lineIndex(amt));
            }
        }
        return ret;
    }

    public Hash256 bookBase() {
        return Index.bookStart(takerPays().issue(), takerGets().issue());
    }

    public boolean belongsToBook(Hash256 bookBase) {
        byte[] baseBytes = bookBase.bytes();
        byte[] directoryBytes = bookDirectory().bytes();

        for (int i = 0; i < 24; i++) {
            if (baseBytes[i] != directoryBytes[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean sellingOwnFunds() {
        return account().equals(takerGets().issuer());
    }

    public Amount takerGetsFunded() {
        return has(Field.taker_gets_funded) ? get(Amount.taker_gets_funded) : takerGets();
    }
    public Amount takerPaysFunded() {
        return has(Field.taker_pays_funded) ? get(Amount.taker_pays_funded) : takerPays();
    }

    public static Comparator<Offer> qualityAscending = new Comparator<Offer>() {
        @Override
        public int compare(Offer lhs, Offer rhs) {
            return lhs.directoryAskQuality().compareTo(rhs.directoryAskQuality());
        }
    };

    public static Iterator<Offer> iterateCollection(Collection<STObject> offers) {
        final Iterator<STObject> iterator = offers.iterator();

        return new Iterator<Offer>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Offer next() {
                return (Offer) iterator.next();
            }

            @Override
            public void remove() {
                iterator.remove();

            }
        };
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
