package com.ripple.core.serialized;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class BytesTreeTest {
    @Test
    public void testNested() throws Exception {

        BytesTree ba1 = new BytesTree();
        BytesTree ba2 = new BytesTree();

        ba1.add(new byte[]{'a', 'b', 'c'});
        ba1.add(new byte[]{'d', 'e'});

        ba2.add(new byte[]{'f', 'g'});
        ba2.add((byte) 'h');
        ba2.add(ba1);

        assertEquals(ba2.length(), 8);
        byte[] bytes = ba2.bytes();
        String ascii = new String(bytes, "ascii");

        assertEquals("fghabcde", ascii);
    }
}
