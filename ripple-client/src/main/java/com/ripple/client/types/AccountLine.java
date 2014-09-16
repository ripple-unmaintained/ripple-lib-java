package com.ripple.client.types;

import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Currency;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class AccountLine {
    public Amount balance;
    public Amount limit_peer;
    public Amount limit;

    public Currency currency;

    public boolean freeze = false;
    public boolean freeze_peer = false;

    public boolean authorized = false;
    public boolean authorized_peer = false;

    public boolean no_ripple = false;
    public boolean no_ripple_peer = false;

    public int quality_in = 0;
    public int quality_out = 0;

    public static AccountLine fromJSON(AccountID orientedTo, JSONObject line) {
        AccountLine l = new AccountLine();
        try {

            AccountID peer = AccountID.fromAddress(line.getString("account"));

            BigDecimal balance = new BigDecimal(line.getString("balance"));
            BigDecimal limit = new BigDecimal(line.getString("limit"));
            BigDecimal limit_peer = new BigDecimal(line.getString("limit_peer"));

            l.currency = Currency.fromString(line.getString("currency"));
            l.balance = new Amount(balance, l.currency, peer);

            l.limit = new Amount(limit, l.currency, peer);
            l.limit_peer = new Amount(limit_peer, l.currency, orientedTo);

            l.freeze = line.optBoolean("freeze", false);
            l.freeze_peer = line.optBoolean("freeze_peer", false);

            l.authorized = line.optBoolean("authorized", false);
            l.authorized_peer = line.optBoolean("authorized_peer", false);

            l.no_ripple = line.optBoolean("no_ripple", false);
            l.no_ripple_peer = line.optBoolean("no_ripple_peer", false);

            l.quality_in = line.optInt("quality_in", 0);
            l.quality_out = line.optInt("quality_out", 0);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return l;
    }

}
