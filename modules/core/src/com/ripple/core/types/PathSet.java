package com.ripple.core.types;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.HasField;
import com.ripple.core.serialized.ByteArrayList;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PathSet extends ArrayList<PathSet.Path> implements SerializedType {
    @Override
    public TypeTranslator translator() {
        return translate;
    }

    public static class Hop{
        AccountID account;
        AccountID issuer;
        String currency;
        int type;

        public static byte TYPE_ACCOUNT  = (byte) 0x01;
        public static byte TYPE_CURRENCY = (byte) 0x10;
        public static byte TYPE_ISSUER   = (byte) 0x20;

        public int getType() {
            if (type == 0) {
                synthesizeType();
            }
            return type;
        }

        static public Hop fromJSONObject(JSONObject json) {
            Hop hop = new Hop();
            try {
                if (json.has("account")) {
                    hop.account = AccountID.fromAddress(json.getString("account"));
                }
                if (json.has("issuer")) {
                    hop.issuer = AccountID.fromAddress(json.getString("issuer"));
                }
                if (json.has("currency")) {
                    hop.currency = json.getString("currency");
                }

                if (json.has("type")) {
                    hop.type = json.getInt("type");
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return hop;
        }

        public void synthesizeType() {
            type = 0;

            if (account != null) type |= TYPE_ACCOUNT;
            if (currency != null) type |= TYPE_CURRENCY;
            if (issuer != null) type |= TYPE_ISSUER;
        }

        public JSONObject toJSONObject() {
            JSONObject object = new JSONObject();
            try {
                object.put("type", getType());

                if (account  != null) object.put("account", AccountID.translate.toJSON(account));
                if (issuer   != null) object.put("issuer", AccountID.translate.toJSON(issuer));
                if (currency != null) object.put("currency", currency);

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return object;
        }
    }
    public static class Path extends ArrayList<Hop> {
        static public Path fromJSONArray(JSONArray array) {
            Path path = new Path();
            int nHops = array.length();
            for (int i = 0; i < nHops; i++) {
                try {
                    JSONObject hop = array.getJSONObject(i);
                    path.add(Hop.fromJSONObject(hop));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            return path;
        }
        public JSONArray toJSONArray() {
            JSONArray array = new JSONArray();
            for (Hop hop : this) {
                array.put(hop.toJSONObject());
            }
            return array;
        }
    }

    static class Translator extends TypeTranslator<PathSet> {
        @Override
        public PathSet fromWireBytes(byte[] bytes) {
            return null;
        }

        @Override
        public Object toJSON(PathSet obj) {
            return toJSONArray(obj);
        }

        @Override
        public JSONArray toJSONArray(PathSet pathSet) {
            JSONArray array = new JSONArray();
            for (Path path : pathSet) {
                array.put(path.toJSONArray());
            }
            return array;
        }

        @Override
        public PathSet fromJSONArray(JSONArray array) {
            PathSet paths = new PathSet();

            int nPaths = array.length();

            for (int i = 0; i < nPaths; i++) {
                try {
                    JSONArray path = array.getJSONArray(i);
                    paths.add(Path.fromJSONArray(path));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            return paths;
        }

        @Override
        public byte[] toWireBytes(PathSet obj) {
            ByteArrayList buffer = new ByteArrayList();
            byte typeBoundary = (byte) 0xff,
                      typeEnd = (byte) 0x00;

            int n = 0;
            for (Path hops: obj) {
                if (n++ != 0) {
                    buffer.add(typeBoundary);
                }
                for (Hop hop : hops) {
                    int type = hop.getType();
                    buffer.add((byte) type);
                    if (hop.account != null) {
                        buffer.add(hop.account.bytes());
                    }
                    if (hop.currency != null) {
                        buffer.add(Currency.encodeCurrency(hop.currency));
                    }
                    if (hop.issuer != null) {
                        buffer.add(hop.issuer.bytes());
                    }
                }
            }
            buffer.add(typeEnd);
            return buffer.toByteArray();
        }
    }
    static public Translator translate = new Translator();

    private PathSet(){}
    
    protected abstract static class PathSetField extends PathSet implements HasField{}
    public static PathSetField pathsetField(final Field f) {
        return new PathSetField(){ @Override public Field getField() {return f;}};
    }
    
    static public PathSetField Paths = pathsetField(Field.Paths);

    
    
}
