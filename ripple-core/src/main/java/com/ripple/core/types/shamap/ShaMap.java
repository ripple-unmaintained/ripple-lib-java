package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.Vector256;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.Index;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.sle.LedgerEntry;
import com.ripple.core.types.known.sle.LedgerHashes;
import com.ripple.core.types.known.sle.entries.DirectoryNode;
import com.ripple.core.types.known.tx.result.TransactionResult;

// TODO, consider an AccountState and Transaction map?
public class ShaMap extends ShaMapInner {
    public ShaMap() {
        super(0);
    }
    public DirectoryNode getDirectoryNode(Hash256 rootIndex) {
        return (DirectoryNode) getLedgerEntry(rootIndex);
    }

    public void addTransactionResult(final TransactionResult tr) {
        addLeaf(tr.hash, new TransactionResultLeaf(tr));
    }

    public TransactionResult getTransactionResult(Hash256 index) {
        TransactionResultLeaf leaf = (TransactionResultLeaf) getLeaf(index);
        if (leaf == null) {
            return null;
        }
        return leaf.result;
    }

    public LedgerEntry getLedgerEntry(Hash256 index) {
        LedgerEntryLeaf leaf = (LedgerEntryLeaf) getLeaf(index);
        if (leaf == null) {
            return null;
        }
        return leaf.le;
    }

    public void updateSkipLists(long currentIndex, Hash256 parentHash) {
        long prev = currentIndex - 1;

        if ((prev & 0xFF) == 0) {
            Hash256 skipIndex = Index.ledgerHashes(prev);
            LedgerHashes skip = createOrUpdateSkipList(this, skipIndex);
            Vector256 hashes = skip.hashes();
            assert hashes.size() <= 256;
            hashes.add(parentHash);
            skip.put(UInt32.LastLedgerSequence, new UInt32(prev));
        }

        Hash256 skipIndex = Index.ledgerHashes();
        LedgerHashes skip = createOrUpdateSkipList(this, skipIndex);
        Vector256 hashes = skip.hashes();

        if (hashes.size() > 256) throw new AssertionError();
        if (hashes.size() == 256) {
            hashes.remove(0);
        }

        hashes.add(parentHash);
        skip.put(UInt32.LastLedgerSequence, new UInt32(prev));
    }

    public static LedgerHashes createOrUpdateSkipList(ShaMap state, Hash256 skipIndex) {
        LedgerHashes skip = (LedgerHashes) state.getLedgerEntry(skipIndex);

        if (skip == null) {
            skip = newSkipList(skipIndex);
            state.addLE(skip);
        } else {
            state.getLeafForUpdating(skipIndex);
        }
        return skip;
    }

    private static LedgerHashes newSkipList(Hash256 skipIndex) {
        LedgerHashes skip;
        skip = new LedgerHashes();
        skip.put(UInt32.Flags, new UInt32(0));
        skip.hashes(new Vector256());
        skip.index(skipIndex);
        return skip;
    }

}
