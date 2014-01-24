package com.ripple.cli;

import com.ripple.config.Config;
import com.ripple.core.coretypes.Vector256;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.types.shamap.ShaMapInnerNode;
import com.ripple.core.types.shamap.ShaMapLeafNode;
import com.ripple.core.types.shamap.ShaMapNode;
import com.ripple.encodings.common.B16;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

class InstrumentedInnerNode extends ShaMapInnerNode {
    static {
        Config.initBouncy();
    }

    public TreeMap<Hash256, Integer> hashes = new TreeMap<Hash256, Integer>();
    protected InstrumentedInnerNode(int node_depth) {
        super(node_depth);
    }

    @Override
    public ShaMapInnerNode makeInnerChild() {
        return new InstrumentedInnerNode(depth + 1);
    }
    @Override
    public void onHash(Hash256 hash, int fullBranches) {
        hashes.put(hash, fullBranches);
    }

    public Counter<Integer> slotHistogramOverTime(Counter<Integer> counter) {
        if (counter == null) {
            counter = new Counter<Integer>();
        }
        for (Integer numberUsedSlots : hashes.values()) {
            counter.count(numberUsedSlots);
        }

        for (ShaMapNode branch : branches) {
            if (branch instanceof InstrumentedInnerNode) {
                InstrumentedInnerNode inner = (InstrumentedInnerNode) branch;
                inner.slotHistogramOverTime(counter);
            }
        }

        return counter;
    }

    public long unusedBranchesOverTime() {
        long total = 0;
        for (Integer used : hashes.values()) {
            total += (16 - used);
        }

        for (ShaMapNode branch : branches) {
            if (branch instanceof InstrumentedInnerNode) {
                InstrumentedInnerNode inner = (InstrumentedInnerNode) branch;
                total += inner.unusedBranchesOverTime();
            }
        }
        return total;
    }

    public long totalInnerNodes() {
        long total = 1;

        for (ShaMapNode branch : branches) {
            if (branch instanceof InstrumentedInnerNode) {
                InstrumentedInnerNode inner = (InstrumentedInnerNode) branch;
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
            } else if (branch instanceof InstrumentedInnerNode) {
                InstrumentedInnerNode inner = (InstrumentedInnerNode) branch;
                unused += inner.unusedSlots();
            }
        }
        return unused;
    }

    public long totalSlots() {
        long total = 16;

        for (ShaMapNode branch : branches) {
            if (branch instanceof InstrumentedInnerNode) {
                InstrumentedInnerNode inner = (InstrumentedInnerNode) branch;
                total += inner.totalSlots();
            }
        }
        return total;
    }

    public long totalUniqueInnerNodesOverTime() {
        long total = hashes.size();

        for (ShaMapNode branch : branches) {
            if (branch instanceof InstrumentedInnerNode) {
                InstrumentedInnerNode inner = (InstrumentedInnerNode) branch;
                total += inner.totalUniqueInnerNodesOverTime();
            }
        }
        return total;
    }

    public static class Counter<KeyType> extends TreeMap<KeyType, Integer> {
        public void count(KeyType value) {
            Integer existing = get(value);
            if (existing == null) existing = 1;
            put(value, existing  + 1);
        }
    }
}
class InstrumentedShaMap extends InstrumentedInnerNode {
    protected InstrumentedShaMap() {
        super(0);
    }
}

public class ShamapInnerNodeAnalysis {
    int NUM_LEDGERS = 10000;
    int NEW_NODES_PER_LEDGER = 50;
    int MODIFIED_NODES_PER_LEDGER = 100;
    // Emulating a book base, which has a 192 bit common prefix
    public int NUM_PREFIXES = 25;
    public boolean CREATE_PREFIXED = false;
    public int PREFIXED_EVERY = 5;

    Random randomer = new Random();
    public long     hashesCounter = 0;

    ArrayList<byte[]> prefixes;
    InstrumentedInnerNode.Counter<Integer> prefixIndexes;
    {
        setUpBookBasePrefixes();
    }

    private void setUpBookBasePrefixes() {
        prefixes = new ArrayList<byte[]>();

        for (int i = 0; i < NUM_PREFIXES; i++) {
            byte[] b = new byte[24];
            b[0] |= ((i % 16) << 4); // A little trick to ensure we hit every top level inner node children of root
            randomer.nextBytes(b);
            prefixes.add(b);
        }
        prefixIndexes = new InstrumentedInnerNode.Counter<Integer>();
    }

    // Use a book base key
    public boolean bookKey() {
        return CREATE_PREFIXED && ++hashesCounter % PREFIXED_EVERY == 0;
    }

    private byte[] randomPrefix() {
        int randomIndex = randomer.nextInt(prefixes.size() - 1);
        prefixIndexes.count(randomIndex);
        return prefixes.get(randomIndex);
    }

    private Hash256 randomPrefixedHash() {
        byte[] prefix1 = randomPrefix();

        byte[] b;
        b = new byte[8];
        randomer.nextBytes(b);

        byte[] f = new byte[32];

        System.arraycopy(prefix1, 0, f, 0, prefix1.length);
        System.arraycopy(b, 0, f, prefix1.length, b.length);
        b = f;
        return new Hash256(b);
    }

    public void addKeyAsValueItem(InstrumentedShaMap ledger, Hash256 key) {
        ledger.addItem(key, ShaMapNode.NodeType.tnTRANSACTION_MD, createItem(key));
    }


    public double now() {
        return System.nanoTime();
    }

    private ShaMapLeafNode.Item createItem(final Hash256 id1) {
        return new ShaMapLeafNode.Item() {
            @Override
            public byte[] bytes() {
                return id1.bytes();
            }
        };
    }

    private Hash256 randomHash() {
        byte[] b = new byte[32];
        randomer.nextBytes(b);
        return new Hash256(b);
    }

    public void analyseInnerNodeUsage() {
        InstrumentedShaMap ledger = new InstrumentedShaMap();

        // Ledger will blow up if we try to use the same index twice,
        // so we don't need to store these in sets, just a container
        // with quick append
        Vector256 randomNodes = new Vector256();
        Vector256 bookBases = new Vector256();

        int totalLeavesAdded = 0;
        int totalLeavesAddedOrModified = 0;
        int ledgersDone = 0;
        double pta;

        for (int i = 0; i < NUM_LEDGERS; i++) {
            pta = now();
            int n = 0;
            while (n++ < NEW_NODES_PER_LEDGER) {
                Hash256 key;
                if (bookKey()) {
                    key = randomPrefixedHash();
                    bookBases.add(key);
                } else {
                    key = randomHash();
                    randomNodes.add(key);
                }

                addKeyAsValueItem(ledger, key);
                totalLeavesAdded++;
            }
            double nsForAdding = now() - pta;

            pta = now();
            if (ledgersDone > 1 && MODIFIED_NODES_PER_LEDGER <= randomNodes.size()) {
                for (int j = 0; j < MODIFIED_NODES_PER_LEDGER; j++) {
                    ArrayList<Hash256> whichRandom;
                    if (bookKey()) {
                        whichRandom = bookBases;
                    } else {
                        whichRandom = randomNodes;
                    }
                    int randomKeyIndex = randomer.nextInt(whichRandom.size() - 1);
                    Hash256 keyToUpdate = whichRandom.get(randomKeyIndex);
                    // invalidating ;)
                    ShaMapLeafNode leaf = ledger.getLeafForUpdating(keyToUpdate);
                    // so the hash is actually different ;0
                    leaf.setBlob(createItem(randomHash()));
                }
            }
            double nsForModding = now() - pta;

            totalLeavesAddedOrModified += MODIFIED_NODES_PER_LEDGER + NEW_NODES_PER_LEDGER;

            pta = now();
            ledger.hash();
            double nsForHashing = now() - pta;

            ledgersDone++;

            if (ledgersDone % 25 == 0) {
                double uniqueInnerNodes = ledger.totalUniqueInnerNodesOverTime();
                long l1 = ledger.unusedBranchesOverTime();
                double ratioUnusedToUsedInInnerNodesOverTime = l1 / (uniqueInnerNodes * 16);

                long totalInnerNodes = ledger.totalInnerNodes();
                long totalSlots = ledger.totalSlots();
                long unusedSlots = ledger.unusedSlots();
                long usedSlots = totalSlots - ledger.unusedSlots();

                System.out.println("Ledgers passed: " + ledgersDone);
                System.out.println("Total leaf nodes in latest ledger: " + totalLeavesAdded);
                System.out.println("Leaf nodes added per ledger: " + NEW_NODES_PER_LEDGER);
                System.out.println("Leaf nodes updated per ledger: " + MODIFIED_NODES_PER_LEDGER);
                System.out.println("Ms to add " + NEW_NODES_PER_LEDGER + " nodes to ledger: " + nsForAdding / 1e6);
                System.out.println("Ms to update " + MODIFIED_NODES_PER_LEDGER + " nodes to ledger: " + nsForModding / 1e6);
                System.out.println("Ms to hash ledger: " + nsForHashing / 1e6);
                System.out.println("Total inner nodes in ledger: " + totalInnerNodes);
                System.out.println("Total unique inner nodes over all ledgers: " + uniqueInnerNodes);
                System.out.println("Total branches in ledger: " + totalSlots);
                System.out.println("Total unused branches in ledger: " + unusedSlots);
                System.out.println("Total unique leaf instances: " + totalLeavesAddedOrModified);
                System.out.println("Unique inner nodes over all ledgers: " + uniqueInnerNodes);
                System.out.println("Ratio of unused/used branches in inner nodes over all ledgers: " + ratioUnusedToUsedInInnerNodesOverTime);
                System.out.println("Ratio of unused/used branches in ledger: " + ((double) unusedSlots) / usedSlots);
                System.out.println("Unique inner nodes per total leaf instances in node store: " + uniqueInnerNodes / totalLeavesAddedOrModified);
                System.out.println("Unique inner nodes per total leaf nodes in ledger: " + uniqueInnerNodes / totalLeavesAdded);
                System.out.println();
            }
        }
        System.out.println("Random   keys used: " + randomNodes.size());

        if (CREATE_PREFIXED) {
            System.out.println("Prefixed keys used: " + bookBases.size());
            System.out.println("Prefix Histogram");
            for (Map.Entry<Integer, Integer> count : prefixIndexes.entrySet()) {
                System.out.printf("%s : %s%n", B16.toString(prefixes.get(count.getKey())), count.getValue());
            }
        }

        System.out.println();
        System.out.println("Unique inner node, branch usage histogram");
        InstrumentedInnerNode.Counter<Integer> integerCounter = ledger.slotHistogramOverTime(null);
        for (Map.Entry<Integer, Integer> count : integerCounter.entrySet()) {
            System.out.printf("%s : %s%n", count.getKey(), count.getValue());
        }

    }
    public static void main(String[] args) {
        ShamapInnerNodeAnalysis analysis = new ShamapInnerNodeAnalysis();
        analysis.analyseInnerNodeUsage();
    }
}
