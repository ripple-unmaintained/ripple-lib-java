package com.ripple.core;

import com.ripple.core.coretypes.STObject;
import com.ripple.core.coretypes.hash.Hash128;
import com.ripple.core.coretypes.hash.Hash256;
import org.junit.Test;

import static com.ripple.core.coretypes.hash.Hash256.Hash256Map;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;


public class HashTest{
    @Test
    public void testPadding() {
        Hash128 hash128 = new Hash128(new byte[]{0});
        assertEquals(16, hash128.bytes().length);
    }

    @Test(expected = RuntimeException.class)
    public void testBoundsChecking() {
        Hash128 hash128 = new Hash128(new byte[32]);
    }

    @Test
    public void testTreeMapSuitability() throws Exception {
        Hash256 a = new Hash256(new byte[32]);
        Hash256 b = new Hash256(new byte[32]);
        Hash256 c = new Hash256(new byte[32]);
        Hash256 d = new Hash256(new byte[32]);

        STObject objectA = new STObject();
        STObject objectB = new STObject();
        STObject objectC = new STObject();

        a.bytes()[0] = 'a';
        b.bytes()[0] = 'b';
        c.bytes()[0] = 'c';
        d.bytes()[0] = 'a';

        Hash256Map<STObject> tree = new Hash256Map<STObject>();
        tree.put(a, objectA);

        // There can be ONLY one
        assertTrue(tree.containsKey(d));

        tree.put(b, objectB);
        tree.put(c, objectC);

        assertTrue(tree.get(a) == objectA);
        assertTrue(tree.get(b) == objectB);
        assertTrue(tree.get(c) == objectC);
    }
}
