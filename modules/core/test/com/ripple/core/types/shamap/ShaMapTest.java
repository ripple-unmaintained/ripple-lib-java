package com.ripple.core.types.shamap;

import com.ripple.core.types.hash.Hash256;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class ShaMapTest {
    @Test
    public void testNibblet() throws Exception {
        String ledgerIndex = "D66D0EC951FD5707633BEBE74DB18B6D2DDA6771BA0FBF079AD08BFDE6066056";
        Hash256 index = Hash256.translate.fromString(ledgerIndex);

        for (int i = 0; i < ledgerIndex.length(); i++) {
            int n1 = index.nibblet(i);
            String s = Integer.toHexString(n1).toUpperCase();
            assertEquals(ledgerIndex.substring(i, i + 1), s);
        }
    }

    @Test
    public void testArrayAssumptions() throws Exception {
        String[] arr = new String[16];
        assertEquals(null, arr[0]);
        assertEquals(null, arr[15]);
    }
}
