
package com.ripple.crypto.sjcljson;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JSEscapeTest {
    @Test
    public void testEncodings() {
        assertSane("エォオカガキ", "%u30A8%u30A9%u30AA%u30AB%u30AC%u30AD");
        assertSane("wtf bbq?? -- this", "wtf%20bbq%3F%3F%20--%20this");
    }

    private void assertSane(String raw, String escaped) {
        assertEquals(raw, JSEscape.unescape(escaped));
        assertEquals(escaped, JSEscape.escape(raw));
    }
}
