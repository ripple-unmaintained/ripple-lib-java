package com.ripple.client.payments;

import com.ripple.core.types.Amount;
import com.ripple.core.types.PathSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Alternatives extends ArrayList<Alternative> {
    public Alternatives(JSONArray array) {
        for (int i = 0; i < array.length(); i++) {

            try {
                JSONObject alternativeJson = array.getJSONObject(i);
                Amount sourceAmount = Amount.translate.fromValue(alternativeJson.get("source_amount"));
                PathSet paths = PathSet.translate.fromJSONArray(alternativeJson.getJSONArray("paths_computed"));
                add(new Alternative(paths, sourceAmount));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
