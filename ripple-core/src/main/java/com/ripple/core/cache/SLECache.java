package com.ripple.core.cache;

import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.sle.LedgerEntry;
import com.ripple.core.types.known.tx.result.AffectedNode;
import com.ripple.core.types.known.tx.result.TransactionMeta;
import com.ripple.core.types.known.tx.result.TransactionResult;

import java.util.TreeMap;

public class SLECache {
    private final TreeMap<Hash256, CacheEntry> cache = new TreeMap<Hash256, CacheEntry>();

    public static class CacheEntry {
        public LedgerEntry le;
        public UInt32  prevTxnIndex;
        public UInt32 prevLedger;
        public boolean deleted = false;

        public void upateLedgerEntry(LedgerEntry le, UInt32 ledgerIndex, UInt32 txnIndex) {
            if (doUpdate(txnIndex, ledgerIndex)) {
                // in the first case
                prevTxnIndex = txnIndex;
                prevLedger = ledgerIndex;

                if (le == null) {
                    deleted = true;
                }
                this.le = le;
            }
        }

        private boolean doUpdate(UInt32 txnIndex, UInt32 ledgerIndex) {
            if (le == null && !deleted) {
                return true;
            }
            if (prevLedger == null) {
                return true;
            }
            int ledgerCmp = ledgerIndex.compareTo(prevLedger);
            if (ledgerCmp == 1) {
                return true;
            }
            if (ledgerCmp == 0) {
                if (prevTxnIndex == null) {
                    // We don't know, should log a warning or something
                    // Should we keep the first one that we have of this index
                    // or can we assume that the latest is the best?
                    return true;
                }
                if (txnIndex.compareTo(prevTxnIndex) == 1) {
                    // This happened AFTER
                    return true;
                }
            }
            //ledgerCmp == -1  or txnIndex <= previousTxnIndex                                                                          ss
            return false;
        }
    }

    public boolean cache(LedgerEntry le, UInt32 validatedLedgerIndex) {
        Hash256 index = le.ledgerIndex();
        CacheEntry ce = getOrCreate(index);
        ce.upateLedgerEntry(le, validatedLedgerIndex, null);
        return true;
    }

    private CacheEntry getEntry(Hash256 index) {
        return cache.get(index);
    }

    private CacheEntry createEntry(Hash256 index) {
        CacheEntry ce = new CacheEntry();
        cache.put(index, ce);
        return ce;
    }

    public LedgerEntry get(Hash256 index) {
        CacheEntry entry = getEntry(index);
        return entry == null || entry.deleted ? null : entry.le;
    }

    public void updateFromTransactionResult(TransactionResult tr) {
        if (!tr.validated) {
            return;
        }

        TransactionMeta meta = tr.meta;
        UInt32 ledgerIndex = tr.ledgerIndex;
        UInt32 txnIndex = meta.transactionIndex();

        for (AffectedNode an : meta.affectedNodes()) {
            Hash256 index = an.ledgerIndex();
            CacheEntry ce = getOrCreate(index);
            ce.upateLedgerEntry(an.isDeletedNode() ? null : (LedgerEntry) an.nodeAsFinal(),
                                ledgerIndex,
                                txnIndex);
        }
    }

    private CacheEntry getOrCreate(Hash256 index) {
        CacheEntry already = getEntry(index);
        if (already == null) {
            return createEntry(index);
        } else {
            return already;
        }
    }
}
