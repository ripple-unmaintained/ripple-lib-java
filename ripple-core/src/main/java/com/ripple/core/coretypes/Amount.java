package com.ripple.core.coretypes;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.TypedFields;
import com.ripple.core.serialized.*;
import com.ripple.core.coretypes.uint.UInt64;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * In ripple, amounts are either XRP, the native currency, or an IOU of
 * a given currency as issued by a designated account.
 */
public class Amount extends Number implements SerializedType, Comparable<Amount>

{

    private static BigDecimal TAKER_PAYS_FOR_THAT_DAMN_OFFER = new BigDecimal("1000000000000.000100");

    /**
     * Thrown when an Amount is constructed with an invalid value
     */
    public static class PrecisionError extends RuntimeException {
        public PrecisionError(String s) {
            super(s);
        }
    }

    // For rounding/multiplying/dividing
    public static final MathContext MATH_CONTEXT = new MathContext(16, RoundingMode.HALF_UP);
    // The maximum amount of digits in mantissa of an IOU amount
    public static final int MAXIMUM_IOU_PRECISION = 16;
    // The smallest quantity of an XRP is a drop, 1 millionth of an XRP
    public static final int MAXIMUM_NATIVE_SCALE = 6;
    // Defines bounds for native amounts
    public static final BigDecimal MAX_NATIVE_VALUE = parseDecimal("100,000,000,000.0");
    public static final BigDecimal MIN_NATIVE_VALUE = parseDecimal("0.000,001");

    // These are flags used when serializing to binary form
    public static final UInt64 BINARY_FLAG_IS_IOU = new UInt64("8000000000000000", 16);
    public static final UInt64 BINARY_FLAG_IS_NON_NEGATIVE_NATIVE = new UInt64("4000000000000000", 16);

    public static final Amount ONE_XRP = fromString("1.0");

    // The quantity of XRP or Issue(currency/issuer pairing)
    // When native, the value unit is XRP, not drops.
    private BigDecimal value;
    private Currency currency;
    // If the currency is XRP
    private boolean isNative;
    // Normally, in the constructor of an Amount the value is checked
    // that it's scale/precision and quantity are correctly bounded.
    // If unbounded is true, these checks are skipped.
    // This is there for historical ledgers that contain amounts that
    // would now be considered malformed (in the sense of the transaction 
    // engine result class temMALFORMED)
    private boolean unbounded = false;
    // The ZERO account is used for specifying the issuer for native 
    // amounts. In practice the issuer is never used when an
    // amount is native.
    private AccountID issuer;

    // While internally the value is stored as a BigDecimal
    // the mantissa and offset, as per the binary
    // format can be computed.
    // The mantissa is computed lazily, then cached
    private UInt64 mantissa = null;
    // The offset is always calculated.
    private int offset;

    public Amount(BigDecimal value, Currency currency, AccountID issuer) {
        this(value, currency, issuer, false);
    }

    public Amount(BigDecimal value) {
        isNative = true;
        currency = Currency.XRP;
        this.setAndCheckValue(value);
    }

    public Amount(BigDecimal value, Currency currency, AccountID issuer, boolean isNative, boolean unbounded) {
        this.isNative = isNative;
        this.currency = currency;
        this.unbounded = unbounded;
        this.setAndCheckValue(value);
        // done AFTER set value which sets some default values
        this.issuer = issuer;
    }

    // Private constructors
    Amount(BigDecimal newValue, Currency currency, AccountID issuer, boolean isNative) {
        this(newValue, currency, issuer, isNative, false);
    }

    private Amount(BigDecimal value, String currency, String issuer) {
        this(value, currency);
        if (issuer != null) {
            this.issuer = AccountID.fromString(issuer);
        }
    }

    private Amount(BigDecimal value, String currency) {
        isNative = false;
        this.currency = Currency.fromString(currency);
        this.setAndCheckValue(value);
    }

    private void setAndCheckValue(BigDecimal value) {
        this.value = value.stripTrailingZeros();
        initialize();
    }

    private void initialize() {
        if (isNative()) {
            issuer = AccountID.ZERO;
            if (!unbounded) {
                checkXRPBounds(value);
            }
            // Offset is unused for native amounts
            offset = -6; // compared to drops.
        } else {
            if (value.precision() > MAXIMUM_IOU_PRECISION && !unbounded) {
                throw new PrecisionError("Overflow Error!");
            }
            issuer = AccountID.ONE;
            offset = calculateOffset();
        }
    }

    private Amount newValue(BigDecimal newValue) {
        return newValue(newValue, false, false);
    }

    private Amount newValue(BigDecimal newValue, boolean round, boolean unbounded) {
        if (round) {
            newValue = roundValue(newValue, isNative);
        }
        return new Amount(newValue, currency, issuer, isNative, unbounded);
    }

    private Amount newValue(BigDecimal val, boolean round) {
        return newValue(val, round, false);
    }

    /* Getters and Setters */

    public BigDecimal value() {
        return value;
    }

    public Currency currency() {
        return currency;
    }

    public AccountID issuer() {
        return issuer;
    }

    public Issue issue() {
        // TODO: store the currency and issuer as an Issue
        return new Issue(currency, issuer);
    }

    public UInt64 mantissa() {
        if (mantissa == null) {
            mantissa = calculateMantissa();
        }
        return mantissa;
    }

    public int offset() {
        return offset;
    }

    public boolean isNative() {
        return isNative;
    }

    public String currencyString() {
        return currency.toString();
    }

    public String issuerString() {
        if (issuer == null) {
            return "";
        }
        return issuer.toString();
    }

    /* Offset & Mantissa Helpers */

    /**
     * @return a positive value for the mantissa
     */
    private UInt64 calculateMantissa() {
        if (isNative()) {
            return new UInt64(bigIntegerDrops().abs());
        } else {
            return new UInt64(bigIntegerIOUMantissa());
        }
    }

    protected int calculateOffset() {
        return -MAXIMUM_IOU_PRECISION + value.precision() - value.scale();
    }

    public BigInteger bigIntegerIOUMantissa() {
        return exactBigIntegerScaledByPowerOfTen(-offset).abs();
    }

    private BigInteger bigIntegerDrops() {
        return exactBigIntegerScaledByPowerOfTen(MAXIMUM_NATIVE_SCALE);
    }

    private BigInteger exactBigIntegerScaledByPowerOfTen(int n) {
        return value.scaleByPowerOfTen(n).toBigIntegerExact();
    }

    /* Equality testing */

    private boolean equalValue(Amount amt) {
        return compareTo(amt) == 0;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Amount) {
            return equals((Amount) obj);
        }
        return super.equals(obj);
    }

    public boolean equals(Amount amt) {
        return equalValue(amt) &&
                currency.equals(amt.currency) &&
                (isNative() || issuer.equals(amt.issuer));
    }

    public boolean equalsExceptIssuer(Amount amt) {
        return equalValue(amt) &&
                currencyString().equals(amt.currencyString());
    }



    public int compareTo(Amount amount) {
        return value.compareTo(amount.value);
    }

    public boolean isZero() {
        return value.signum() == 0;
    }

    public boolean isNegative() {
        return value.signum() == -1;
    }

    // Maybe you want !isNegative()
    // Any amount that !isNegative() isn't necessarily positive
    // Is a zero amount strictly positive? no
    private boolean isPositive() {
        return value.signum() == 1;
    }

    /**

    Arithmetic Operations

    There's no checking if an amount is of a different currency/issuer.
    
    All operations return amounts of the same currency/issuer as the
    first operand.

    eg.

        amountOne.add(amountTwo)

        The currency/issuer of the resultant amount, is that of `amountOne`
    
    Divide and multiply are equivalent to the javascript ripple-lib
    ratio_human and product_human.

    */
    public Amount add(BigDecimal augend) {
        return newValue(value.add(augend), true);
    }

    public Amount add(Amount augend) {
        return add(augend.value);
    }

    public Amount add(Number augend) {
        return add(BigDecimal.valueOf(augend.doubleValue()));
    }

    public Amount subtract(BigDecimal subtrahend) {
        return newValue(value.subtract(subtrahend), true);
    }

    public Amount subtract(Amount subtrahend) {
        return subtract(subtrahend.value);
    }

    public Amount subtract(Number subtrahend) {
        return subtract(BigDecimal.valueOf(subtrahend.doubleValue()));
    }

    public Amount multiply(BigDecimal divisor) {
        return newValue(value.multiply(divisor, MATH_CONTEXT), true);
    }

    public Amount multiply(Amount multiplicand) {
        return multiply(multiplicand.value);
    }

    public Amount multiply(Number multiplicand) {
        return multiply(BigDecimal.valueOf(multiplicand.doubleValue()));
    }

    public Amount divide(BigDecimal divisor) {
        return newValue(value.divide(divisor, MATH_CONTEXT), true);
    }

    public Amount divide(Amount divisor) {
        return divide(divisor.value);
    }

    public Amount divide(Number divisor) {
        return divide(BigDecimal.valueOf(divisor.doubleValue()));
    }

    public Amount negate() {
        return newValue(value.negate());
    }

    public Amount abs() {
        return newValue(value.abs());
    }
    public Amount min(Amount val) {
        return (compareTo(val) <= 0 ? this : val);
    }
    public Amount max(Amount val) {
        return (compareTo(val) >= 0 ? this : val);
    }

    /* Offer related helpers */
    public BigDecimal computeQuality(Amount toExchangeThisWith) {
        return value.divide(toExchangeThisWith.value, MathContext.DECIMAL128);
    }
    /**
     * @return Amount
     * The real native unit is a drop, one million of which are an XRP.
     * We want `one` unit at XRP scale (1e6 drops), or if it's an IOU,
     * just `one`.
     */
    public Amount one() {
        if (isNative()) {
            return ONE_XRP;
        } else {
            return issue().amount(1);
        }
    }

    /* Serialized Type implementation */

    @Override
    public Object toJSON() {
        if (isNative()) {
            return toDropsString();
        } else {
            return toJSONObject();
        }
    }

    public JSONObject toJSONObject() {
        try {
            JSONObject out = new JSONObject();
            out.put("currency", currencyString());
            out.put("value", valueText());
            out.put("issuer", issuerString());
            return out;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] toBytes() {
        return translate.toBytes(this);
    }

    @Override
    public String toHex() {
        return translate.toHex(this);
    }

    @Override
    public void toBytesSink(BytesSink to) {
        UInt64 man = mantissa();

        if (isNative()) {
            if (!isNegative()) {
                man = man.or(BINARY_FLAG_IS_NON_NEGATIVE_NATIVE);
            }
            to.add(man.toByteArray());
        } else {
            int offset = offset();
            UInt64 packed;

            if (isZero()) {
                packed = BINARY_FLAG_IS_IOU;
            } else if (isNegative()) {
                packed = man.or(new UInt64(512 + 0 + 97 + offset).shiftLeft(64 - 10));
            } else {
                packed = man.or(new UInt64(512 + 256 + 97 + offset).shiftLeft(64 - 10));
            }

            to.add(packed.toByteArray());
            to.add(currency.bytes());
            to.add(issuer.bytes());
        }
    }

    public static class Translator extends TypeTranslator<Amount> {
        @Override
        public Amount fromString(String s) {
            return com.ripple.core.coretypes.Amount.fromString(s);
        }

        @Override
        public Amount fromParser(BinaryParser parser, Integer hint) {
            BigDecimal value;
            byte[] mantissa = parser.read(8);
            byte b1 = mantissa[0], b2 = mantissa[1];

            boolean isIOU = (b1 & 0x80) != 0;
            boolean isPositive = (b1 & 0x40) != 0;
            int sign = isPositive ? 1 : -1;

            if (isIOU) {
                mantissa[0] = 0;
                Currency curr = Currency.translate.fromParser(parser);
                AccountID issuer = AccountID.translate.fromParser(parser);
                int offset = ((b1 & 0x3F) << 2) + ((b2 & 0xff) >> 6) - 97;
                mantissa[1] &= 0x3F;

                value = new BigDecimal(new BigInteger(sign, mantissa), -offset);
                return new Amount(value, curr, issuer, false);
            } else {
                mantissa[0] &= 0x3F;
                value = xrpFromDropsMantissa(mantissa, sign);
                return new Amount(value);
            }
        }

        @Override
        public String toString(com.ripple.core.coretypes.Amount obj) {
            return obj.stringRepr();
        }

        @Override
        public JSONObject toJSONObject(Amount obj) {
            return obj.toJSONObject();
        }

        @Override
        public Amount fromJSONObject(JSONObject jsonObject) {
            try {
                String valueString = jsonObject.getString("value");
                String issuerString = jsonObject.getString("issuer");
                String currencyString = jsonObject.getString("currency");
                return new Amount(new BigDecimal(valueString), currencyString, issuerString);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
    static public Translator translate = new Translator();

    public static BigDecimal xrpFromDropsMantissa(byte[] mantissa, int sign) {
        return new BigDecimal(new BigInteger(sign, mantissa), 6);
    }

    /* Number overides */
    @Override
    public int intValue() {
        return value.intValueExact();
    }

    @Override
    public long longValue() {
        return value.longValueExact();
    }

    @Override
    public float floatValue() {
        return value.floatValue();
    }

    @Override
    public double doubleValue() {
        return value.doubleValue();
    }

    public BigInteger bigIntegerValue() {
        return value.toBigIntegerExact();
    }

    public Amount newIssuer(AccountID issuer) {
        return new Amount(value, currency, issuer);
    }

    // Static constructors
    public static Amount fromString(String val) {
        if (val.contains("/")) {
            return fromIOUString(val);
        } else if (val.contains(".")) {
            return fromXrpString(val);
        } else {
            return fromDropString(val);
        }
    }

    public static Amount fromDropString(String val) {
        BigDecimal drops = new BigDecimal(val).scaleByPowerOfTen(-6);
        checkDropsValueWhole(val);
        return new Amount(drops);
    }

    public static Amount fromIOUString(String val) {
        String[] split = val.split("/");
        if (split.length == 1) {
            throw new RuntimeException("IOU string must be in the form number/currencyString or number/currencyString/issuerString");
        } else if (split.length == 2) {
            return new Amount(new BigDecimal(split[0]), split[1]);
        } else {
            return new Amount(new BigDecimal(split[0]), split[1], split[2]);
        }
    }

    @Deprecated
    private static Amount fromXrpString(String valueString) {
        BigDecimal val = new BigDecimal(valueString);
        return new Amount(val);
    }

    /**
     * @return A String representation as used by ripple json format
     */
    public String stringRepr() {
        if (isNative()) {
            return toDropsString();
        } else {
            return iouTextFull();
        }
    }

    public String toDropsString() {
        if (!isNative()) {
            throw new RuntimeException("Amount is not native");
        }
        return bigIntegerDrops().toString();
    }

    private String iouText() {
        return String.format("%s/%s", valueText(), currencyString());
    }

    public String iouTextFull() {
        return String.format("%s/%s/%s", valueText(), currencyString(), issuerString());
    }

    public String toTextFull() {
        if (isNative()) {
            return nativeText();
        } else {
            return iouTextFull();
        }
    }

    public String nativeText() {
        return String.format("%s/XRP", valueText());
    }

    @Override
    public String toString() {
        return toTextFull();
    }

    public String toText() {
        if (isNative()) {
            return nativeText();
        } else {
            return iouText();
        }
    }

    /**
     * @return A String containing the value as a decimal number (in XRP scale)
     */
    public String valueText() {
        return value.signum() == 0 ? "0" : value().toPlainString();
    }

    public static void checkLowerDropBound(BigDecimal val) {
        if (val.scale() > 6) {
            throw getOutOfBoundsError(val, "bigger", MIN_NATIVE_VALUE);
        }
    }

    public static void checkUpperBound(BigDecimal val) {
        if (val.compareTo(MAX_NATIVE_VALUE) == 1) {
            throw getOutOfBoundsError(val, "bigger", MAX_NATIVE_VALUE);
        }
    }

    private static PrecisionError getOutOfBoundsError(BigDecimal abs, String sized, BigDecimal bound) {
        return new PrecisionError(abs.toPlainString() + " is " + sized + " than bound " + bound);
    }

    public static void checkXRPBounds(BigDecimal value) {
        // This is for that damn offer at index: 6310D78E6AD408892743DD62455694162E758DA283D0E4A2CB3A3C173B7C794A
        if (value.compareTo(TAKER_PAYS_FOR_THAT_DAMN_OFFER) == 0) {
            return;
        }
        value = value.abs();
        checkLowerDropBound(value);
        checkUpperBound(value);
    }

    public static void checkDropsValueWhole(String drops) {
        boolean contains = drops.contains(".");
        if (contains) {
            throw new RuntimeException("Drops string contains floating point is decimal");
        }
    }

    public static BigDecimal roundValue(BigDecimal value, boolean nativeSrc) {
        int i = value.precision() - value.scale();
        return value.setScale(nativeSrc ? MAXIMUM_NATIVE_SCALE :
                MAXIMUM_IOU_PRECISION - i,
                MATH_CONTEXT.getRoundingMode());
    }

    private static BigDecimal parseDecimal(String s) {
        return new BigDecimal(s.replace(",", "")); //# .scaleByPowerOfTen(6);
    }

    public static TypedFields.AmountField amountField(final Field f) {
        return new TypedFields.AmountField() {
            @Override
            public Field getField() {
                return f;
            }
        };
    }

    static public TypedFields.AmountField Amount = amountField(Field.Amount);
    static public TypedFields.AmountField Balance = amountField(Field.Balance);
    static public TypedFields.AmountField LimitAmount = amountField(Field.LimitAmount);
    static public TypedFields.AmountField DeliveredAmount = amountField(Field.DeliveredAmount);
    static public TypedFields.AmountField TakerPays = amountField(Field.TakerPays);
    static public TypedFields.AmountField TakerGets = amountField(Field.TakerGets);
    static public TypedFields.AmountField LowLimit = amountField(Field.LowLimit);
    static public TypedFields.AmountField HighLimit = amountField(Field.HighLimit);
    static public TypedFields.AmountField Fee = amountField(Field.Fee);
    static public TypedFields.AmountField SendMax = amountField(Field.SendMax);
    static public TypedFields.AmountField MinimumOffer = amountField(Field.MinimumOffer);
    static public TypedFields.AmountField RippleEscrow = amountField(Field.RippleEscrow);
    static public TypedFields.AmountField taker_gets_funded = amountField(Field.taker_gets_funded);
    static public TypedFields.AmountField taker_pays_funded = amountField(Field.taker_pays_funded);

}
