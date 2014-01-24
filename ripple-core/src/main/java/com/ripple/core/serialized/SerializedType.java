package com.ripple.core.serialized;

import org.json.JSONArray;
import org.json.JSONObject;

public interface SerializedType {
    Object toJSON();
    JSONArray toJSONArray();
    JSONObject toJSONObject();
    byte[] toWireBytes();
    String toWireHex();
    void toBytesList(BytesList to);
}
