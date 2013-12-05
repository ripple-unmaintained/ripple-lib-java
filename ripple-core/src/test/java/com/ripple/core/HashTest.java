package com.ripple.core;

import com.ripple.core.types.hash.Hash128;
import org.junit.Test;

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
}
