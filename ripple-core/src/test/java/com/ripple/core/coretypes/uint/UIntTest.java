package com.ripple.core.coretypes.uint;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

public class UIntTest {
    @Test
    public void testLTE() throws Exception {
        UInt64 n = new UInt64(34);
        UInt32 n2 = new UInt32(400);
        assertTrue(n.lte(n2));
    }
}
