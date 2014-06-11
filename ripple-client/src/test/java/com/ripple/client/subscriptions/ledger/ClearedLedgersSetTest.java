package com.ripple.client.subscriptions.ledger;

import org.junit.Test;

import static org.junit.Assert.*;

public class ClearedLedgersSetTest {
    ClearedLedgersSet set = new ClearedLedgersSet();
    {
        set.clear(5);
        set.clear(1);
        set.clear(0);
    }

    @Test
    public void testOkToClear() throws Exception {
        assertFalse(set.okToClear());
    }

    @Test
    public void testGaps() throws Exception {
        long[] expectedGaps = new long[]{2, 3, 4};
        int i = 0;
        for (Long gap : set.gaps()) {
            assertEquals(expectedGaps[i++], (long) gap);
        }
    }
}