package com.ripple.core.types.shamap;

import com.ripple.core.binary.STWriter;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.serialized.BytesSink;

import java.util.TreeSet;


public class ShamapDiff {
    ShaMap one, two;

    public TreeSet<Hash256> modified = new TreeSet<Hash256>();
    public TreeSet<Hash256> deleted = new TreeSet<Hash256>();
    public TreeSet<Hash256> added = new TreeSet<Hash256>();

    public ShamapDiff(ShaMap one, ShaMap two) {
        this.one = one;
        this.two = two;
    }

    public void find() {
        compare(one, two);
    }

    public void apply(ShaMap sa) {
        for (Hash256 mod : modified) {
            ShaMapLeaf leafForUpdating = sa.getLeafForUpdating(mod);
            leafForUpdating.copyItemFrom(two.getLeaf(mod));
        }

        for (Hash256 add : added) {
            sa.addLeaf(add, two.getLeaf(add));
        }
        for (Hash256 delete : deleted) {
            boolean b = sa.removeLeaf(delete);
            if (!b) throw new AssertionError();
        }
    }

    public void toBytesSink(BytesSink sink) {
        STWriter bw = new STWriter(sink);
        bw.write(new UInt32(added.size()));
        for (Hash256 add : added) {
            bw.write(add);
            bw.writeVl(two.getLedgerEntry(add));
        }
        bw.write(new UInt32(modified.size()));
        for (Hash256 mv : modified) {
            bw.write(mv);
            bw.writeVl(two.getLedgerEntry(mv));
        }
        bw.write(new UInt32(deleted.size()));
        for (Hash256 rm : deleted) {
            bw.write(rm);
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
                boolean aleaf  = (aChild instanceof ShaMapLeaf),
                        bLeaf  = (bChild instanceof ShaMapLeaf);

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
