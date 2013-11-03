package com.ripple.client.subscriptions;

import com.ripple.config.Config;
import com.ripple.core.enums.TransactionType;
import com.ripple.core.types.Amount;
import com.ripple.core.types.uint.UInt16;
import org.json.JSONObject;

public class ServerInfo {
    public boolean updated = false;

    public int fee_base;
    public int fee_ref;
    public int reserve_base;
    public int reserve_inc;
    public int load_base;
    public int load_factor;
    public int ledger_time;
    public int ledger_index;
    public String ledger_hash;
    public String random;
    public String server_status;
    public String validated_ledgers;

    public Amount computeFee(int units) {
        if (!updated) {
            throw new IllegalStateException("No information from the server yet");
        }

        double fee_unit = fee_base / fee_ref, fee;
        fee_unit *= load_factor / load_base;
        fee = units * fee_unit * Config.getFeeCushion();
        String s = String.valueOf((long) Math.ceil(fee));
        return Amount.fromString(s);
    }

    public Amount transactionFee() {
        return computeFee(fee_base);
    }

    public void update(JSONObject json) {
        // TODO, this might asking for trouble, just assuming certain fields, it should BLOW UP

        fee_base          = json.optInt(     "fee_base",          fee_base);
        fee_ref           = json.optInt(     "fee_ref",           fee_ref);
        reserve_base      = json.optInt(     "reserve_base",      reserve_base);
        reserve_inc       = json.optInt(     "reserve_inc",       reserve_inc);
        load_base         = json.optInt(     "load_base",         load_base);
        load_factor       = json.optInt(     "load_factor",       load_factor);
        ledger_time       = json.optInt(     "ledger_time",       ledger_time);
        ledger_index      = json.optInt(     "ledger_index",      ledger_index);
        ledger_hash       = json.optString(  "ledger_hash",       ledger_hash);
        validated_ledgers = json.optString(  "validated_ledgers", validated_ledgers);

        random            = json.optString(  "random",            random);
        server_status     = json.optString(  "server_status",     server_status);

        updated = true;
    }

    public boolean primed() {
        return updated;
    }

    public Amount transactionFee(UInt16 uInt16) {
        TransactionType tt = TransactionType.fromNumber(uInt16.intValue());
        return transactionFee();
    }
}

