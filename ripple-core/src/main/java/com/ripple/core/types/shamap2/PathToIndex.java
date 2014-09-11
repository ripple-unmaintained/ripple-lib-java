package com.ripple.core.types.shamap2;

import com.ripple.core.coretypes.hash.Hash256;

import java.util.ArrayDeque;
import java.util.Iterator;

public class PathToIndex {
    public  Hash256 index;
    public ShaMapLeaf leaf = null;

    private ArrayDeque<ShaMapInner> inners;
    private ShaMapInner[] dirtied;
    private boolean matched = false;

    public boolean hasLeaf() {
        return leaf != null;
    }
    public boolean leafMatchedIndex() {
        return matched;
    }

    // returns the
    public ShaMapInner dirtyOrCopyInners() {
        if (maybeCopyOnWrite()) {
            int ix = 0;
            // We want to make a uniformly accessed array of the inners
            dirtied = new ShaMapInner[inners.size()];
            // from depth 0 to 1, to 2, to 3, don't be fooled by the api
            Iterator<ShaMapInner> it = inners.descendingIterator();

            // This is actually the root which COULD be the top of the stack
            // Think about it ;)
            ShaMapInner top = it.next();
            dirtied[ix++] = top;
            top.invalidate();
            // we set this to true, but we may
            boolean doCopies = true;

            while (it.hasNext()) {
                ShaMapInner next = it.next();
                if (ix == 1) {
                    doCopies = next.version != top.version;
                }

                if (doCopies) {
                    ShaMapInner copy = next.copy(top.version);
                    copy.invalidate();
                    top.setBranch(index, copy);
                    next = copy;
                } else {
                    next.invalidate();
                }
                top = next;
                dirtied[ix++] = top;
            }
            return top;
        } else {
            copyInnersToDirtiedArray();
            return inners.peekFirst();
        }
    }

    public boolean hasMatchedLeaf() {
        return hasLeaf() && leafMatchedIndex();
    }

    public void collapseSingleLeafInners() {
        assert dirtied != null;

        ShaMapInner next;
        ShaMapLeaf singleLeaf = null;

        for (int i = dirtied.length - 1; i >= 0; i--) {
            next = dirtied[i];
            if (singleLeaf != null) {
                next.setBranch(singleLeaf.index, singleLeaf);
            }
            singleLeaf = next.singleLeaf();
            if (singleLeaf == null) {
                break;
            }
        }
    }

    // So can be used by makeValid etc
    private void copyInnersToDirtiedArray() {
        int ix = 0;
        dirtied = new ShaMapInner[inners.size()];
        Iterator<ShaMapInner> descending = inners.descendingIterator();
        while (descending.hasNext()) {
            dirtied[ix++] = descending.next();
        }
    }

    private boolean maybeCopyOnWrite() {
        return inners.peekLast().doCoW;
    }

    public PathToIndex(ShaMapInner root, Hash256 index) {
        this.index = index;
        inners = makeStack(root, index);
    }

    private ArrayDeque<ShaMapInner> makeStack(ShaMapInner root, Hash256 index) {
        ArrayDeque<ShaMapInner> inners = new ArrayDeque<ShaMapInner>();
        ShaMapInner top = root;

        while (true) {
            inners.push(top);
            ShaMapNode existing = top.getBranch(index);
            if (existing == null) {
                break;
            } else if (existing.isLeaf()) {
                leaf = existing.asLeaf();
                matched = leaf.index.equals(index);
                break;
            }
            else if (existing.isInner()) {
                top = existing.asInner();
            }
        }
        return inners;
    }
}
