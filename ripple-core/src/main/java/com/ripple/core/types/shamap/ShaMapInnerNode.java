package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.Hash256;

import java.util.TreeMap;

public class ShaMapInnerNode extends ShaMapNode {
    public static final Hash256 ZERO_256 = new Hash256(new byte[32]);
    public ShaMapNode[] branches;
    private int slotBits = 0;
    int depth;

    ShaMapInnerNode parentNode;
    public Hash256 hash;
//    public Set<Hash256> hashes = new TreeSet<Hash256>();
    public TreeMap<Hash256, Integer> hashes2 = new TreeMap<Hash256, Integer>();


    public static class Counter<KeyType> extends TreeMap<KeyType, Integer> {
        public void count(KeyType value) {
            Integer existing = get(value);
            if (existing == null) existing = 1;
            put(value, existing  + 1);
        }
    }

    public Counter<Integer> slotHistogram(Counter<Integer> counter) {
        if (counter == null) {
            counter = new Counter<Integer>();
        }
        for (Integer numberUsedSlots : hashes2.values()) {
            counter.count(numberUsedSlots);
        }

        for (ShaMapNode branch : branches) {
            if (branch instanceof ShaMapInnerNode) {
                ShaMapInnerNode inner = (ShaMapInnerNode) branch;
                inner.slotHistogram(counter);
            }
        }

        return counter;
    }

    public long unusedBranchesOverTime() {
        long total = 0;
        for (Integer used : hashes2.values()) {
            total += (16 - used);
        }

        for (ShaMapNode branch : branches) {
            if (branch instanceof ShaMapInnerNode) {
                ShaMapInnerNode inner = (ShaMapInnerNode) branch;
                total += inner.unusedBranchesOverTime();
            }
        }
        return total;
    }


    public long totalInnerNodes() {
        long total = 1;

        for (ShaMapNode branch : branches) {
            if (branch instanceof ShaMapInnerNode) {
                ShaMapInnerNode inner = (ShaMapInnerNode) branch;
                total += inner.totalInnerNodes();
            }
        }
        return total;
    }




    public long unusedSlots() {
        long unused = 0;

        for (ShaMapNode branch : branches) {
            if (branch == null) {
                unused += 1;
            } else if (branch instanceof ShaMapInnerNode) {
                ShaMapInnerNode inner = (ShaMapInnerNode) branch;
                unused += inner.unusedSlots();
            }
        }
        return unused;
    }

    public long totalSlots() {
        long total = 16;

        for (ShaMapNode branch : branches) {
            if (branch instanceof ShaMapInnerNode) {
                ShaMapInnerNode inner = (ShaMapInnerNode) branch;
                total += inner.totalSlots();
            }
        }
        return total;
    }

    public long totalUniqueInnerNodesOverTime() {
        long total = hashes2.size();

        for (ShaMapNode branch : branches) {
            if (branch instanceof ShaMapInnerNode) {
                ShaMapInnerNode inner = (ShaMapInnerNode) branch;
                total += inner.totalUniqueInnerNodesOverTime();
            }
        }
        return total;
    }

    protected ShaMapInnerNode(int node_depth) {
        branches = new ShaMapNode[16];
        type = NodeType.tnINNER;
        depth = node_depth;
    }

    public Hash256 hash() {
        if (!needsHashing()) {
            return hash;
        }

        if (empty()) {
            return ZERO_256;
        }

        Hash256.HalfSha512 hasher = new Hash256.HalfSha512();
        hasher.update(Hash256.HASH_PREFIX_INNER_NODE);

        int fullBranches = 0;
        for (ShaMapNode node : branches) {
            if (node != null) {
                Hash256 hash = node.hash();
                hasher.update(hash);
                fullBranches++;
            } else {
                hasher.update(ZERO_256);
            }
        }

        Hash256 finish = hasher.finish();
        hash = finish;
//        hashes.add(finish);
        hashes2.put(hash, fullBranches);
        return finish;
    }

    private boolean needsHashing() {
        if (hash == null) {
            return true;
        }

        for (ShaMapNode branch : branches) {
            if (branch instanceof ShaMapInnerNode) {
                ShaMapInnerNode b = (ShaMapInnerNode) branch;
                if (b.needsHashing()) {
                    return true;
                }
            }
        }

        return false;
    }

    protected void setNode(int slot, ShaMapNode node) {
        slotBits = slotBits | (1 << slot);
        branches[slot] = node;
    }

    private void invalidate() {
        hash = null;
//        if (hash != null) {
//            hash = null;
//            ShaMapInnerNode node = this;
//            while ((node = node.parentNode) != null) {
//                if (node.hash == null) {
//                    break;
//                }
//                node.hash = null;
//            }
//        }
    }

    @SuppressWarnings("unused")
    protected void removeNode(int slot) {
        branches[slot] = null;
        slotBits = slotBits & ~(1 << slot);
    }

    public boolean empty() {
        return slotBits == 0;
    }

    private ShaMapLeafNode getLeaf(Hash256 id, boolean invalidating) {
        int ix = id.nibblet(depth);
        ShaMapNode existing = branches[ix];
        if (invalidating) {
            invalidate();
        }
        if (existing == null) {
            return null;
        }
        else if (existing instanceof ShaMapLeafNode) {
            return (ShaMapLeafNode) existing;
        } else {
            return ((ShaMapInnerNode) existing).getLeaf(id, invalidating);
        }
    }

    public ShaMapLeafNode getLeafForUpdating(Hash256 id) {
        return getLeaf(id, true);
    }

    public ShaMapLeafNode getLeafFor(Hash256 id) {
        return getLeaf(id, false);
    }

    // A new type of node ...
    // A compressed inner ... probably needs to store depth, and an index ;)

//    public void compressTree() {
//        int slot = 0;
//        for (ShaMapNode branch : branches) {
//            if (branch instanceof ShaMapInnerNode) {
//                ShaMapInnerNode inner = (ShaMapInnerNode) branch;
//                int i = inner.justOneInner();
//                if (i != -1) {
//                    ShaMapInnerNode branch1 = (ShaMapInnerNode) inner.branches[i];
//                    setNode(slot, branch1);
//                    branch1.compressTree();
//                } else {
//                    inner.compressTree();
//                }
//            }
//            slot++;
//        }
//    }
//
//    public void uncompressTree() {
//        int slot = 0;
//        for (ShaMapNode branch : branches) {
//            if (branch instanceof ShaMapInnerNode) {
//                ShaMapInnerNode inner = (ShaMapInnerNode) branch;
//                if (inner.depth != depth + 1) {
//                    ShaMapInnerNode reslotted = null;
//                    for (int i = depth + 1; i < inner.depth; i++) {
//                        reslotted = new ShaMapInnerNode(i);
//                        setNode(slot, reslotted);
//                    }
//                    if (reslotted != null) {
//                        reslotted.setNode(slot,inner);
//                    }   else {
//                        throw new IllegalStateException("wtf ... ???");
//                    }
//                }
//            }
//            slot++;
//        }
//    }
//    private int justOneInner() {
//        int others = 0;
//        int inners = 0;
//        int slot = 0;
//        for (ShaMapNode branch : branches) {
//            if (branch instanceof ShaMapInnerNode) {
//                inners ++;
//            } else if (branch instanceof ShaMapLeafNode) {
//                others ++;
//            }
//            if (others > 0 || inners > 1) {
//                return -1;
//            }
//            slot++;
//        }
//        return slot;
//    }

    private void addLeaf(Hash256 id, NodeType nodeType, ShaMapLeafNode.Item blob, ShaMapLeafNode moved) {
        int ix = id.nibblet(depth);
        ShaMapNode existing = branches[ix];
        invalidate();

        if (existing == null) {
            ShaMapLeafNode node;
            if (moved == null) {
                node = new ShaMapLeafNode(id, nodeType, blob);
            } else {
                node = moved;
            }
            setNode(ix, node);
        } else if (existing instanceof ShaMapLeafNode) {
            ShaMapLeafNode existingLeaf = (ShaMapLeafNode) existing;
            if (existingLeaf.index.equals(id)) {
//                System.out.println(depth);
                throw new UnsupportedOperationException("Tried to add node already in tree!");
            } else {
                ShaMapInnerNode container = new ShaMapInnerNode(depth + 1);
                container.addLeaf(existingLeaf.index, existingLeaf);
                container.addLeaf(id, nodeType, blob);
                setNode(ix, container);
            }
        } else {
            ShaMapInnerNode existing1 = (ShaMapInnerNode) existing;
            existing1.addLeaf(id, nodeType, blob);
        }
    }
    private void addLeaf(Hash256 id, ShaMapLeafNode existingLeaf) {
        addLeaf(id, null, null, existingLeaf);
    }
    public void addLeaf(Hash256 id, NodeType nodeType, ShaMapLeafNode.Item blob) {
        addLeaf(id, nodeType, blob, null);
    }
}
