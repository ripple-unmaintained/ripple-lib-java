package com.ripple.core.coretypes;

import com.ripple.core.coretypes.hash.Hash160;
import org.json.JSONObject;

import java.math.BigDecimal;

/**
 * Represents a currency/issuer pair
 */
public class Issue implements Comparable<Issue> {

    public static final Issue XRP = fromString("XRP");
    final Currency currency;
    final AccountID issuer;

    public Issue(Currency currency, AccountID issuer) {
        this.currency = currency;
        this.issuer = issuer;
    }

    public static Issue fromString(String pair) {
        String[] split = pair.split("/");
        return fromStringPair(split);
    }

    private static Issue fromStringPair(String[] split) {
        if (split.length == 2) {
            return new Issue(Currency.fromString(split[0]), AccountID.fromString(split[1]));
        } else if (split[0].equals("XRP")) {
            return new Issue(Currency.XRP, AccountID.XRP_ISSUER);
        } else {
            throw new RuntimeException("Issue string must be XRP or $currency/$issuer");
        }
    }

    /**
     * See {@link com.ripple.core.fields.Field#TakerGetsCurrency}
     * See {@link com.ripple.core.fields.Field#TakerGetsIssuer}
     *
     * TODO: better handling of Taker(Gets|Pays)(Issuer|Curency)
     *       maybe special subclasses of AccountID / Currency
     *       respectively?
     */
    public static Issue from160s(Hash160 currency, Hash160 issuer) {
        return new Issue(new Currency(currency.bytes()),
                new AccountID(issuer.toBytes()));
    }

    public Currency currency() {
        return currency;
    }

    public AccountID issuer() {
        return issuer;
    }

    @Override
    public String toString() {
        if (isNative()) {
            return "XRP";
        } else {
            return String.format("%s/%s", currency, issuer);
        }
    }

    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        o.put("currency", currency);
        if (!isNative()) {
            o.put("issuer", issuer);
        }
        return o;
    }

    public Amount amount(BigDecimal value) {
        return new Amount(value, currency, issuer, isNative());
    }

    public boolean isNative() {
        return this == XRP || currency.equals(Currency.XRP);
    }

    public Amount amount(Number value) {
        return new Amount(BigDecimal.valueOf(value.doubleValue()), currency, issuer, isNative());
    }

    @Override
    public int compareTo(Issue o) {
        int ret = issuer.compareTo(o.issuer);
        if (ret != 0) {
            return ret;
        }
        ret = currency.compareTo(o.currency);
        return ret;
    }

    public Amount roundedAmount(BigDecimal amount) {
        return amount(Amount.roundValue(amount, isNative()));
    }
}
