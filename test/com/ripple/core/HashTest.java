package com.ripple.core;
import com.ripple.core.types.hash.Hash128;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class HashTest{
    @Test
    public void testPadding() {
        Hash128 hash128 = new Hash128(new byte[]{0});
        assertEquals(16, hash128.getBytes().length);
    }

    @Test(expected = RuntimeException.class)
    public void testBoundsChecking() {
        Hash128 hash128 = new Hash128(new byte[32]);
    }
}
