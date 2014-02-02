package com.ripple.cli.shamapanalysis;

import com.ripple.config.Config;
import com.ripple.core.coretypes.Vector256;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.types.shamap.ShaMapInnerNode;
import com.ripple.core.types.shamap.ShaMapLeafNode;
import com.ripple.core.types.shamap.ShaMapNode;
import com.ripple.encodings.common.B16;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

/**
See the README.md in the same directory
*/
public class ShamapInnerNodeAnalysis {

    public static String ANALYSIS_LOG_FILE = "ledger-analysis-log.json";

    public static void main(String[] args) throws IOException {
        File f = new File(ANALYSIS_LOG_FILE);
        if (f.exists()) {
            boolean delete = f.delete();
        }
        appendTo(ANALYSIS_LOG_FILE, LedgerStat.legendJSON());

        ShamapInnerNodeAnalysis analysis = new ShamapInnerNodeAnalysis();
        analysis.CREATE_PREFIXED = false;
        analysis.analyseInnerNodeUsage();

        ShamapInnerNodeAnalysis analysis2 = new ShamapInnerNodeAnalysis();
        analysis2.CREATE_PREFIXED = true;
        analysis2.analyseInnerNodeUsage();
    }

    // Basic configuration for
    int NUM_LEDGERS = 10000;
    int NEW_NODES_PER_LEDGER = 10;
    int MODIFIED_NODES_PER_LEDGER = 30;

    // Call onLedgerHashed every n ledgers
    int INFO_EVERY_N_LEDGERS = 25;

    // Emulating a book base, which has a 192 bit common prefix
    boolean CREATE_PREFIXED = false;
    int     NUM_PREFIXES = 25;
    int     PREFIXED_EVERY = 5;

    public static enum LedgerStat {
        ledgers_passed("Ledgers passed"),
        leaf_nodes_in_ledger("Total leaf nodes in latest ledger"),
        leaf_nodes_added_per_ledger("Leaf nodes added per ledger"),
        leaf_nodes_updated_per_ledger("Leaf nodes updated per ledger"),
        ledger_maximum_depth("Ledger maximum depth"),
        ms_to_add_leaves("Ms to add new leaves"),
        ms_to_update_leaves("Ms to update old leaves"),
        ledger_dirty_vs_clean_inner_nodes_before_hashing("Histogram of dirty to clean inner nodes before hashing"),
        ms_to_hash_ledger("Ms to hash ledger"),
        ledger_inner_nodes("Total inner nodes in ledger"),
        nodestore_inner_node_instances("Total unique inner nodes over all ledgers"),
        ledger_total_branches("Total branches in ledger"),
        ledger_unused_branches("Total unused branches in ledger"),
        nodestore_leaf_instances("Total unique leaf instances"),
        nodestore_inner_node_ratio_unused_used_branches("Ratio of unused/used branches in inner nodes over all ledgers"),
        ledger_inner_node_ratio_unused_used_branches("Ratio of unused/used branches in ledger"),
        nodestore_inner_node_to_nodestore_leaf_ratio("Unique inner nodes per total leaf instances in node store"),
        nodestore_inner_node_to_ledger_leaf_ratio("Unique inner nodes per total leaf nodes in ledger");
        String description;

        LedgerStat(String description) {
            this.description = description;
        }

        public static String legendJSON() {
            JSONObject obj = new JSONObject();
            try {
                obj.put("log_type", "ledger_stats_legend");
                for (LedgerStat stat : values()) {
                    obj.put(stat.toString(), stat.description);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return obj.toString();
        }
    }

    // TODO, this unwieldy beast needs to be rewritten for increased performance
    // rather than walk the map multiple times redundantly, collect
    // all the information at once
    // perhaps a `walk` function, that takes a visitor
    // onLeafNode, onInnerNode
    // it could then hopefully rather easily gather all the stats in 2
    // goes, before/after hashing, after every $n ledgers

    // We create random indexes, using the index bytes themselves as
    // the leaf node bytes
    Random randomness = new Random();
    //
    public long hashesCounter = 0;

    // Create some random prefixes, as equivalent to the
    ArrayList<byte[]> prefixes;
    Counter<Integer> prefixIndexes;
    {
        setUpBookBasePrefixes();
    }

    private void setUpBookBasePrefixes() {
        prefixes = new ArrayList<byte[]>();

        for (int i = 0; i < NUM_PREFIXES; i++) {
            byte[] b = new byte[24];
            // A little trick to ensure we hit every top level
            // inner node children of root (where NUM_PREFIXES >= 16)
            b[0] |= ((i % 16) << 4);
            randomness.nextBytes(b);
            prefixes.add(b);
        }
        prefixIndexes = new Counter<Integer>();
    }

    // Use a book base key
    public boolean usePrefixedKey() {
        return CREATE_PREFIXED && ++hashesCounter % PREFIXED_EVERY == 0;
    }

    private byte[] randomPrefix() {
        int randomIndex = randomness.nextInt(prefixes.size() - 1);
        prefixIndexes.count(randomIndex);
        return prefixes.get(randomIndex);
    }

    private Hash256 randomPrefixedHash() {
        byte[] prefix1 = randomPrefix();

        byte[] b;
        b = new byte[8];
        randomness.nextBytes(b);

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
        randomness.nextBytes(b);
        return new Hash256(b);
    }

    public void analyseInnerNodeUsage() {
        appendTo(ANALYSIS_LOG_FILE, ledgerAnalysisStartLog());

        InstrumentedShaMap ledger = new InstrumentedShaMap();

        // Ledger will blow up if we try to use the same index twice,
        // so we don't need to store these in sets, just a container
        // with quick append
        Vector256 randomIndexes = new Vector256();
        Vector256 prefixedIndexes = new Vector256();

        int totalLeavesAdded = 0;
        int totalLeavesAddedOrModified = 0;
        int ledgersDone = 0;
        double pta;

        for (int i = 0; i < NUM_LEDGERS; i++) {
            pta = now();
            int n = 0;
            while (n++ < NEW_NODES_PER_LEDGER) {
                Hash256 key;
                if (usePrefixedKey()) {
                    key = randomPrefixedHash();
                    prefixedIndexes.add(key);
                } else {
                    key = randomHash();
                    randomIndexes.add(key);
                }

                addKeyAsValueItem(ledger, key);
                totalLeavesAdded++;
            }
            double nsForAdding = now() - pta;

            pta = now();
            if (ledgersDone > 1 && MODIFIED_NODES_PER_LEDGER <= randomIndexes.size()) {
                for (int j = 0; j < MODIFIED_NODES_PER_LEDGER; j++) {
                    ArrayList<Hash256> whichRandom;
                    if (usePrefixedKey()) {
                        whichRandom = prefixedIndexes;
                    } else {
                        whichRandom = randomIndexes;
                    }
                    int randomKeyIndex = randomness.nextInt(whichRandom.size() - 1);
                    Hash256 keyToUpdate = whichRandom.get(randomKeyIndex);
                    // invalidating ;)
                    ShaMapLeafNode leaf = ledger.getLeafForUpdating(keyToUpdate);
                    // so the hash is actually different ;0
                    leaf.setBlob(createItem(randomHash()));
                }
            }
            double nsForModding = now() - pta;

            totalLeavesAddedOrModified += MODIFIED_NODES_PER_LEDGER + NEW_NODES_PER_LEDGER;

            Counter<String> dirtyVsClean = ledger.dirtyVsCleanNodes(null);

            pta = now();
            ledger.hash();
            double nsForHashing = now() - pta;

            ledgersDone++;

            if (ledgersDone % INFO_EVERY_N_LEDGERS == 0) {
                onLedgerHashed(ledger, totalLeavesAdded, totalLeavesAddedOrModified, ledgersDone, nsForAdding, nsForModding, nsForHashing, dirtyVsClean);
            }
        }
        System.out.println("Random   keys used: " + randomIndexes.size());

        if (CREATE_PREFIXED) {
            System.out.println("Prefixed keys used: " + prefixedIndexes.size());
            System.out.println("Prefix Histogram");
            for (Map.Entry<Integer, Integer> count : prefixIndexes.entrySet()) {
                System.out.printf("%s : %s%n", B16.toString(prefixes.get(count.getKey())), count.getValue());
            }
        }

        System.out.println();
        System.out.println("Unique inner node, branch usage histogram");
        Counter<Integer> integerCounter = ledger.slotHistogramOverTime(null);
        JSONObject statsLog;
        try {
            statsLog = new JSONObject();
            statsLog.put("log_type", "nodestore_innernode_slot_usage_histogram");
            statsLog.put("histogram", integerCounter.histogram());

            for (Map.Entry<Integer, Integer> count : integerCounter.entrySet()) {
//                JSONArray tuple = new JSONArray();
                Integer slotsUsed = count.getKey();
                Integer nodesUsingThatMany = count.getValue();
                System.out.printf("%s : %s%n", slotsUsed, nodesUsingThatMany);
//                tuple.put(slotsUsed);
//                tuple.put(nodesUsingThatMany);
//                slotHistogram.put(tuple);

//                slotHistogram.put(slotsUsed.toString(), nodesUsingThatMany);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        appendTo(ANALYSIS_LOG_FILE, statsLog.toString());

    }

    private String ledgerAnalysisStartLog() {
        JSONObject obj = new JSONObject();

        try {
            obj.put("log_type", "ledger_analysis_start");
            obj.put("num_ledgers", NUM_LEDGERS);
            obj.put("new_nodes_per_ledger", NEW_NODES_PER_LEDGER);
            obj.put("modified_nodes_per_ledger", MODIFIED_NODES_PER_LEDGER);
            obj.put("info_every_n_ledgers", INFO_EVERY_N_LEDGERS);
            obj.put("num_prefixes", NUM_PREFIXES);
            obj.put("create_prefixed", CREATE_PREFIXED);
            obj.put("prefixed_every", PREFIXED_EVERY);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return obj.toString();
    }

    public class LedgerStats extends EnumMap<LedgerStat, Object> {
        public LedgerStats() {
            super(LedgerStat.class);
        }

        public JSONObject json() {
            JSONObject json = new JSONObject();
            try {
                json.put("log_type", "ledger_stats");
                for (LedgerStat stat : this.keySet()) {
                    json.put(stat.toString(), get(stat));
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return json;
        }
    }

    public class LedgersStats extends TreeMap<Integer, LedgerStats> {}
    LedgersStats ledgersStats = new LedgersStats();

    private void onLedgerHashed(InstrumentedShaMap ledger, int totalLeavesAdded, int totalLeavesAddedOrModified, int ledgersDone, double nsForAdding, double nsForModding, double nsForHashing, Counter<String> dirtyVsClean) {
        System.out.println("Ledgers Passed:                 " + ledgersDone);
        System.out.println("Ms for adding/updating/hashing: " + (nsForAdding + nsForHashing + nsForModding) / 1e6);

        if (false) {
            return;
        }

        double uniqueInnerNodes = ledger.totalUniqueInnerNodesOverTime();
        long l1 = ledger.unusedBranchesOverTime();
        double ratioUnusedToUsedInInnerNodesOverTime = l1 / (uniqueInnerNodes * 16);

        long totalInnerNodes = ledger.totalInnerNodes();
        long totalSlots = ledger.totalSlots();
        long unusedSlots = ledger.unusedSlots();
        long usedSlots = totalSlots - ledger.unusedSlots();

        LedgerStats ledgerStats = new LedgerStats();

        ledgerStats.put(LedgerStat.ledgers_passed,                                   ledgersDone);
        ledgerStats.put(LedgerStat.leaf_nodes_in_ledger,                              totalLeavesAdded);
        ledgerStats.put(LedgerStat.ledger_maximum_depth,                             ledger.depth());
        ledgerStats.put(LedgerStat.ms_to_add_leaves,                                 nsForAdding / 1e6);
        ledgerStats.put(LedgerStat.ms_to_add_leaves,                                 nsForModding / 1e6);
        ledgerStats.put(LedgerStat.ms_to_hash_ledger,                                nsForHashing / 1e6);
        ledgerStats.put(LedgerStat.ledger_inner_nodes,                               totalInnerNodes);
        ledgerStats.put(LedgerStat.ledger_dirty_vs_clean_inner_nodes_before_hashing,  dirtyVsClean);
        ledgerStats.put(LedgerStat.nodestore_inner_node_instances,                   uniqueInnerNodes);
        ledgerStats.put(LedgerStat.ledger_total_branches,                            totalSlots);
        ledgerStats.put(LedgerStat.ledger_unused_branches,                           unusedSlots);
        ledgerStats.put(LedgerStat.nodestore_leaf_instances,                         totalLeavesAddedOrModified);
        ledgerStats.put(LedgerStat.nodestore_inner_node_ratio_unused_used_branches,  ratioUnusedToUsedInInnerNodesOverTime);
        ledgerStats.put(LedgerStat.ledger_inner_node_ratio_unused_used_branches,     ((double) unusedSlots) / usedSlots);
        ledgerStats.put(LedgerStat.nodestore_inner_node_to_nodestore_leaf_ratio,     uniqueInnerNodes / totalLeavesAddedOrModified);
        ledgerStats.put(LedgerStat.nodestore_inner_node_to_ledger_leaf_ratio,        uniqueInnerNodes / totalLeavesAdded);

//        ledgersStats.put(ledgersDone, ledgerStats);

        appendTo(ANALYSIS_LOG_FILE, ledgerStats.json().toString());
    }

    private static void appendTo(String outputFile, String result) {
        try {
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, true)));
            writer.write(result);
            writer.write("\n");
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static class Counter<KeyType> extends TreeMap<KeyType, Integer> {
        public void count(KeyType value) {
            Integer existing = get(value);
            if (existing == null) existing = 1;
            put(value, existing  + 1);
        }
        public JSONArray histogram() {
            JSONArray histogram = new JSONArray();
            try {
                for (Map.Entry<KeyType, Integer> entry : entrySet()) {
                    JSONArray tuple = new JSONArray();
                    tuple.put(entry.getKey());
                    tuple.put(entry.getValue());
                    histogram.put(tuple);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return histogram;
        }
    }
}

class InstrumentedInnerNode extends ShaMapInnerNode {
    // Annoyingly we need to init this
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

    public int depth() {
        int depth = this.depth;

        for (ShaMapNode branch : branches) {
            if (branch instanceof InstrumentedInnerNode) {
                InstrumentedInnerNode inner = (InstrumentedInnerNode) branch;
                depth = Math.max(depth, inner.depth());
            }
        }

        return depth;
    }

    public ShamapInnerNodeAnalysis.Counter<String> dirtyVsCleanNodes(ShamapInnerNodeAnalysis.Counter<String> counter) {
        if (counter == null) {
            counter = new ShamapInnerNodeAnalysis.Counter<String>();
        }
        counter.count(hash == null ? "dirty" : "clean");

        for (ShaMapNode branch : branches) {
            if (branch instanceof InstrumentedInnerNode) {
                InstrumentedInnerNode inner = (InstrumentedInnerNode) branch;
                inner.dirtyVsCleanNodes(counter);
            }
        }

        return counter;
    }

    public ShamapInnerNodeAnalysis.Counter<Integer> slotHistogramOverTime(ShamapInnerNodeAnalysis.Counter<Integer> counter) {
        if (counter == null) {
            counter = new ShamapInnerNodeAnalysis.Counter<Integer>();
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

}
class InstrumentedShaMap extends InstrumentedInnerNode {
    protected InstrumentedShaMap() {
        super(0);
    }
}
