package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.types.known.sle.LedgerEntry;
import com.ripple.core.types.known.sle.entries.DirectoryNode;
import com.ripple.core.types.known.tx.result.TransactionResult;

// TODO, consider an AccountState and Transaction map?
public class ShaMap extends ShaMapInnerNode {
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

    public void addLE(LedgerEntry le) {
        addLeaf(le.index(), new LedgerEntryLeaf(le));
    }
    public LedgerEntry getLedgerEntry(Hash256 index) {
        LedgerEntryLeaf leaf = (LedgerEntryLeaf) getLeaf(index);
        if (leaf == null) {
            return null;
        }
        return leaf.le;
    }
}
