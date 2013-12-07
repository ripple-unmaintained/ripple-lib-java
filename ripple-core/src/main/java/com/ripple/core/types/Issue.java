package com.ripple.core.types;

import java.math.BigDecimal;

/**
 * Represents a currency/issuer pair
 */
public class Issue {
    public static final Issue XRP = fromString("XRP");
    Currency currency;
    AccountID issuer;

    public Issue(Currency currency, AccountID issuer) {
        this.currency = currency;
        this.issuer = issuer;
    }

    public static Issue fromString(String pair) {
        String[] split = pair.split("/");
        return getIssue(split);
    }

    public static Issue getIssue(String[] split) {
        if (split.length == 2) {
            return new Issue(Currency.fromString(split[0]), AccountID.fromString(split[1]));
        }
        else if (split[0].equals("XRP")) {
            return new Issue(Currency.XRP, AccountID.ZERO);
        }  else {
            throw new RuntimeException("Issue string must be XRP or $currency/$issuer");
        }
    }

    public Currency currency() {
        return currency;
    }

    public AccountID issuer() {
        return issuer;
    }

    public Amount amount(BigDecimal value) {
        return new Amount(value, currency, issuer, this == XRP);
    }
    public Amount amount(Number value) {
        return new Amount(BigDecimal.valueOf(value.longValue()), currency, issuer, this == XRP);
    }
}
