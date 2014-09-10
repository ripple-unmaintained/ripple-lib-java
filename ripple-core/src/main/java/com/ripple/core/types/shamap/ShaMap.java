package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.Vector256;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.Index;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.sle.LedgerEntry;
import com.ripple.core.types.known.sle.LedgerHashes;
import com.ripple.core.types.known.tx.result.TransactionResult;

import java.util.concurrent.atomic.AtomicInteger;

public class ShaMap extends ShaMapInner {
    private AtomicInteger copies;

    public ShaMap() {
        super(0);
        // This way we can copy the first to the second,
        // copy the second, then copy the first again ;)
        copies = new AtomicInteger();
    }
    public ShaMap(boolean isCopy, int depth) {
        super(isCopy, depth, 0);
    }

    @Override
    protected ShaMapInner copyInner(int depth) {
        return new ShaMap(true, depth);
    }

    public ShaMap copy() {
        version = copies.incrementAndGet();
        ShaMap copy = (ShaMap) copy(copies.incrementAndGet());
        copy.copies = copies;
        return copy;
    }

    public void addLE(LedgerEntry entry) {
        LedgerEntryItem item = new LedgerEntryItem(entry);
        addItem(entry.index(), item);
    }

    public void updateSkipLists(long currentIndex, Hash256 parentHash) {
        long prev = currentIndex - 1;

        if ((prev & 0xFF) == 0) {
            Hash256 skipIndex = Index.ledgerHashes(prev);
            LedgerHashes skip = createOrUpdateSkipList(skipIndex);
            Vector256 hashes = skip.hashes();
            assert hashes.size() <= 256;
            hashes.add(parentHash);
            skip.put(UInt32.LastLedgerSequence, new UInt32(prev));
        }

        Hash256 skipIndex = Index.ledgerHashes();
        LedgerHashes skip = createOrUpdateSkipList(skipIndex);
        Vector256 hashes = skip.hashes();

        if (hashes.size() > 256) throw new AssertionError();
        if (hashes.size() == 256) {
            hashes.remove(0);
        }

        hashes.add(parentHash);
        skip.put(UInt32.LastLedgerSequence, new UInt32(prev));
    }

    private LedgerHashes createOrUpdateSkipList(Hash256 skipIndex) {
        ShaMap state = this;

        PathToIndex path = state.pathToIndex(skipIndex);
        ShaMapInner top = path.dirtyOrCopyInners();
        LedgerEntryItem item;

        if (path.hasMatchedLeaf()) {
            item = (LedgerEntryItem) path.leaf.item;
        } else {
            LedgerHashes hashes = newSkipList(skipIndex);
            item = new LedgerEntryItem(hashes);
            top.addLeafToTerminalInner(new ShaMapLeaf(skipIndex, item));
        }
        return (LedgerHashes) item.entry;
    }

    private static LedgerHashes newSkipList(Hash256 skipIndex) {
        LedgerHashes skip;
        skip = new LedgerHashes();
        skip.put(UInt32.Flags, new UInt32(0));
        skip.hashes(new Vector256());
        skip.index(skipIndex);
        return skip;
    }

    public void addTransactionResult(TransactionResult tr) {
        TransactionResultItem item = new TransactionResultItem(tr);
        addItem(tr.hash, item);
    }
}
