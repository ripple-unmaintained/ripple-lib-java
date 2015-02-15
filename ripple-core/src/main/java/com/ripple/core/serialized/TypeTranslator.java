
package com.ripple.core.serialized;

import com.ripple.core.runtime.Value;
import com.ripple.encodings.common.B16;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @param <T> The SerializedType class
 * TODO, this should only really have methods that each class over-rides
 *       it's currently pretty NASTY
 */
public abstract class TypeTranslator<T extends SerializedType> {

    @SuppressWarnings("unchecked")
    public T fromValue(Object object) {
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
            case BYTE_ARRAY:
                return fromBytes((byte[]) object);
            case UNKNOWN:
            default:
                return (T) object;
        }
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
        return obj.toString();
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

    /**
     * @param hint Using a boxed integer, allowing null for no hint
     */
    public abstract T fromParser(BinaryParser parser, Integer hint);

    public T fromParser(BinaryParser parser) {
        return fromParser(parser, null);
    }

    public T fromBytes(byte[] b) {
        return fromParser(new BinaryParser(b));
    }

    public T fromHex(String hex) {
        return fromBytes(B16.decode(hex));
    }

    public JSONObject toJSONObject(T obj) {
        throw new UnsupportedOperationException();
    }

    public JSONArray toJSONArray(T obj) {
        throw new UnsupportedOperationException();
    }
    public Object toJSON(T obj) {
        return obj.toJSON();
    }

    public void toBytesSink(T obj, BytesSink to) {
        obj.toBytesSink(to);
    }

    public byte[] toBytes(T obj) {
        BytesList to = new BytesList();
        toBytesSink(obj, to);
        return to.bytes();
    }

    public String toHex(T obj) {
        BytesList to = new BytesList();
        toBytesSink(obj, to);
        return to.bytesHex();
    }
}
