package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.Vector256;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.Index;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.sle.LedgerEntry;
import com.ripple.core.types.known.sle.LedgerHashes;
import com.ripple.core.types.known.sle.entries.DirectoryNode;

public class AccountState extends ShaMap {
    public AccountState() {
        super();
    }
    public AccountState(boolean isCopy, int depth) {
        super(isCopy, depth);
    }

    @Override
    protected ShaMapInner makeInnerOfSameClass(int depth) {
        return new AccountState(true, depth);
    }

    private static LedgerHashes newSkipList(Hash256 skipIndex) {
        LedgerHashes skip;
        skip = new LedgerHashes();
        skip.put(UInt32.Flags, new UInt32(0));
        skip.hashes(new Vector256());
        skip.index(skipIndex);
        return skip;
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
        PathToIndex path = pathToIndex(skipIndex);
        ShaMapInner top = path.dirtyOrCopyInners();
        LedgerEntryItem item;

        if (path.hasMatchedLeaf()) {
            ShaMapLeaf leaf = path.invalidatedPossiblyCopiedLeafForUpdating();
            item = (LedgerEntryItem) leaf.item;
        } else {
            item = new LedgerEntryItem(newSkipList(skipIndex));
            top.addLeafToTerminalInner(new ShaMapLeaf(skipIndex, item));
        }
        return (LedgerHashes) item.entry;
    }

    public void addLE(LedgerEntry entry) {
        LedgerEntryItem item = new LedgerEntryItem(entry);
        addItem(entry.index(), item);
    }

    public LedgerEntry getLE(Hash256 index) {
        LedgerEntryItem item = (LedgerEntryItem) getItem(index);
        return item == null ? null : item.entry;
    }

    public DirectoryNode getDirectoryNode(Hash256 index) {
        return (DirectoryNode) getLE(index);
    }

    public Hash256 getNextIndex(Hash256 nextIndex, Hash256 bookEnd) {
        return null;
    }

    @Override
    public AccountState copy() {
        return (AccountState) super.copy();
    }
}
