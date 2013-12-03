package com.ripple.core.types;

import com.ripple.core.types.hash.Hash160;
import org.junit.Test;

public class CurrencyTest {
    @Test
    public void testCurrencyParsing() throws Exception {
        Hash160 stupid = Hash160.translate.fromString("rrrrrrrrrrrrrpSYV6scnaNmpdyWq7");
        String s = Currency.decodeCurrency(stupid.getBytes());
        System.out.println(s);
    }
}
