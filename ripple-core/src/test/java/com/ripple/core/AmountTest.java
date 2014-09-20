package com.ripple.core;

import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.encodings.base58.EncodingFormatException;
import org.json.JSONObject;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.Assert.*;

public class AmountTest {
    public String rootAddress = TestFixtures.master_seed_address;
    Amount.Translator amounts = Amount.translate;

    static {
        AccountID.addAliasFromPassPhrase("root", "masterpassphrase");
        AccountID.addAliasFromPassPhrase("bob", "bob");
    }

    @Test
    public void testXRPFromMantissaBytes() throws Exception {
        byte[] mantissa = new UInt32(99000001).toByteArray();
        BigDecimal bigDecimal = Amount.xrpFromDropsMantissa(mantissa, 1);
        assertEquals("99000001", bigDecimal.scaleByPowerOfTen(6).toPlainString());
    }

    @Test
    public void testAmountSerializations() throws Exception {
        rehydrationTest(amt("1/USD/bob"));
        rehydrationTest(amt("1"));
        rehydrationTest(amt("10000"));
        rehydrationTest(amt("9999999999999999"));
        rehydrationTest(amt("-9999999999999999"));
        rehydrationTest(amt("-1/USD/bob"));
        rehydrationTest(amt("-1"));
        rehydrationTest(amt("-10000"));
        rehydrationTest(amt("-0.0001"));
        rehydrationTest(amt("-0.000001"));
        rehydrationTest(amt("0.0001"));
        rehydrationTest(amt("0.0001/USD/bob"));
        rehydrationTest(amt("0.0000000000000001/USD/bob"));
        rehydrationTest(amt("-0.1234567890123456/USD/bob"));
        rehydrationTest(amt("0.1234567890123456/USD/bob"));
        rehydrationTest(amt("-0.0001/USD/bob"));

    }

    @Test(expected = Amount.PrecisionError.class)
    public void testBlowup() throws Exception {
        rehydrationTest(amt("-0.12345678901234567/USD/bob"));
    }

    private void rehydrationTest(Amount amt) {
        assertEquals(amt, driedWet(amt));
    }

    private Amount driedWet(Amount amt) {
        String hex = amounts.toHex(amt);
        return amounts.fromHex(hex);
    }

    @Test
    public void testFunkyCurrencies() throws Exception {
        String amtJSON = "{\"currency\": \"015841551A748AD23FEFFFFFFFEA028000000000\"," +
                "\"issuer\": \"rM1oqKtfh1zgjdAgbFmaRm3btfGBX25xVo\"," +
                "\"value\": \"1000\"}";

        Amount amt = amounts.fromJSONObject(new JSONObject(amtJSON));
        String expected = "D5438D7EA4C68000015841551A748AD23FEFFFFFFFEA028000000000E4FE687C90257D3D2D694C8531CDEECBE84F3367";
        String hex = amt.toHex();
        assertEquals(expected, hex);
    }

    @Test
    public void testAbs() throws Exception {
        assertEquals(amt("-11").abs(), amt("11"));
    }

    @Test
    public void testSubtraction() throws Exception {
        assertEquals(amt("10"), amt("11").subtract(Amount.fromString("0.000001")));
        assertEquals(amt("10"), amt("11").subtract(new BigDecimal("0.000001")));
    }

    @Test
    public void testSerializing0XRP() throws Exception {
        Amount amt = Amount.fromDropString("0");
        String s = amounts.toHex(amt);
        assertEquals("4000000000000000", s);
        assertEquals(Amount.BINARY_FLAG_IS_NON_NEGATIVE_NATIVE.toString(16), s);
    }

    @Test
    public void testSerializingNegativeIOU() throws Exception {
        String json = "{\"currency\": \"USD\", \"issuer\": \"rrrrrrrrrrrrrrrrrrrrBZbvji\", \"value\": \"-99.2643419677474\"}";

        Amount amount = amounts.fromJSONObject(new JSONObject(json));
        String hex = amounts.toHex(amount);

        int offset = amount.exponent();
        assertEquals(-14, offset);
        assertTrue(amount.isNegative());
        assertFalse(amount.isNative());

        String expectedHex = "94E3440A102F5F5400000000000000000000000055534400000000000000000000000000000000000000000000000001";

        assertEquals(expectedHex, hex);

    }

    @Test
    public void testXRPIOULegacySupport() throws Exception {
        String json =  "{\n" +
                        "  \"currency\": \"0000000000000000000000005852500000000000\",\n" +
                        "  \"issuer\": \"rrrrrrrrrrrrrrrrrrrrBZbvji\",\n" +
                        "  \"value\": \"0\"\n" +
                        "}";

        Amount amount = amounts.fromJSONObject(new JSONObject(json));
        assertFalse(amount.isNative());

        JSONObject jsonObject = amounts.toJSONObject(amount);
        Amount rebuilt = amounts.fromJSONObject(jsonObject);
        assertEquals(amount, rebuilt);

        byte[] a1bytes = amounts.toBytes(amount);
        byte[] a2bytes = amounts.toBytes(rebuilt);

        boolean equals = Arrays.equals(a1bytes, a2bytes);
        assertTrue(equals);


        String legacy = "{\n" +
                "    \"currency\": \"0000000000000000000000005852500000000000\",\n" +
                "    \"issuer\": \"rrrrrrrrrrrrrrrrrrrrBZbvji\",\n" +
                                 //rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh
                "    \"value\": \"0\"\n" +
                "  }\n" +
                "  \n" +
                "";
        String expected_hex = "800000000000000000000000000000000000000058525000000000000000000000000000000000000000000000000001";

        Amount legacyAmount = amounts.fromJSONObject(new JSONObject(legacy));
        assertEquals(expected_hex, amounts.toHex(legacyAmount));

    }

    @Test
    public void test_Decimal_Parsing() {
        assertEquals(0, amt("1.0").compareTo(amt("1000000")));
        assertEquals(0, amt("1").compareTo(amt("0.000001")));
    }

    @Test(expected = Amount.PrecisionError.class)
    public void tests_Mother_Fucker_Do_You_Write_Them() {
        amt("-0.0001621621623423423234234234");
    }

    @Test
    public void test_Scale_Assumption() {
        assertScale("1.0",   0);
        assertScale("1.000", 0);
        assertScale("1", 0);
    }

    private void assertScale(String s, int i) {
        BigDecimal bd = new BigDecimal(s);
        bd = bd.stripTrailingZeros();
        assertEquals(bd.scale(), i);
    }

    @Test
    public void test_Decimal_Equality() {
        /*This is something to watch log for! Must delegate to compareTo() !*/
        assertFalse(decimal("1.0").equals(decimal("1.00")));
    }

    private BigDecimal decimal(String s) {
        return new BigDecimal(s);
    }

    @Test
    public void test_Offset() {
        assertOffset(".9999999999999999",  -16);
        assertOffset("9.999999999999999",  -15);
        assertOffset("99.99999999999999",  -14);
        assertOffset("999.9999999999999",  -13);
        assertOffset("9999.999999999999",  -12);
        assertOffset("99999.99999999999",  -11);
        assertOffset("999999.9999999999",  -10);
        assertOffset("9999999.999999999",  -9);
        assertOffset("99999999.99999999",  -8);
        assertOffset("999999999.9999999",  -7);
        assertOffset("9999999999.999999",  -6);
        assertOffset("99999999999.99999",  -5);
        assertOffset("999999999999.9999",  -4);
        assertOffset("9999999999999.999",  -3);
        assertOffset("99999999999999.99",  -2);
        assertOffset("999999999999999.9",  -1);

        assertOffset(".9",                 -16 );

        assertOffset("9",                  -15 );
        assertOffset("99",                 -14 );
        assertOffset("999",                -13 );
        assertOffset("9999",               -12 );
        assertOffset("99999",              -11 );
        assertOffset("999999",             -10 );
        assertOffset("9999999",            -9 );
        assertOffset("99999999",           -8 );
        assertOffset("999999999",          -7 );
        assertOffset("9999999999",         -6 );
        assertOffset("99999999999",        -5 );
        assertOffset("999999999999",       -4 );
        assertOffset("9999999999999",      -3 );
        assertOffset("99999999999999",     -2 );
        assertOffset("999999999999999",    -1 );
        assertOffset("9999999999999999",    0 );

    }

    @Test
    public void testRidiculousNess() throws Exception {
        Amount oneXRP = amt("1.0");
        Amount oneUSD = amt("1.0/USD");
        assertEquals(oneUSD, oneUSD.multiply(oneXRP));
    }

    private void assertOffset(String s, int i) {
        Amount amt = Amount.fromString(s + "/USD/" + TestFixtures.bob_account.address);
        assertEquals(String.format("Offset for %s should be %d", s, i),  i, amt.exponent());
    }

    public Amount a50 = amt("50/USD/root");
    public Amount b20 = amt("20/USD");
    public Amount c5  = amt("5/USD");

    @Test
    public void test_Division() {
        Amount d = a50.divide(b20).multiply(c5);
        assertEquals("50/USD/rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", a50.stringRepr());
        assertEquals("12.5/USD/rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", d.stringRepr());
    }

    @Test
    public void test_Addition() {
        Amount d = a50.add(b20).add(c5);
        assertEquals("75/USD/rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", d.stringRepr());
        assertEquals("80/USD/rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", d.add(new BigInteger("5")).stringRepr());
        assertEquals("80/USD/rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", d.add(5).stringRepr());
    }

    @Test
    public void test_Min_Max() {
        Amount d75 = a50.add(b20).add(c5);

        assertEquals(a50.min(b20), b20);
        assertEquals(b20.min(c5), c5);
        assertEquals(b20.max(d75), d75);
        assertEquals(b20.max(c5), b20);
        assertEquals(amt("-5/USD").max(c5), c5);
    }

    @Test
    public void test_Equals() {
        assertTrue(a50.equals(amt("50/USD/root")));
        assertFalse(a50.equals(amt("50/USD/bob")));
        assertTrue(a50.equalsExceptIssuer(amt("50/USD/bob")));

    }

    private static Amount amt(String s) {
        return Amount.fromString(s);
    }

    @Test
    public void test_IOUParsing() {
        assertEquals("USD", amt("1.0/USD").currencyString());
        Amount amount = amt("1.0/USD/" + TestFixtures.master_seed_address);
        assertEquals("USD", amount.currencyString());
        assertEquals(TestFixtures.master_seed_address, amount.issuerString());
        assertEquals(false, amount.isNative());
    }

    @Test(expected = EncodingFormatException.class)
    public void test_IOUIssuer_Validation() {
        amt("1.0/USD/" + TestFixtures.root_account + "F");
    }

    @Test(expected = RuntimeException.class)
    public void test_Check_Whole() {
        Amount.checkDropsValueWhole("1.0");
    }


    @Test
    public void test_Zero_USDEquals_Zero_USD() {
        Amount a = amt("0/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        Amount b = amt("0/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        assertTrue(a.equals(b));
    }
    @Test
    public void test_Zero_USDEquals_Negative_Zero_USD() {
        Amount a = amt("0/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        Amount b = amt("-0/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        assertTrue(a.equals(b));
    }
    @Test
    public void test_Zero_XRPEquals_Zero_XRP() {
        Amount a = amt("0");
        Amount b = amt("0.0");
        assertTrue(a.equals(b));
    }
    @Test
    public void test_Zero_XRPEquals_Negative_Zero_XRP() {
        Amount a = amt("0");
        Amount b = amt("-0");
        assertTrue(a.equals(b));
    }
    @Test
    public void test_10_USD_Equals_10_USD() {
        Amount a = amt("10/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        Amount b = amt("10/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        assertTrue(a.equals(b));
    }
    @Test
    public void test_Equality_Of_USD_with_Fraction() {
        Amount a = amt("123.4567/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        Amount b = amt("123.4567/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        assertTrue(a.equals(b));
    }
    @Test
    public void test_10_Drops_Equals_10_Drops() {
        Amount a = amt("10");
        Amount b = amt("10");
        assertTrue(a.equals(b));
    }
    @Test
    public void test_Fractional_XRP_Equality() {
        Amount a = amt("1.1");
        Amount b = amt("11.0").divide(10);
        assertTrue(a.equals(b));
    }
    @Test
    public void test_Equality_Ignoring_Issuer() {
        Amount a = amt("0/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        Amount b = amt("0/USD/rH5aWQJ4R7v4Mpyf4kDBUvDFT5cbpFq3XP");
        assertTrue(a.equalsExceptIssuer(b));
    }
    @Test
    public void test_Trailing_Zeros_Equality_Ignoring_Issuer() {
        Amount a = amt("1.1/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        Amount b = amt("1.10/USD/rH5aWQJ4R7v4Mpyf4kDBUvDFT5cbpFq3XP");
        assertTrue(a.equalsExceptIssuer(b));
    }
    @Test
    public void test_IOU_Exponent_Mismatch() {
        Amount a = amt("10/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        Amount b = amt("100/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        assertFalse(a.equals(b));
    }
    @Test
    public void test_XRP_Exponent_Mismatch() {
        Amount a = amt("10");
        Amount b = amt("100");
        assertFalse(a.equals(b));
    }
    @Test
    public void test_Mantissa_Mismatch_One_IOU_Not_Equaling_Two() {
        Amount a = amt("1/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        Amount b = amt("2/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        assertFalse(a.equals(b));
    }
    @Test
    public void test_Mantissa_Mismatch_One_XRP_Not_Equaling_Two() {
        Amount a = amt("1");
        Amount b = amt("2");
        assertFalse(a.equals(b));
    }
    @Test
    public void test_Mantissa_Mismatch_Fractional_IOU() {
        Amount a = amt("0.1/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        Amount b = amt("0.2/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        assertFalse(a.equals(b));
    }
    // Sign mismatch
    @Test
    public void test_Negativity_In_Equality_For_IOU() {
        Amount a = amt("1/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        Amount b = amt("-1/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        assertFalse(a.equals(b));
    }
    @Test
    public void test_Negativity_In_Equality_For_XRP() {
        Amount a = amt("1");
        Amount b = amt("-1");
        assertFalse(a.equals(b));
    }
    @Test
    public void test_Issuer_Derived_Inequality() {
        Amount a = amt("1/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        Amount b = amt("1/USD/rH5aWQJ4R7v4Mpyf4kDBUvDFT5cbpFq3XP");
        assertFalse(a.equals(b));
    }
    @Test
    public void test_Currency_Inequality() {
        Amount a = amt("1/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        Amount b = amt("1/EUR/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        assertFalse(a.equals(b));
    }
    @Test
    public void test_Same_Value_Yet_Native_Vs_IOU_Inequality() {
        Amount a = amt("1/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        Amount b = amt("1");
        assertFalse(a.equals(b));
    }
    @Test
    public void test_Same_Value_Yet_Native_Vs_IOU_Inequality_Operand_Switch() {
        Amount a = amt("1");
        Amount b = amt("1/USD/rNDKeo9RrCiRdfsMG8AdoZvNZxHASGzbZL");
        assertFalse(a.equals(b));
    }

    @Test
    public void test_Negate_native_123() {
        assertEquals("-0.000123/XRP", amt("123").negate().toTextFull());
    }
    @Test
    public void test_Negate_native_123_2() {
        assertEquals("0.000123/XRP", amt("-123").negate().toTextFull());
    }
    @Test
    public void test_Negate_non_native_123() {
        assertEquals("-123/USD/" + rootAddress, amt("123/USD/root").negate().toTextFull());
    }
    @Test
    public void test_Negate_non_native_123_2() {
        assertEquals("123/USD/" + rootAddress, amt("-123/USD/root").negate().toTextFull());
    }
    //    @Test
//    public void test_Clone_non_native_123_3() {
//        assertEquals("-123/USD/" + rootAddress, amt("-123/USD/root").clone().toTextFull());
//    }
    @Test
    public void test_Add_XRP_to_XRP() {
        assertEquals("0.0002/XRP", amt("150").add(amt("50")).toTextFull());
    }
    @Test
    public void test_Add_USD_to_USD() {
        assertEquals("200.52/USD/" + rootAddress, amt("150.02/USD/root").add(amt("50.5/USD/root")).toTextFull());
    }
    @Test
    public void test_Multiply_0_XRP_with_0_XRP() {
        assertEquals("0/XRP", amt("0").multiply(amt("0")).toTextFull());
    }
    @Test
    public void test_Multiply_0_USD_with_0_XRP() {
        assertEquals("0/USD/" + rootAddress, amt("0/USD/root").multiply(amt("0")).toTextFull());
    }
    @Test
    public void test_Multiply_0_XRP_with_0_USD() {
        assertEquals("0/XRP", amt("0").multiply(amt("0/USD/root")).toTextFull());
    }
    @Test
    public void test_Multiply_1_XRP_with_0_XRP() {
        assertEquals("0/XRP", amt("1").multiply(amt("0")).toTextFull());
    }
    @Test
    public void test_Multiply_1_USD_with_0_XRP() {
        assertEquals("0/USD/" + rootAddress, amt("1/USD/root").multiply(amt("0")).toTextFull());
    }
    @Test
    public void test_Multiply_1_XRP_with_0_USD() {
        assertEquals("0/XRP", amt("1").multiply(amt("0/USD/root")).toTextFull());
    }
    @Test
    public void test_Multiply_0_XRP_with_1_XRP() {
        assertEquals("0/XRP", amt("0").multiply(amt("1")).toTextFull());
    }
    @Test
    public void test_Multiply_0_USD_with_1_XRP() {
        assertEquals("0/USD/" + rootAddress, amt("0/USD/root").multiply(amt("1")).toTextFull());
    }
    @Test
    public void test_Multiply_0_XRP_with_1_USD() {
        assertEquals("0/XRP", amt("0").multiply(amt("1/USD/root")).toTextFull());
    }
    @Test
    public void test_Multiply_XRP_with_USD() {
        assertEquals("2000/XRP", amt("200.0").multiply(amt("10/USD/root")).toTextFull());
    }
    @Test
    public void test_Multiply_XRP_with_USD2() {
        assertEquals("0.2/XRP", amt("20000").multiply(amt("10/USD/root")).toTextFull());
    }
    @Test
    public void test_Multiply_XRP_with_USD3() {
        assertEquals("20/XRP", amt("2000000").multiply(amt("10/USD/root")).toTextFull());
    }
    @Test
    public void test_Multiply_XRP_with_USD_neg() {
        assertEquals("-0.002/XRP", amt("200").multiply(amt("-10/USD/root")).toTextFull());
    }
    @Test
    public void test_Multiply_XRP_with_USD_neg_frac() {
        assertEquals("-0.222/XRP", amt("-0.006").multiply(amt("37/USD/root")).toTextFull());
    }
    @Test
    public void test_Multiply_USD_with_USD() {
        assertEquals("20000/USD/" + rootAddress, amt("2000/USD/root").multiply(amt("10/USD/root")).toTextFull());
    }
    @Test
    public void test_Multiply_USD_with_USD2() {
        assertEquals("200000000000/USD/" + rootAddress, amt("2000000/USD/root").multiply(amt("100000/USD/root")).toTextFull());
    }
    @Test
    public void test_Multiply_EUR_with_USD_result_1() {
        assertEquals("100000/EUR/" + rootAddress, amt("100/EUR/root").multiply(amt("1000/USD/root")).toTextFull());
    }
    @Test
    public void test_Multiply_EUR_with_USD_neg() {
        assertEquals("-48000000/EUR/" + rootAddress, amt("-24000/EUR/root").multiply(amt("2000/USD/root")).toTextFull());
    }
    @Test
    public void test_Multiply_EUR_with_USD_neg_1() {
        assertEquals("-100/EUR/" + rootAddress, amt("0.1/EUR/root").multiply(amt("-1000/USD/root")).toTextFull());
    }
    @Test
    public void test_Multiply_EUR_with_XRP_factor_1() {
        assertEquals("0.0001/EUR/" + rootAddress, amt("0.05/EUR/root").multiply(amt("0.002000")).toTextFull());
    }
    @Test
    public void test_Multiply_EUR_with_XRP_neg() {
        assertEquals("-0.0005/EUR/" + rootAddress, amt("-100/EUR/root").multiply(amt("0.000005")).toTextFull());
    }
    @Test
    public void test_Multiply_EUR_with_XRP_neg_1() {
        assertEquals("-0.0001/EUR/" + rootAddress, amt("-0.05/EUR/root").multiply(amt("0.002000")).toTextFull());
    }
    @Test
    public void test_Multiply_XRP_with_XRP() {
        // This is actually too small for XRP so is rounded into nothingness
        // TODO, rounding values that are inside bound extremes seems fine
        // but rounding to nothingness ?? Should that blow up ??
        assertEquals("0/XRP", amt("10").multiply(amt("10")).toTextFull());
    }
    @Test
    public void test_Divide_XRP_by_USD() {
        assertEquals("0.00002/XRP", amt("200").divide(amt("10/USD/root")).toTextFull());
    }
    @Test
    public void test_Divide_XRP_by_USD2() {
        assertEquals("0.002/XRP", amt("20000").divide(amt("10/USD/root")).toTextFull());
    }
    @Test
    public void test_Divide_XRP_by_USD3() {
        assertEquals("0.2/XRP", amt("2000000").divide(amt("10/USD/root")).toTextFull());
    }
    @Test
    public void test_Divide_XRP_by_USD_neg() {
        assertEquals("-0.00002/XRP", amt("200").divide(amt("-10/USD/root")).toTextFull());
    }
    @Test
    public void test_Divide_XRP_by_USD_neg_frac() {
        assertEquals("-0.000162/XRP", amt("-6000").divide(amt("37/USD/root")).toTextFull());
    }
    @Test
    public void test_Divide_USD_by_USD() {
        assertEquals("200/USD/" + rootAddress, amt("2000/USD/root").divide(amt("10/USD/root")).toTextFull());
    }
    @Test
    public void test_Divide_USD_by_USD_fractional() {
        assertEquals("57142.85714285714/USD/" + rootAddress, amt("2000000/USD/root").divide(amt("35/USD/root")).toTextFull());
    }
    @Test
    public void test_Divide_USD_by_USD2() {
        assertEquals("20/USD/" + rootAddress, amt("2000000/USD/root").divide(amt("100000/USD/root")).toTextFull());
    }
    @Test
    public void test_Divide_EUR_by_USD_factor_1() {
        assertEquals("0.1/EUR/" + rootAddress, amt("100/EUR/root").divide(amt("1000/USD/root")).toTextFull());
    }
    @Test
    public void test_Divide_EUR_by_USD_neg() {
        assertEquals("-12/EUR/" + rootAddress, amt("-24000/EUR/root").divide(amt("2000/USD/root")).toTextFull());
    }
    @Test
    public void test_Divide_EUR_by_USD_neg_1() {
        assertEquals("-0.1/EUR/" + rootAddress, amt("100/EUR/root").divide(amt("-1000/USD/root")).toTextFull());
    }
    @Test
    public void test_Divide_EUR_by_XRP_result_1() {
        assertEquals("50000/EUR/" + rootAddress, amt("100/EUR/root").divide(amt("0.002000")).toTextFull());
    }
    @Test
    public void test_Divide_EUR_by_XRP_neg() {
        assertEquals("-20000000/EUR/" + rootAddress, amt("-100/EUR/root").divide(amt("0.000005")).toTextFull());
    }
    @Test
    public void test_Divide_EUR_by_XRP_neg_1() {
        assertEquals("-50000/EUR/" + rootAddress, amt("-100/EUR/root").divide(amt("0.002000")).toTextFull());
    }

    @Test
    public void test_Parse_native_0(){
        assertEquals("0/XRP", amt("0").toTextFull());
    }
    @Test
    public void test_Parse_native_0_pt_0(){
        assertEquals("0/XRP", amt("0.0").toTextFull());
    }
    @Test
    public void test_Parse_native_negative_0(){
        assertEquals("0/XRP", amt("-0").toTextFull());
    }
    @Test
    public void test_Parse_native_negative_0_pt_0(){
        assertEquals("0/XRP", amt("-0.0").toTextFull());
    }
    @Test
    public void test_Parse_native_1000_drops(){
        assertEquals("0.001/XRP", amt("1000").toTextFull());
    }
    @Test
    public void test_Parse_native_12_pt_3(){
        assertEquals("12.3/XRP", amt("12.3").toTextFull());
    }
    @Test
    public void test_Parse_native__12_pt_3(){
        assertEquals("-12.3/XRP", amt("-12.3").toTextFull());
    }
    @Test
    public void test_Parse_123_trailing_pt_USD(){
        assertEquals("123/USD/rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", amt("123./USD/rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh").toTextFull());
    }
    @Test
    public void test_Parse_12300_USD(){
        assertEquals("12300/USD/rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", amt("12300/USD/rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh").toTextFull());
    }
    @Test
    public void test_Parse_12_pt_3_USD(){
        assertEquals("12.3/USD/rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", amt("12.3/USD/rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh").toTextFull());
    }
    @Test
    public void test_Parse_1_pt_23_with_trailing_00_USD(){
        assertEquals("1.23/USD/rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", amt("1.2300/USD/rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh").toTextFull());
    }
    @Test
    public void test_Parse_negative_0_USD(){
        assertEquals("0/USD/rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", amt("-0/USD/rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh").toTextFull());
    }
    @Test
    public void test_Parse__0_pt_0_USD(){
        assertEquals("0/USD/rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", amt("-0.0/USD/rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh").toTextFull());
    }
}
