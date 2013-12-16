package com.ripple.client;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONBuilder {
    public JSONObject json;

    public JSONBuilder() {
        json = new JSONObject();
    }

    public static JSONBuilder build() {
        return new JSONBuilder();
    }
    public JSONObject finish() {
        return json;
    }

    public JSONBuilder set(String k, Object v) {
        try {
            json.put(k, v);
            return this;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    public JSONBuilder load_base(Object v) { return set("load_base", v); }
    public JSONBuilder ledger_hash(Object v) { return set("ledger_hash", v); }
    public JSONBuilder fee_ref(Object v) { return set("fee_ref", v); }
    public JSONBuilder validated_ledgers(Object v) { return set("validated_ledgers", v); }
    public JSONBuilder fee_base(Object v) { return set("fee_base", v); }
    public JSONBuilder load_factor(Object v) { return set("load_factor", v); }
    public JSONBuilder random(Object v) { return set("random", v); }
    public JSONBuilder ledger_time(Object v) { return set("ledger_time", v); }
    public JSONBuilder server_status(Object v) { return set("server_status", v); }
    public JSONBuilder full(Object v) { return set("full", v); }
    public JSONBuilder reserve_base(Object v) { return set("reserve_base", v); }
    public JSONBuilder reserve_inc(Object v) { return set("reserve_inc", v); }
    public JSONBuilder ledger_index(Object v) { return set("ledger_index", v); }
}
