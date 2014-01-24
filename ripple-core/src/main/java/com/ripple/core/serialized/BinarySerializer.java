package com.ripple.core.serialized;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.Type;

import java.util.ArrayList;
import java.util.Arrays;

public class BinarySerializer {
    private final BytesList buffer;

    public BinarySerializer() {
        this.buffer = new BytesList();
    }

    public BinarySerializer(BytesList buffer) {
        this.buffer = buffer;
    }

    public static byte[] fieldHeader(Field field) {
        int name = field.getId(), type = field.getType().getId();
        if (!((type > 0) && (type < 256) && (name > 0) && (name < 256))) {
            throw new RuntimeException("Field is invalid: " + field.toString());
        }

        ArrayList<Byte> header = new ArrayList<Byte>(3);
        
        if (type < 16)
        {
            if (name < 16) // common type, common name
                header.add((byte)((type << 4) | name));
            else
            {
                // common type, uncommon name
                header.add((byte)(type << 4));
                header.add((byte)(name));
            }
        }
        else if (name < 16)
        {
            // uncommon type, common name
            header.add((byte)(name));
            header.add((byte)(type));
        }
        else
        {
            // uncommon type, uncommon name
            header.add((byte)(0));
            header.add((byte)(type));
            header.add((byte)(name));
        }

        byte[] headerBytes = new byte[header.size()];
        for (int i = 0; i < header.size(); i++) {
            headerBytes[i] = header.get(i);
        }

        return headerBytes;
    }

    public void add(byte[] n) {
        buffer.add(n);
    }

    public void addLengthEncoded(byte[] n) {
        add(encodeVL(n.length));
        add(n);
    }

    public static byte[] encodeVL(int  length) {
        byte[] lenBytes = new byte[4];

        if (length <= 192)
        {
            lenBytes[0] = (byte) (length);
            return Arrays.copyOf(lenBytes, 1);
        }
        else if (length <= 12480)
        {
            length -= 193;
            lenBytes[0] = (byte) (193 + (length >>> 8));
            lenBytes[1] = (byte) (length & 0xff);
            return Arrays.copyOf(lenBytes, 2);
        }
        else if (length <= 918744) {
            length -= 12481;
            lenBytes[0] = (byte) (241 + (length >>> 16));
            lenBytes[1] = (byte) ((length >> 8) & 0xff);
            lenBytes[2] = (byte) (length & 0xff);
            return Arrays.copyOf(lenBytes, 3);
        } else {
            throw new RuntimeException("Overflow error");
        }
    }


    public void add(Field f, SerializedType t, TypeTranslator<SerializedType> ts) {
        addFieldHeader(f);
        add(f.getType(), t, ts);
    }

    // We shouldn't have any dependency on concrete classes, either directly
    // or transitively, so don't import `com.ripple.core.translators` directly
    public void add(Type type, SerializedType t, TypeTranslator<SerializedType> ts) {
        switch (type) {
            case UNKNOWN:
            case DONE:
            case NOTPRESENT:
            case UINT8:
            case UINT16:
            case UINT32:
            case UINT64:
            case AMOUNT:
            case HASH256:
            case PATHSET:
            case HASH128:
            case HASH160:
                ts.toBytesTree(t, buffer);
                break;

            case ACCOUNT:
            case VECTOR256:
            case VL:
                BytesList inner = new BytesList();
                ts.toBytesTree(t, inner);
                add(encodeVL(inner.length()));
                add(inner);
                break;

            case OBJECT:
                ts.toBytesTree(t, buffer);
                add(Markers.OBJECT_END);
                break;
            case ARRAY:
                ts.toBytesTree(t, buffer);
                add(Markers.ARRAY_END);
                break;

            case TRANSACTION:
            case LEDGERENTRY:
            case VALIDATION:
                throw new UnsupportedOperationException("Can't serialize " + type.toString());
        }
    }

    private void add(BytesList inner) {
        buffer.add(inner);
    }

    private int addFieldHeader(Field f) {
        byte[] n = fieldHeader(f);
        add(n);
        return n.length;
    }

    public byte[] bytes() {
        return buffer.bytes();
    }

    public void add(byte type) {
        buffer.add(type);
    }
}
