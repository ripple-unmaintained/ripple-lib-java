package com.ripple.android;

import org.json.JSONException;
import org.json.JSONObject;

public class JSON {
    public static JSONObject parseJSON(String s) {
        try {
            return new JSONObject(s);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static String prettyJSON(JSONObject jsonObject) {
        try {
            return jsonObject.toString(4);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
