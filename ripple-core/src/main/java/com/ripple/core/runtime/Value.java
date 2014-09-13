package com.ripple.core.runtime;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public enum Value {
    UNKNOWN,
    STRING,
    JSON_OBJECT,
    JSON_ARRAY,
    LIST,
    MAP,
    NUMBER,
    BYTE,
    DOUBLE,
    FLOAT,
    INTEGER,
    LONG,
    BYTE_ARRAY,
    SHORT, BOOLEAN;

    static public Value typeOf (Object object) {
        if (object instanceof String) {
            return STRING;
        }
        else if (object instanceof Number) {
            if (object instanceof Byte) {
                return BYTE;
            }
            else if (object instanceof Double) {
                return DOUBLE;
            }
            else if (object instanceof Float) {
                return FLOAT;
            }
            else if (object instanceof Integer) {
                return INTEGER;
            }
            else if (object instanceof Long) {
                return LONG;
            }
            else if (object instanceof Short) {
                return SHORT;
            }
            return NUMBER;
        }
        else if (object instanceof JSONObject) {
            return JSON_OBJECT;
        }
        else if (object instanceof JSONArray) {
            return JSON_ARRAY;
        }
        else if (object instanceof Map) {
            return MAP;
        }
        else if (object instanceof Boolean) {
            return BOOLEAN;
        }
        else if (object instanceof List) {
            return LIST;
        }
        else if (object instanceof byte[]) {
            return BYTE_ARRAY;
        }
        else {
            return UNKNOWN;
        }
    }
}
