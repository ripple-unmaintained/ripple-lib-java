
package com.ripple.core.serialized;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ripple.core.runtime.Value;
import com.ripple.encodings.common.B16;

import java.util.Iterator;

/**
 * @param <T> The SerializedType class
 * TODO, this should only really have methods that each class over-rides
 *       it's currently pretty NASTY
 */
public abstract class TypeTranslator<T extends SerializedType> {

//    private static ObjectMapper objectMapper = new ObjectMapper();

/*    public static ArrayNode makeJacksonArray() {
        return objectMapper.createArrayNode();
    }
    public static ObjectNode makeJacksonObject() {
        return objectMapper.createObjectNode();
    }

    public static JsonNode makeJacksonNode(Object o, ContainerNode parent) {
        if (o instanceof JSONObject) {
            return convertJSONObject((JSONObject) o, parent);
        } else if (o instanceof JSONArray) {
            return convertArrayObject((JSONArray) o, parent);
        } else if (o instanceof Integer) {
            return parent.numberNode((Integer) o);
        } else if (o instanceof Long) {
            return parent.numberNode((Long) o);
        } else if (o instanceof Short) {
            return parent.numberNode((Short) o);
        } else if (o instanceof String) {
            return parent.textNode((String) o);
        } else {
            return parent.pojoNode(o);
        }
    }

    private static JsonNode convertArrayObject(JSONArray o, ContainerNode parent) {
        ArrayNode array = parent.arrayNode();
        for (int i = 0; i < o.length(); i++) {
            try {
                Object value = o.get(i);
                array.add(makeJacksonNode(value, array));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return array;
    }

    private static JsonNode convertJSONObject(JSONObject o, ContainerNode parent) {
        ObjectNode nodes = parent.objectNode();

        Iterator keys = o.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            try {
                Object value = o.get(key);
                nodes.put(key, makeJacksonNode(value, nodes));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
        return nodes*/;


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

    public String toString(T obj) {
        return obj.toString();
    }

    public T fromJSONObject(JSONObject jsonObject) {
        throw new UnsupportedOperationException();
    }

    public T fromJSONArray(JSONArray jsonArray) {
        throw new UnsupportedOperationException();
    }

    public T fromJacksonObject(ObjectNode jsonObject) {
        throw new UnsupportedOperationException();
    }

    public T fromJacksonArray(ArrayNode array) {
        throw new UnsupportedOperationException();
    }

    public T fromJackson(JsonNode node) {
        switch (node.getNodeType()) {
            case ARRAY:
                return fromJacksonArray((ArrayNode) node);
            case BINARY:
                return fromBytes(((BinaryNode) node).binaryValue());
            case BOOLEAN:
                return fromBoolean(node.asBoolean());
            case MISSING:
                throw new UnsupportedOperationException();
            case NULL:
                return null;
            case NUMBER:
                if (node.isIntegralNumber()) {
                    if (node.isLong()) {
                        return fromLong(node.asLong());
                    }
                    else {
                        return fromInteger(node.intValue());
                    }
                } else {
                    throw new UnsupportedOperationException();
                }
            case OBJECT:
                return fromJacksonObject((ObjectNode) node);
            case POJO:
                return fromValue(((POJONode) node).getPojo());
            case STRING:
                return fromString(node.asText());
            default:
                throw new UnsupportedOperationException();
        }
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

    public ObjectNode toJacksonObject(T obj) {
        throw new UnsupportedOperationException();
    }
    public ArrayNode toJacksonArray(T obj) {
        throw new UnsupportedOperationException();
    }

    public Object toJSON(T obj) {
        return obj.toJSON();
    }
    public JsonNode toJackson(T obj) {
        return obj.toJackson();
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
