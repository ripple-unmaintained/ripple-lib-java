package com.ripple.client.transactions;
import com.ripple.core.types.uint.UInt32;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public class SequencedQueue {
    PriorityQueue<Sequenced> txs = new PriorityQueue<Sequenced>(0, new Comparator<Sequenced>() {
        @Override
        public int compare(Sequenced o1, Sequenced o2) {
            return o1.sequence().compareTo(o2.sequence());
        }
    });

    public ArrayList<Sequenced> invalidated(UInt32 Sequence) {
        ArrayList<Sequenced> invalid = new ArrayList<Sequenced>();
        Iterator<Sequenced>     iter = txs.iterator();

        while (iter.hasNext()) {
            Sequenced next = iter.next();

            if (next.sequence().lte(Sequence)) {
                iter.remove();
            }
        }

        return invalid;
    }
}
