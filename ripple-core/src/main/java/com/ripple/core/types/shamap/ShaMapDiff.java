package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.Hash256;

import java.util.TreeSet;

public class ShaMapDiff {
    public ShaMap one, two;

    public TreeSet<Hash256> modified = new TreeSet<Hash256>();
    public TreeSet<Hash256> deleted = new TreeSet<Hash256>();
    public TreeSet<Hash256> added = new TreeSet<Hash256>();

    public ShaMapDiff(ShaMap one, ShaMap two) {
        this.one = one;
        this.two = two;
    }

    // Find what's added, modified and deleted in `two`
    public void find() {
        one.hash();
        two.hash();
        compare(one, two);
    }

    public ShaMapDiff inverted() {
        ShaMapDiff shaMapDiff = new ShaMapDiff(two, one);

        shaMapDiff.added = deleted;
        shaMapDiff.modified = modified;
        shaMapDiff.deleted = added;

        return shaMapDiff;
    }

    public void apply(ShaMap sa) {
        for (Hash256 mod : modified) {
            boolean modded = sa.updateItem(mod, two.getItem(mod).copy());
            if (!modded) throw new AssertionError();
        }

        for (Hash256 add : added) {
            boolean added = sa.addItem(add, two.getItem(add).copy());
            if (!added) throw new AssertionError();
        }
        for (Hash256 delete : deleted) {
            boolean removed = sa.removeLeaf(delete);
            if (!removed) throw new AssertionError();
        }
    }
    private void compare(ShaMapInner a, ShaMapInner b) {
        for (int i = 0; i < 16; i++) {
            ShaMapNode aChild = a.getBranch(i);
            ShaMapNode bChild = b.getBranch(i);

            if (aChild == null && bChild != null) {
                trackAdded(bChild);
                // added in B
            } else if (aChild != null && bChild == null) {
                trackRemoved(aChild);
                // removed from B
            } else if (aChild != null && !aChild.hash().equals(bChild.hash())) {
                boolean aleaf  = aChild.isLeaf(),
                        bLeaf  = bChild.isLeaf();

                if (aleaf && bLeaf) {
                    ShaMapLeaf la = (ShaMapLeaf) aChild;
                    ShaMapLeaf lb = (ShaMapLeaf) bChild;
                    if (la.index.equals(lb.index)) {
                        modified.add(la.index);
                    } else {
                        deleted.add(la.index);
                        added.add(lb.index);
                    }
                } else if (aleaf /*&& bInner*/) {
                    ShaMapLeaf la = (ShaMapLeaf) aChild;
                    ShaMapInner ib = (ShaMapInner) bChild;
                    trackAdded(ib);

                    if (ib.hasLeaf(la.index)) {
                        // because trackAdded would have added it
                        added.remove(la.index);
                        ShaMapLeaf leaf = ib.getLeaf(la.index);
                        if (!leaf.hash().equals(la.hash())) {
                            modified.add(la.index);
                        }
                    } else {
                        deleted.add(la.index);
                    }
                } else if (bLeaf /*&& aInner*/) {
                    ShaMapLeaf lb = (ShaMapLeaf) bChild;
                    ShaMapInner ia = (ShaMapInner) aChild;
                    trackRemoved(ia);

                    if (ia.hasLeaf(lb.index)) {
                        // because trackRemoved would have deleted it
                        deleted.remove(lb.index);
                        ShaMapLeaf leaf = ia.getLeaf(lb.index);
                        if (!leaf.hash().equals(lb.hash())) {
                            modified.add(lb.index);
                        }
                    } else {
                        added.add(lb.index);
                    }
                } else /*if (aInner && bInner)*/ {
                    compare((ShaMapInner) aChild, (ShaMapInner) bChild);
                }
            }
        }
    }
    private void trackRemoved(ShaMapNode child) {
        child.walkAnyLeaves(new LeafWalker() {
            @Override
            public void onLeaf(ShaMapLeaf leaf) {
                deleted.add(leaf.index);
            }
        });
    }
    private void trackAdded(ShaMapNode child) {
        child.walkAnyLeaves(new LeafWalker() {
            @Override
            public void onLeaf(ShaMapLeaf leaf) {
                added.add(leaf.index);
            }
        });
    }
}
