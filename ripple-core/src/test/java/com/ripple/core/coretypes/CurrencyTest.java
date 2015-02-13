package com.ripple.core.coretypes;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;

public class CurrencyTest {
    @Test
    public void testTranslateToString() throws Exception {
        String code = "USD";
        Currency currency = Currency.fromString(code);
        String hex = currency.toHex();
        assertEquals("0000000000000000000000005553440000000000", hex);
        assertEquals("USD", Currency.translate.toString(Currency.fromString(hex)));
    }


    @Test
    public void testDemurraging() throws Exception {
        String demureHex = "015841551A748AD23FEFFFFFFFEA028000000000";
        Currency currency = Currency.fromString(demureHex);
        Currency.Demurrage demurrage = currency.demurrage;
        assertEquals(demureHex, Currency.translate.toString(currency));
        assertEquals("XAU", demurrage.isoCode);
        assertEquals(0.99999999984D, demurrage.interestRate);
        assertEquals("24 Jan 2014 02:22:10 GMT", demurrage.interestStart.toGMTString());
    }

//    @Test
    public void testDemurragingRate() throws Exception {
        BigDecimal amount = new BigDecimal("100");
        BigDecimal factor = new BigDecimal("0.995");
        BigDecimal rate = Currency.Demurrage.calculateRate(factor, TimeUnit.DAYS, 365);

        System.out.println("The starting amount is: " + amount);
        System.out.println("The demurrage factor:   " + factor);
        System.out.println("The rate:               " + rate);
        System.out.println();

        for (int days = 1; days < 366 ; days++) {
            BigDecimal reduced = Currency.Demurrage.applyRate(amount, rate, TimeUnit.DAYS, days);
            System.out.printf("After %3d days is %s%n",  days, reduced);
        }

    }
}
