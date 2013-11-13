package com.ripple.core.serialized;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class ByteArrayListTest {
    @Test
    public void testNested() throws Exception {

        ByteArrayList ba1 = new ByteArrayList();
        ByteArrayList ba2 = new ByteArrayList();

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
