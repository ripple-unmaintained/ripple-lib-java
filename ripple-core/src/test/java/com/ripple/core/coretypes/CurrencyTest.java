package com.ripple.core.coretypes;

import com.ripple.core.coretypes.hash.Hash160;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class CurrencyTest {
    @Test
    public void testCurrencyParsing() throws Exception {
        Hash160 stupid = Hash160.translate.fromString("rrrrrrrrrrrrrpSYV6scnaNmpdyWq7");
        String s = Currency.getCurrencyCodeFromTLCBytes(stupid.bytes());
        System.out.println(s);
    }

    @Test
    public void testDemurraging() throws Exception {
        String wtfDemure = "015841551A748AD23FEFFFFFFFEA028000000000";
        Currency currency = Currency.fromString(wtfDemure);
        Currency.Demurrage demurrage = currency.demurrage;
        assertEquals("XAU", demurrage.code);
        assertEquals(0.99999999984D, demurrage.rate);
        assertEquals("23 Jan 2014 02:22:10 GMT", demurrage.startDate.toGMTString());
    }
}
