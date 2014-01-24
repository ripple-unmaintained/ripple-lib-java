package com.ripple.core.coretypes;

import com.ripple.core.fields.TypedFields;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesList;
import com.ripple.core.fields.Field;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PathSet extends ArrayList<PathSet.Path> implements SerializedType {

    public static class Hop{
        public AccountID account;
        public AccountID issuer;
        public Currency currencyBytes;

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
                    hop.currency(json.getString("currency"));
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
            if (currencyString() != null) type |= TYPE_CURRENCY;
            if (issuer != null) type |= TYPE_ISSUER;
        }

        public JSONObject toJSONObject() {
            JSONObject object = new JSONObject();
            try {
                object.put("type", getType());

                if (account  != null) object.put("account", AccountID.translate.toJSON(account));
                if (issuer   != null) object.put("issuer", AccountID.translate.toJSON(issuer));
                if (currencyString() != null) object.put("currency", currencyString());

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return object;
        }

        public void currency(String currency) {
            this.currencyBytes = Currency.translate.fromString(currency);
        }

        public String currencyString() {
            if (currencyBytes == null) {
                return null;
            }
            return currencyBytes.toString();
        }

        public void currency(byte[] read) {
            currencyBytes = new Currency(read);
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

    public static class Translator extends TypeTranslator<PathSet> {
        @Override
        public PathSet fromParser(BinaryParser parser, Integer hint) {
            PathSet pathSet = new PathSet();
            PathSet.Path path = null;
            while (!parser.end()) {
                byte type = parser.readOne();
                if (type == 0x00) {
                    break;
                }
                if (path == null) {
                    path = new PathSet.Path();
                    pathSet.add(path);
                }
                if (type == (byte) 0xFF) {
                    path = null;
                    continue;
                }

                PathSet.Hop hop = new PathSet.Hop();
                path.add(hop);
                if ((type & 0x01) != 0) {
                    hop.account = AccountID.translate.fromParser(parser);
                }
                if ((type & 0x10) != 0) {
                    hop.currencyBytes = Currency.translate.fromParser(parser);
                }
                if ((type & 0x20) != 0) {
                    hop.issuer = AccountID.translate.fromParser(parser);
                }
            }

            return pathSet;
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
        public void toBytesTree(PathSet obj, BytesList buffer) {
            // TODO, move these to fields to share with fromParser()
            byte typeBoundary = (byte) 0xff,
                    typeEnd = (byte) 0x00;

            int n = 0;
            for (Path path : obj) {
                if (n++ != 0) {
                    buffer.add(typeBoundary);
                }
                for (Hop hop : path) {
                    int type = hop.getType();
                    buffer.add((byte) type);
                    if (hop.account != null) {
                        buffer.add(hop.account.bytes());
                    }
                    // TODO, need to create a Currency class!!
                    if (hop.currencyString() != null) {
                        buffer.add(hop.currencyBytes.bytes());
                    }
                    if (hop.issuer != null) {
                        buffer.add(hop.issuer.bytes());
                    }
                }
            }
            buffer.add(typeEnd);
        }
    }
    static public Translator translate = new Translator();

    private PathSet(){}

    public static TypedFields.PathSetField pathsetField(final Field f) {
        return new TypedFields.PathSetField(){ @Override public Field getField() {return f;}};
    }
    
    static public TypedFields.PathSetField Paths = pathsetField(Field.Paths);
}
