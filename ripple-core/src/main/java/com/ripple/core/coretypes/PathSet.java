package com.ripple.core.coretypes;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.PathSetField;
import com.ripple.core.fields.Type;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesSink;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class PathSet extends ArrayList<PathSet.Path> implements SerializedType {
    public static byte PATH_SEPARATOR_BYTE = (byte) 0xFF;
    public static byte PATHSET_END_BYTE = (byte) 0x00;

    public PathSet(){}

    public static class Hop {
        public static byte TYPE_ACCOUNT  = (byte) 0x01;
        public static byte TYPE_CURRENCY = (byte) 0x10;
        public static byte TYPE_ISSUER   = (byte) 0x20;
        public static final int TYPE_ACCOUNT_CURRENCY_ISSUER = TYPE_CURRENCY | TYPE_ACCOUNT | TYPE_ISSUER;
        public static final int TYPE_ACCOUNT_CURRENCY = TYPE_CURRENCY | TYPE_ACCOUNT;
        public static int VALID_TYPE_MASK =  ~(TYPE_ACCOUNT | TYPE_CURRENCY | TYPE_ISSUER);

        public AccountID account;
        public AccountID issuer;
        public Currency  currency;
        private int type;

        public boolean hasIssuer() {
            return issuer   != null;
        }
        public boolean hasCurrency() {
            return currency != null;
        }
        public boolean hasAccount() {
            return account != null;
        }

        public int getType() {
            if (type == 0) {
                synthesizeType();
            }
            return type;
        }

        static public Hop fromJSONObject(JSONObject json) {
            Hop hop = new Hop();
            if (json.has("account")) {
                hop.account = AccountID.fromAddress(json.getString("account"));
            }
            if (json.has("issuer")) {
                hop.issuer = AccountID.fromAddress(json.getString("issuer"));
            }
            if (json.has("currency")) {
                hop.currency = Currency.fromString(json.getString("currency"));
            }
            if (json.has("type")) {
                hop.type = json.getInt("type");
            }
            return hop;
        }

        public void synthesizeType() {
            type = 0;

            if (hasAccount()) type |= TYPE_ACCOUNT;
            if (hasCurrency()) type |= TYPE_CURRENCY;
            if (hasIssuer()) type |= TYPE_ISSUER;
        }

        public JSONObject toJSONObject() {
            JSONObject object = new JSONObject();
            object.put("type", getType());

            if (hasAccount()) object.put("account", account.toJSON());
            if (hasIssuer()) object.put("issuer", issuer.toJSON());
            if (hasCurrency()) object.put("currency", currency.toJSON());
            return object;
        }
    }
    public static class Path extends ArrayList<Hop> {
        static public Path fromJSONArray(JSONArray array) {
            Path path = new Path();
            int nHops = array.length();
            for (int i = 0; i < nHops; i++) {
                JSONObject hop = array.getJSONObject(i);
                path.add(Hop.fromJSONObject(hop));
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

    public JSONArray toJSONArray() {
        JSONArray array = new JSONArray();
        for (Path path : this) {
            array.put(path.toJSONArray());
        }
        return array;
    }

    // SerializedType interface implementation
    @Override
    public Object toJSON() {
        return toJSONArray();
    }

    @Override
    public void toBytesSink(BytesSink buffer) {
        int n = 0;
        for (Path path : this) {
            if (n++ != 0) {
                buffer.add(PATH_SEPARATOR_BYTE);
            }
            for (Hop hop : path) {
                int type = hop.getType();
                buffer.add((byte) type);
                if (hop.hasAccount()) {
                    buffer.add(hop.account.bytes());
                }
                if (hop.hasCurrency()) {
                    buffer.add(hop.currency.bytes());
                }
                if (hop.hasIssuer()) {
                    buffer.add(hop.issuer.bytes());
                }
            }
        }
        buffer.add(PATHSET_END_BYTE);
    }

    @Override
    public Type type() {
        return Type.PathSet;
    }

    @Override
    public String toHex() {
        return translate.toHex(this);
    }

    @Override
    public byte[] toBytes() {
        return translate.toBytes(this);
    }


    public static class Translator extends TypeTranslator<PathSet> {
        @Override
        public PathSet fromParser(BinaryParser parser, Integer hint) {
            PathSet pathSet = new PathSet();
            PathSet.Path path = null;
            while (!parser.end()) {
                byte type = parser.readOne();
                if (type == PATHSET_END_BYTE) {
                    break;
                }
                if (path == null) {
                    path = new PathSet.Path();
                    pathSet.add(path);
                }
                if (type == PATH_SEPARATOR_BYTE) {
                    path = null;
                    continue;
                }

                PathSet.Hop hop = new PathSet.Hop();
                path.add(hop);
                if ((type & Hop.TYPE_ACCOUNT) != 0) {
                    hop.account = AccountID.translate.fromParser(parser);
                }
                if ((type & Hop.TYPE_CURRENCY) != 0) {
                    hop.currency = Currency.translate.fromParser(parser);
                }
                if ((type & Hop.TYPE_ISSUER) != 0) {
                    hop.issuer = AccountID.translate.fromParser(parser);
                }
            }

            return pathSet;
        }

        @Override
        public PathSet fromJSONArray(JSONArray array) {
            PathSet paths = new PathSet();

            int nPaths = array.length();

            for (int i = 0; i < nPaths; i++) {
                JSONArray path = array.getJSONArray(i);
                paths.add(Path.fromJSONArray(path));
            }

            return paths;
        }
    }
    static public Translator translate = new Translator();

    public static PathSetField pathsetField(final Field f) {
        return new PathSetField(){ @Override public Field getField() {return f;}};
    }
    static public PathSetField Paths = pathsetField(Field.Paths);
}
