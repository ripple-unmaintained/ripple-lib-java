package com.ripple.core.serialized;

import com.ripple.core.runtime.Value;
import org.json.JSONArray;
import org.json.JSONObject;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
        throw new NotImplementedException();
    }

    public JSONArray toJSONArray(T obj) {
        throw new NotImplementedException();
    }

    public boolean toBoolean(T obj) {
        throw new NotImplementedException();
    }

    public long toLong(T obj) {
        throw new NotImplementedException();
    }

    public int toInteger(T obj) {
        throw new NotImplementedException();
    }

    public double toDouble(T obj) {
        throw new NotImplementedException();
    }

    public String toString(T obj) {
        throw new NotImplementedException();
    }

    public T fromJSONObject(JSONObject jsonObject) {
        throw new NotImplementedException();
    }

    public T fromJSONArray(JSONArray jsonArray) {
        throw new NotImplementedException();
    }

    public T fromBoolean(boolean aBoolean) {
        throw new NotImplementedException();
    }

    public T fromLong(long aLong) {
        throw new NotImplementedException();
    }

    public T fromInteger(int integer) {
        throw new NotImplementedException();
    }

    public T fromDouble(double aDouble) {
        throw new NotImplementedException();
    }

    public T fromString(String value) {
        throw new NotImplementedException();
    }

    public abstract T fromWireBytes(byte[] bytes);
    public abstract Object toJSON(T obj);
    public abstract byte[] toWireBytes(T obj);
}
