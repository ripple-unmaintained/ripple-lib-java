package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.STObject;
import com.ripple.core.coretypes.Vector256;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.Index;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.serialized.enums.LedgerEntryType;
import com.ripple.core.types.known.sle.LedgerEntry;
import com.ripple.core.types.known.sle.LedgerHashes;
import com.ripple.core.types.known.sle.entries.DirectoryNode;
import com.ripple.core.types.known.sle.entries.OfferDirectory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONWriter;

import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

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

    public boolean addLE(LedgerEntry entry) {
        LedgerEntryItem item = new LedgerEntryItem(entry);
        return addItem(entry.index(), item);
    }

    public boolean updateLE(LedgerEntry entry) {
        LedgerEntryItem item = new LedgerEntryItem(entry);
        return updateItem(entry.index(), item);
    }

    public LedgerEntry getLE(Hash256 index) {
        LedgerEntryItem item = (LedgerEntryItem) getItem(index);
        return item == null ? null : item.value();
    }

    public DirectoryNode getDirectoryNode(Hash256 index) {
        return (DirectoryNode) getLE(index);
    }

    public Iterable<OfferDirectory> offerDirectories(Hash256 bookBase) {
        final QualityIterator iter = qualityIterator(bookBase);

        return new Iterable<OfferDirectory>() {
            @Override
            public Iterator<OfferDirectory> iterator() {
                return new Iterator<OfferDirectory>() {
                    @Override
                    public boolean hasNext() {
                        boolean hasNext = iter.hasNext();
                        // In case we  need to skip some entries
                        if (hasNext && !(iter.next() instanceof OfferDirectory)) {
                            return this.hasNext();
                        }
                        return hasNext;
                    }

                    @Override
                    public OfferDirectory next() {
                        return (OfferDirectory) iter.next();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public void writeEntriesArray(final JSONWriter writer) {
        writer.array();
        walkEntries(new LedgerEntryVisitor() {
            @Override
            public void onEntry(LedgerEntry entry) {
                writer.value(entry.toJSON());
            }
        });
        writer.endArray();
    }

    // Assumes shamap won't be modified during iteration, not unusual for an
    // iterator.
    public class QualityIterator implements Iterator<LedgerEntry> {
        ShaMapInner[] inners = new ShaMapInner[64];
        Hash256 base;
        Hash256 end;
        int[] selections = new int[64];
        int commonNibblets;
        int depth;
        ShaMapLeaf next;
        private boolean finished = false;

        public QualityIterator(Hash256 start) {
            base = start;
            depth=0;
            end = Index.bookEnd(start);
            ShaMapInner inner = AccountState.this;
            setInner(inner);
            setSelected(start.nibblet(depth));
            findCommonNibblets();
        }

        private void findCommonNibblets() {
            for (int i = 0; i < 64; i++) {
                if (base.nibblet(i) == end.nibblet(i)) {
                    commonNibblets = i;
                } else {
                    break;
                }
            }
        }

        private void setInner(ShaMapInner inner) {
            inners[depth] = inner;
        }

        private void findNext() {
            next = null;

            while (true) {
                while (selected() > 15) {
                    depth--;
                    incrementSelection();
                }
                if (depth < commonNibblets && selected() > end.nibblet(depth)) {
                    finished = true;
                    break;
                }
                ShaMapInner current = currentInner();
                ShaMapNode branch = current.getBranch(selected());
                if (branch == null) {
                    incrementSelection();
                } else if (branch.isInner()) {
                    depth++;
                    setInner(branch.asInner());
                    setSelected(base.nibblet(depth));
                } else if (branch.isLeaf()) {
                    ShaMapLeaf leaf = branch.asLeaf();
                    Hash256 leafIndex = leaf.index;
                    boolean leafIsOnPathToBase = depth < commonNibblets;
                    if ( leafIsOnPathToBase || leafIndex.compareTo(base) > 0 &&
                                               leafIndex.compareTo(end) < 0) {
                        next = leaf;
                        incrementSelection();
                    } else {
                        finished = true;
                    }
                    break;
                }
            }
        }

        private ShaMapInner currentInner() {
            return inners[depth];
        }
        private int selected() {
            return selections[depth];
        }
        private void setSelected(int nibblet) {
            selections[depth] = nibblet;
        }
        private void incrementSelection() {
            selections[depth]++;
        }

        @Override
        public boolean hasNext() {
            findNext();
            return !finished && next != null;
        }
        @Override
        public LedgerEntry next() {
            // Just assume hasNext has been called
            LedgerEntryItem item = (LedgerEntryItem) next.item;
            return item.entry;
        }
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public QualityIterator qualityIterator(final Hash256 bookBase) {
        return new QualityIterator(bookBase);
    }

    public Iterable<Hash256> directoryIterator(OfferDirectory forQuality) {
        // TODO: create an actual iterator
        Vector256 indexes = new Vector256();
        OfferDirectory cursor = forQuality;

        while (cursor != null) {
            indexes.addAll(cursor.indexes());
            if (cursor.hasNextIndex()) {
                LedgerEntry le = getLE(cursor.nextIndex());
                if (le instanceof OfferDirectory) {
                    cursor = (OfferDirectory) le;
                }
                else {
                    break;
                }
            }
            else {
                break;
            }
        }
        return indexes;
    }

    public void walkEntries(final LedgerEntryVisitor walker) {
        walkLeaves(new LeafWalker() {
            @Override
            public void onLeaf(ShaMapLeaf leaf) {
                LedgerEntryItem item = (LedgerEntryItem) leaf.item;
                walker.onEntry(item.entry);
            }
        });
    }

    // TODO
    public Hash256 getNextIndex(Hash256 nextIndex, Hash256 bookEnd) {
        return null;
    }

    @Override
    public AccountState copy() {
        return (AccountState) super.copy();
    }

    public static AccountState loadFromLedgerDump(String filePath) throws IOException {
        FileReader reader = new FileReader(filePath);
        JSONTokener tokenizer = new JSONTokener(reader);
        JSONObject ledger = new JSONObject(tokenizer);
        if (ledger.has("result")) {
            ledger = ledger.getJSONObject("result");
        }
        if (ledger.has("ledger")) {
            ledger = ledger.getJSONObject("ledger");
        }
        JSONArray array = ledger.getJSONArray("accountState");
        reader.close();
        return parseShaMap(array);
    }

    public static AccountState parseShaMap(JSONArray array) {
        AccountState map = new AccountState();
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonItem = array.getJSONObject(i);
            map.addLE((LedgerEntry) STObject.fromJSONObject(jsonItem));
        }
        return map;
    }

}
