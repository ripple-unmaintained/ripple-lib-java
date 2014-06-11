package com.ripple.client.subscriptions.ledger;

import java.util.TreeSet;

public class ClearedLedgersSet {
    public static boolean DEBUG = true;
    long lastCleared =-1, firstCleared =-1;

    public TreeSet<Long> cleared() {
        return clearedLedgers;
    }

    public int size() {
        return clearedLedgers.size();
    }

    TreeSet<Long> clearedLedgers = new TreeSet<Long>();
    TreeSet<Long> clearedLedgersNeverCleared = new TreeSet<Long>();

    public void clear(long ledger_index) {
        if (DEBUG) clearedLedgersNeverCleared.add(ledger_index);
        clearedLedgers.add(ledger_index);
    }

    public void clearIfNoGaps() {
        if (okToClear()) {
            if (DEBUG) {
                // Set the very first ledger cleared
                if (firstCleared == -1) {
                    firstCleared = clearedLedgers.first();
                }
                // The very last cleared ledger
                lastCleared = Math.max(clearedLedgers.last(), lastCleared);

                // If our debug set contained the
                for (long i = firstCleared; i <= lastCleared; i++) {
                    if (!clearedLedgersNeverCleared.contains(i)) throw new AssertionError();
                }
            }
            clearedLedgers.clear();
        }
    }

    public TreeSet<Long> gaps() {
        TreeSet<Long> gaps = new TreeSet<Long>();
        int i = 0;
        long prev = 0;

        for (Long clearedLedger : clearedLedgers) {
            if (i++ > 0) {
                for (long j = prev + 1; j < clearedLedger; j++) {
                    gaps.add(j);
                }
            }
            prev = clearedLedger;
        }

        return gaps;
    }

    public boolean okToClear() {
        return gaps().size() == 0;
    }

    public boolean contains(long ledger_index) {
        if (DEBUG) {
            boolean authoritativeHas = clearedLedgersNeverCleared.contains(ledger_index);
            if (authoritativeHas && !clearedLedgers.contains(ledger_index)) {
                throw new AssertionError();
            }
            return authoritativeHas;
        } else {
            return clearedLedgers.contains(ledger_index);
        }
    }
}
