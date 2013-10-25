package com.ripple.core.formats;

import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;

public class SLEFormatTest {
    @Test
    public void testFromValue() throws Exception {

        SLEFormat accountRoot = SLEFormat.fromValue("AccountRoot");
        assertNotNull(accountRoot);
    }
}
