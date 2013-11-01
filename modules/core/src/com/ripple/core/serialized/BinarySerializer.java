package com.ripple.core.serialized;

import com.ripple.core.fields.Field;
import com.ripple.core.types.translators.Translators;
import java.lang.UnsupportedOperationException;

import java.util.ArrayList;
import java.util.Arrays;

public class BinarySerializer {
    ArrayList<Byte> buffer;

    public BinarySerializer() {
        this.buffer = new ArrayList<Byte>();
    }

    public byte[] fieldHeader(Field field) {
        int name = field.getId(), type = field.getType().getId();
        assert ((type > 0) && (type < 256) && (name > 0) && (name < 256));
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
        for (byte b : n) {
            buffer.add(b);
        }
    }

    public void addLengthEncoded(byte[] n) {
        add(encodeVL(n));

        for (byte b : n) {
            buffer.add(b);
        }
    }

    public static byte[] encodeVL(byte[] n) {
        byte[] lenBytes = new byte[4];
        int length = n.length;
        
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

    public void add(Field f, SerializedType t) {
        TypeTranslator<SerializedType> ts = Translators.forField(f);
        byte[] wireBytes = ts.toWireBytes(t);
        addFieldHeader(f);

        switch (f.getType()) {
            case UNKNOWN:
            case DONE:
            case NOTPRESENT:
            case UINT8:
            case UINT16:
            case UINT32:
            case UINT64:
            case AMOUNT:
            case PATHSET:
                add(wireBytes);
                break;

            case ACCOUNT:
            case HASH128:
            case HASH160:
            case HASH256:
            case VL:
                addLengthEncoded(wireBytes);
                break;

            case OBJECT:
            case ARRAY:
            case VECTOR256:
            case TRANSACTION:
            case LEDGERENTRY:
            case VALIDATION:
                throw new UnsupportedOperationException();
        }

    }

    private int addFieldHeader(Field f) {
        byte[] n = fieldHeader(f);
        add(n);
        return n.length;
    }

    public byte[] toByteArray() {
        byte[] primitive = new byte[buffer.size()];
        for (int i = 0; i < primitive.length; i++) {
            primitive[i] = buffer.get(i);
        }
        return primitive;
    }
}
