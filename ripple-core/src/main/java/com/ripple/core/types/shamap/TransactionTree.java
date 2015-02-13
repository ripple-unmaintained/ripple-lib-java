package com.ripple.core.types.shamap;

import com.ripple.core.types.known.tx.result.TransactionResult;

public class TransactionTree extends ShaMap {
    public TransactionTree() {
        super();
    }

    public TransactionTree(boolean isCopy, int depth) {
        super(isCopy, depth);
    }

    @Override
    protected ShaMapInner makeInnerOfSameClass(int depth) {
        return new TransactionTree(true, depth);
    }

    public void addTransactionResult(TransactionResult tr) {
        TransactionResultItem item = new TransactionResultItem(tr);
        addItem(tr.hash, item);
    }

    @Override
    public TransactionTree copy() {
        return (TransactionTree) super.copy();
    }

    public void walkTransactions(final TransactionResultVisitor walker) {
        walkLeaves(new LeafWalker() {
            @Override
            public void onLeaf(ShaMapLeaf leaf) {
                TransactionResultItem item = (TransactionResultItem) leaf.item;
                walker.onTransaction(item.result);
            }
        });
    }
}
