package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.HalfSha512;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.HashPrefix;
import com.ripple.core.types.known.sle.LedgerEntry;

import java.util.ArrayDeque;

public class ShaMapInner extends ShaMapNode {
    final public ShaMapNode[] branches;
    protected int slotBits = 0;
    public int depth;
    public Hash256 hash;

    protected ShaMapInner(int node_depth) {
        branches = new ShaMapNode[16];
        type = NodeType.tnINNER;
        hashingPrefix = HashPrefix.innerNode;
        depth = node_depth;
    }

    public void walkTree(TreeWalker walker) {
        walker.onInner(hash(), this);

        for (ShaMapNode branch : branches) {
            if (branch == null) {
                continue;
            }
            if (branch instanceof ShaMapLeaf) {
                ShaMapLeaf ln = (ShaMapLeaf) branch;
                walker.onLeaf(branch.hash(), ln);
            } else {
                ShaMapInner childInner = (ShaMapInner) branch;
                childInner.walkTree(walker);
            }
        }
    }

    public Hash256 hash() {
        if (!needsHashing()) {
            return hash;
        }

        if (empty()) {
            return Hash256.ZERO_256;
        }

        HalfSha512 hasher = new HalfSha512();
        hasher.update(hashingPrefix);

        int fullBranches = 0;
        for (ShaMapNode node : branches) {
            if (node != null) {
                Hash256 hash = node.hash();
                hasher.update(hash);
                fullBranches++;
            } else {
                hasher.update(Hash256.ZERO_256);
            }
        }

        hash = hasher.finish();
        onHash(hash, fullBranches);
        return hash;
    }

    public void onHash(Hash256 hash, int fullBranches) {

    }

    public boolean needsHashing() {
        return hash == null;
    }

    public void invalidate() {
        hash = null;
    }

    protected void setNode(int slot, ShaMapNode node) {
        slotBits = slotBits | (1 << slot);
        branches[slot] = node;
    }

    @SuppressWarnings("unused")
    public void removeNode(int slot) {
        branches[slot] = null;
        slotBits = slotBits & ~(1 << slot);
    }

    public boolean empty() {
        return slotBits == 0;
    }

    public boolean removeLeaf(Hash256 id) {
        if (getBranch(selectBranch(id)) == null) {
            return false;
        }

        ArrayDeque<ShaMapNode> shaMapNodes = walkStackTo(id, true);
        ShaMapLeaf terminal = (ShaMapLeaf) shaMapNodes.pop();

        if (terminal.index.equals(id)) {
            // the whole path needs invalidating
            for (ShaMapNode shaMapNode : shaMapNodes) {
                ShaMapInner inner = (ShaMapInner) shaMapNode;
                inner.invalidate();
            }

//            System.out.println(terminal.index);
            ShaMapInner parent = (ShaMapInner) shaMapNodes.pop();
            // remove that leaf

            int leafSlot = parent.selectBranch(id);
            assert parent.getBranch(leafSlot) == terminal;
            parent.removeNode(leafSlot);

            while (!shaMapNodes.isEmpty()) {
                ShaMapInner gp = (ShaMapInner) shaMapNodes.peekFirst();

                if (parent.hasOneChild()) {
                    ShaMapNode branch = parent.getBranch(parent.singleChildIx());

                    if (branch instanceof ShaMapLeaf) {
                       ShaMapLeaf leaf = (ShaMapLeaf) branch;
                        int slot = gp.selectBranch(id);
                        if (gp.getBranch(slot) != parent) throw new AssertionError();

                       gp.removeNode(slot);
                       gp.addLeaf(leaf.index, leaf);
                        if (gp.getBranch(slot) != leaf) throw new AssertionError();
                    } else {
                        break;
                    }
                } else if (parent.empty()) {
                    break;
                }

                parent = (ShaMapInner) shaMapNodes.pop();
            }
            return true;
        } else {
            return false;

        }
    }

    private boolean hasOneInner() {
        int inners = 0;
        for (ShaMapNode branch : branches) {
            if (branch instanceof ShaMapInner) {
                if (++inners > 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean hasOneChild() {
        int children = 0;
        for (ShaMapNode branch : branches) {
            if (branch != null) {
                if (++children > 1) {
                    return false;
                }
            }
        }
        return children != 0;
    }

    public int singleChildIx() {
        int children = 0;
        int ix = 0;
        int i = 0;
        for (ShaMapNode branch : branches) {
            if (branch != null) {
                if (++children > 1) {
                    return -1;
                } else {
                    ix = i;
                }
            }
            i++;
        }
        return children == 0 ? -1 : ix;
    }


    public ShaMapLeaf getFirstLeaf(Hash256 id, boolean invalidating) {
        int ix = id.nibblet(depth);
        ShaMapNode existing = branches[ix];
        if (invalidating) {
            invalidate();
        }
        if (existing == null) {
            return null;
        }
        else if (existing instanceof ShaMapLeaf) {
            return (ShaMapLeaf) existing;
        } else {
            return ((ShaMapInner) existing).getFirstLeaf(id, invalidating);
        }
    }

    public ShaMapLeaf getLeafForUpdating(Hash256 id) {
        ShaMapLeaf leaf = getFirstLeaf(id, true);
        // TODO
        if (leaf == null) {
            return null;
        }
        if (!leaf.index.equals(id)) {
            throw new IllegalStateException("Invalidated the tree, and the leaf wasn't there. TODO: better API");
        }
        return leaf;
    }

    public ShaMapLeaf getLeaf(Hash256 id) {
        ShaMapLeaf leaf = getFirstLeaf(id, false);
        if (leaf != null && !leaf.index.equals(id)) {
            // This is not the leaf we requested ;)
            leaf = null;
        }
        return leaf;
    }

    public void addLeaf(Hash256 index, ShaMapLeaf leaf) {
        int ix = index.nibblet(depth);
        ShaMapNode existing = branches[ix];
        invalidate();

        if (existing == null) {
            setNode(ix, leaf);
        } else if (existing instanceof ShaMapLeaf) {
            ShaMapLeaf existingLeaf = (ShaMapLeaf) existing;
            if (existingLeaf.index.equals(index)) {
                throw new UnsupportedOperationException("Tried to add node already in tree!");
            } else {
                ShaMapInner container = makeInnerChild();
                container.addLeaf(existingLeaf.index, existingLeaf);
                container.addLeaf(index, leaf);
                setNode(ix, container);
            }
        } else {
            ShaMapInner existingInner = (ShaMapInner) existing;
            existingInner.addLeaf(index, leaf);
        }
    }

    protected ShaMapInner makeInnerChild() {
        return new ShaMapInner(depth + 1);
    }

    public void addItem(Hash256 index, NodeType nodeType, ShaMapLeaf.Item blob) {
        addLeaf(index, new ShaMapLeaf(index, nodeType, blob));
    }

    public int selectBranch(Hash256 id) {
        return id.nibblet(depth);
    }
    public boolean branchIsEmpty(int i) {
        return (branches[i] == null);
    }
    public ShaMapNode getBranch(int i) {
        return branches[i];
    }

    public ArrayDeque<ShaMapNode> walkStackTo(Hash256 id, boolean includeNonMatchingLeaf) {
        ArrayDeque<ShaMapNode> stack = new ArrayDeque<ShaMapNode>();
        ShaMapInner top = this;

        while (true) {
            stack.push(top);
            int ix = top.selectBranch(id);
            ShaMapNode existing = top.getBranch(ix);
            if (existing == null) {
                break;
            } else if (existing instanceof ShaMapLeaf) {
                ShaMapLeaf leaf = (ShaMapLeaf) existing;
                if (includeNonMatchingLeaf || leaf.index.equals(id)) {
                    stack.push(existing);
                }
                break;
            }
            else if (existing instanceof ShaMapInner) {
                top = (ShaMapInner) existing;
            }
        }
        return stack;
    }

    /**
     * @param id index returned is relative to this
     * @param end the largest index
     * @return An index where id < index <= end if found, or null
     */
    public Hash256 getNextIndex(Hash256 id, Hash256 end) {
        Hash256 nextIndex = getNextIndex(id);

        if (nextIndex != null) {
            if (nextIndex.compareTo(id) != 1) throw new AssertionError();
        }

        if (nextIndex != null && nextIndex.compareTo(end) > -1) {
            nextIndex = null;
        }
        return nextIndex;
    }

    public Hash256 getNextIndex(Hash256 id) {
        ArrayDeque<ShaMapNode> stack = walkStackTo(id, true);

        while (stack.size() > 0) {
            ShaMapNode node = stack.pop();
            if (node instanceof ShaMapLeaf) {
                ShaMapLeaf leaf = (ShaMapLeaf) node;
                if (leaf.index.compareTo(id) == 1) {
                    return leaf.index;
                }
            }
            else if (node instanceof ShaMapInner) {
                ShaMapInner inner = (ShaMapInner) node;
                for (int ix = inner.selectBranch(id) + 1; ix < 16; ix++) {
                    if (!inner.branchIsEmpty(ix)) {
                        ShaMapLeaf ret = inner.getBranch(ix).firstLeafBelow();
                        if (ret != null) {
                            return ret.index;
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean hasLeaf(Hash256 hash) {
        return getLeaf(hash) != null;
    }

    public void walkItems(LeafWalker leafWalker) {
        for (ShaMapNode branch : branches) {
            if (branch instanceof ShaMapLeaf) {
                leafWalker.onLeaf((ShaMapLeaf) branch);
            } else if (branch instanceof ShaMapInner) {
                ((ShaMapInner) branch).walkItems(leafWalker);
            }
        }
    }

    public Hash256 childHash(int i) {
        ShaMapNode branch = getBranch(i);
        if (branch == null) {
            return Hash256.ZERO_256;
        } else {
            return branch.hash();
        }
    }

    public void addLE(LedgerEntry le) {
        addLeaf(le.index(), new LedgerEntryLeaf(le));
    }

    public void removeBranch(int i) {
        invalidate();
        removeNode(i);
    }

    public void setBranch(int i, ShaMapNode node) {
        invalidate();
        setNode(i, node);
    }
}
