package com.ripple.client.payments;

import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.PathSet;
import com.ripple.core.coretypes.hash.Hash256;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.TreeMap;

public class Alternatives extends ArrayList<Alternative> {
    TreeMap<Hash256, Alternative> altMap = new TreeMap<Hash256, Alternative>();
    public Alternatives(JSONArray array, Alternatives prior) {
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject alternativeJson = array.getJSONObject(i);
                Amount sourceAmount = Amount.translate.fromValue(alternativeJson.get("source_amount"));
                PathSet paths = PathSet.translate.fromJSONArray(alternativeJson.getJSONArray("paths_computed"));
                addRecyclingPrior(new Alternative(paths, sourceAmount), prior);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void addRecyclingPrior(Alternative object, Alternatives prior) {
        if (prior != null) {
            Alternative priorAlternative = altMap.get(object.hash);
            if (priorAlternative != null) {
                object = priorAlternative;
            }
        }
        super.add(object);
        altMap.put(object.hash, object);
    }
}
