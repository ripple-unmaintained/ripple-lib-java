package com.ripple.core.serialized;

import com.ripple.core.runtime.Value;
import org.json.JSONArray;
import org.json.JSONObject;
import java.lang.UnsupportedOperationException;

public abstract class TypeTranslator<T extends SerializedType> {

    @SuppressWarnings("unchecked")
    public <V> T fromValue(V object) {
//        try {
//            return (T) object;
//        } catch (ClassCastException ignored) {
//
//        }

        switch (Value.typeOf(object)) {
            case STRING:
                return fromString((String) object);
            case DOUBLE:
                return fromDouble((Double) object);
            case INTEGER:
                return fromInteger((Integer) object);
            case LONG:
                return fromLong((Long) object);
            case BOOLEAN:
                return fromBoolean((Boolean) object);
            case JSON_ARRAY:
                return fromJSONArray((JSONArray) object);
            case JSON_OBJECT:
                return fromJSONObject((JSONObject) object);
            case UNKNOWN:
            default:
                return (T) object;

        }
    }

    public JSONObject toJSONObject(T obj) {
        throw new UnsupportedOperationException();
    }

    public JSONArray toJSONArray(T obj) {
        throw new UnsupportedOperationException();
    }

    public boolean toBoolean(T obj) {
        throw new UnsupportedOperationException();
    }

    public long toLong(T obj) {
        throw new UnsupportedOperationException();
    }

    public int toInteger(T obj) {
        throw new UnsupportedOperationException();
    }

    public double toDouble(T obj) {
        throw new UnsupportedOperationException();
    }

    public String toString(T obj) {
        throw new UnsupportedOperationException();
    }

    public T fromJSONObject(JSONObject jsonObject) {
        throw new UnsupportedOperationException();
    }

    public T fromJSONArray(JSONArray jsonArray) {
        throw new UnsupportedOperationException();
    }

    public T fromBoolean(boolean aBoolean) {
        throw new UnsupportedOperationException();
    }

    public T fromLong(long aLong) {
        throw new UnsupportedOperationException();
    }

    public T fromInteger(int integer) {
        throw new UnsupportedOperationException();
    }

    public T fromDouble(double aDouble) {
        throw new UnsupportedOperationException();
    }

    public T fromString(String value) {
        throw new UnsupportedOperationException();
    }

    public abstract T fromWireBytes(byte[] bytes);
    public abstract Object toJSON(T obj);
    public abstract byte[] toWireBytes(T obj);
}
