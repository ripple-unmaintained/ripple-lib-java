package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.fields.Field;
import com.ripple.core.types.known.sle.LedgerEntry;
import com.ripple.core.types.known.sle.ThreadedLedgerEntry;
import com.ripple.core.types.known.sle.entries.DirectoryNode;
import com.ripple.core.types.known.sle.entries.Offer;
import com.ripple.core.types.known.sle.entries.RippleState;
import com.ripple.core.types.known.tx.result.AffectedNode;
import com.ripple.core.types.known.tx.result.TransactionResult;

import java.util.*;

public class AccountStateBuilder {
    private AccountState state;
    private AccountState previousState = null;
    private long targetLedgerIndex;
    public long nextTransactionIndex = 0;
    private Hash256 targetAccountHash;
    public long totalTransactions = 0;

    private TreeSet<Hash256> directoriesModifiedMoreThanOnceByTransaction = new TreeSet<Hash256>();
    private TreeSet<Hash256> directoriesModifiedByTransaction = new TreeSet<Hash256>();
    public TreeSet<Hash256> modifiedEntries = new TreeSet<Hash256>();

    public void resetModified() {
        modifiedEntries.clear();
        directoriesModifiedMoreThanOnceByTransaction.clear();
    }

    public AccountStateBuilder(AccountState state, long targetLedgerIndex) {
        this.state = state;
        setStateCheckPoint();
        this.targetLedgerIndex = targetLedgerIndex;
    }

    public void onLedgerClose(long ledgerIndex, Hash256 accountHash, Hash256 parentHash) {
        state.updateSkipLists(ledgerIndex, parentHash);
        targetLedgerIndex = ledgerIndex;
        targetAccountHash = accountHash;
        nextTransactionIndex = 0;
    }

    public void setStateCheckPoint() {
        previousState = state.copy();
    }

    public void onTransaction(TransactionResult tr) {
        if (tr.meta.transactionIndex().longValue() != nextTransactionIndex) throw new AssertionError();
        if (tr.ledgerIndex.longValue() != targetLedgerIndex + 1) throw new AssertionError(String.format("%d != %d", tr.ledgerIndex.longValue(), targetLedgerIndex + 1));
        nextTransactionIndex++;
        totalTransactions++;
        directoriesModifiedByTransaction = new TreeSet<Hash256>();

        for (AffectedNode an : sortedAffectedNodes(tr)) {
            Hash256 id = an.ledgerIndex();
            LedgerEntry le = (LedgerEntry) an.nodeAsFinal();
            if (an.isCreatedNode()) {
                modifiedEntries.add(id);
                le.setDefaults();
                state.addLE(le);

                if (le instanceof Offer) {
                    Offer offer = (Offer) le;
                    offer.setOfferDefaults(); // TODO / TODO

                    for (Hash256 directory : offer.directoryIndexes()) {
                        DirectoryNode dn = getDirectoryForUpdating(directory);
                        Hash256 index = offer.index();
                        addToDirectoryNode(dn, index);
                    }
                } else if (le instanceof RippleState) {
                    RippleState state = (RippleState) le;

                    for (Hash256 directory : state.directoryIndexes()) {
                        DirectoryNode dn = getDirectoryForUpdating(directory);
                        addToDirectoryNode(dn, state.index());
                    }
                }
                if (le instanceof ThreadedLedgerEntry) {
                    ThreadedLedgerEntry tle = (ThreadedLedgerEntry) le;
                    tle.previousTxnID(tr.hash);
                    tle.previousTxnLgrSeq(tr.ledgerIndex);
                }
            } else if (an.isDeletedNode()) {
                modifiedEntries.remove(id);
                directoriesModifiedMoreThanOnceByTransaction.remove(id);
                state.removeLeaf(id);
                if (le instanceof Offer) {
                    Offer offer = (Offer) le;
                    for (Hash256 directory : offer.directoryIndexes()) {
                        try {
                            DirectoryNode dn = getDirectoryForUpdating(directory);
                            if (dn != null) {
//                                Hash256 index = offer.index();
                                if (dn.owner() != null) {
                                    deleteFromDirectoryUnstable(offer, dn);
                                } else {
                                    deleteFromDirectoryStable(offer, dn);
                                }
                            }
                        } catch (Exception e) {
                            //
                        }
                    }
                } else if (le instanceof RippleState) {
                    RippleState state = (RippleState) le;
                    for (Hash256 directory : state.directoryIndexes()) {
                        try {
                            DirectoryNode dn = getDirectoryForUpdating(directory);
                            if (dn != null) {
                                deleteFromDirectoryUnstable(le, dn);
                            }
                        } catch (Exception e) {
                            //
                        }
                    }
                }
            } else if (an.isModifiedNode()) {
                modifiedEntries.add(id);
                ShaMapLeaf leaf = state.getLeafForUpdating(id);
                LedgerEntryItem item = (LedgerEntryItem) leaf.item;
                LedgerEntry leModded = item.entry;

                if (le instanceof ThreadedLedgerEntry) {
                    ThreadedLedgerEntry tle = (ThreadedLedgerEntry) le;
                    tle.previousTxnID(tr.hash);
                    tle.previousTxnLgrSeq(tr.ledgerIndex);
                }
                for (Field field : le) {
                    if (field == Field.LedgerIndex) {
                        continue;
                    }
                    leModded.put(field, le.get(field));
                }
            }
        }
    }

    private void deleteFromDirectoryUnstable(LedgerEntry b4, DirectoryNode dn) {
        boolean b = directoryRemoveUnstable(dn, b4.index());
        DirectoryNode cursor = dn;

        if (!b) {
            while (cursor.indexNext() != null) {
                cursor = getDirectoryForUpdating(cursor.nextIndex());
                b  = directoryRemoveUnstable(cursor, b4.index());
                if (b) {
                    break;
                }
            }
        }
//
        if (!b) {
            cursor = dn;
            while (cursor.indexPrevious() != null) {
                cursor = getDirectoryForUpdating(cursor.prevIndex());
                b  = directoryRemoveUnstable(cursor, b4.index());
                if (b) {
                    break;
                }
            }
        }
    }
    private void deleteFromDirectoryStable(LedgerEntry b4, DirectoryNode dn) {
        boolean b = directoryRemoveStable(dn, b4.index());
        DirectoryNode cursor = dn;

        if (!b) {
            cursor = dn;
            while (cursor.indexPrevious() != null) {
                cursor = getDirectoryForUpdating(cursor.prevIndex());
                b  = directoryRemoveStable(cursor, b4.index());
                if (b) {
                    break;
                }
            }
        }
        if (!b) {
            while (cursor.indexNext() != null) {
                cursor = getDirectoryForUpdating(cursor.nextIndex());
                b  = directoryRemoveStable(cursor, b4.index());
                if (b) {
                    break;
                }
            }
        }
    }

    public static <E> Collection<E> makeCollection(Iterable<E> iter) {
        Collection<E> list = new ArrayList<E>();
        for (E item : iter) {
            list.add(item);
        }
        return list;
    }
    private ArrayList<AffectedNode> sortedAffectedNodes(TransactionResult tr) {
        ArrayList<AffectedNode> sorted = new ArrayList<AffectedNode>(makeCollection(tr.meta.affectedNodes()));
        Collections.sort(sorted, new Comparator<AffectedNode>() {
            @Override
            public int compare(AffectedNode o1, AffectedNode o2) {
                return ord(o1) - ord(o2);
            }

            private int ord(AffectedNode o1) {
                switch (o1.ledgerEntryType()) {
                    case DirectoryNode:
                        return 1;
                    case RippleState:
                        return 2;
                    case Offer:
                        return 3;
                    default:
                        return 4;
                }
            }
        });
        return sorted;
    }

    private void onDirectoryModified(DirectoryNode dn) {
        Hash256 index = dn.index();
        if (directoriesModifiedByTransaction.contains(index)) {
            directoriesModifiedMoreThanOnceByTransaction.add(index);
        }
        else {
            directoriesModifiedByTransaction.add(index);
        }
    }
    private boolean directoryRemoveStable(DirectoryNode dn, Hash256 index) {
        onDirectoryModified(dn);
        return dn.indexes().remove(index);
    }
    private boolean directoryRemoveUnstable(DirectoryNode dn, Hash256 index) {
        onDirectoryModified(dn);
        return dn.indexes().removeUnstable(index);
    }
    private void addToDirectoryNode(DirectoryNode dn, Hash256 index) {
        onDirectoryModified(dn);
        dn.indexes().add(index);
    }
    private DirectoryNode getDirectoryForUpdating(Hash256 directoryIndex) {
        ShaMapLeaf leaf = state.getLeafForUpdating(directoryIndex);
        if (leaf == null) {
            return null;
        }
        LedgerEntryItem lei = (LedgerEntryItem) leaf.item;
        return (DirectoryNode) lei.entry;
    }

    public AccountState state() {
        return state;
    }

    public long currentLedgerIndex() {
        return targetLedgerIndex;
    }

    public String targetAccountHashHex() {
        return targetAccountHash.toHex();
    }
    public Hash256 targetAccountHash() {
        return targetAccountHash;
    }

    public TreeSet<Hash256> directoriesWithIndexesOutOfOrder() {
        TreeSet<Hash256> ret = new TreeSet<Hash256>();
        for (Hash256 hash256 : directoriesModifiedMoreThanOnceByTransaction) {
            DirectoryNode dn = state.getDirectoryNode(hash256);
            if (dn.owner() != null) {
                ret.add(hash256);
            }
        }
        return ret;
    }

    public boolean bad() {
        return !state.hash().equals(targetAccountHash);
    }

    public AccountState previousState() {
        return previousState;
    }

    public void setState(AccountState map) {
        state = map;
    }
}
