package com.ripple.core.coretypes;

import org.json.JSONObject;

import java.text.MessageFormat;

public class IssuePair implements Comparable<IssuePair> {
    public final Issue pays;
    public final Issue gets;

    public IssuePair(Issue pays, Issue gets) {
        this.pays = pays;
        this.gets = gets;
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0}/{1}", pays, gets);
    }

    @Override
    public int compareTo(IssuePair o) {
        int cmp = pays.compareTo(o.pays);
        if (cmp == 0) {
            cmp = gets.compareTo(o.gets);
        }
        return cmp;
    }

    public JSONObject toJSON() {
        JSONObject ret = new JSONObject();
        ret.put("taker_gets", gets.toJSON());
        ret.put("taker_pays", pays.toJSON());
        return ret;
    }
}
