package com.ripple.core.coretypes;

import com.ripple.core.coretypes.hash.Hash160;
import org.junit.Test;

public class CurrencyTest {
    @Test
    public void testCurrencyParsing() throws Exception {
        Hash160 stupid = Hash160.translate.fromString("rrrrrrrrrrrrrpSYV6scnaNmpdyWq7");
        String s = Currency.decodeCurrency(stupid.bytes());
        System.out.println(s);
    }
}
