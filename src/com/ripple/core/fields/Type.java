/* DO NOT EDIT, AUTO GENERATED */
package com.ripple.core.fields;
import java.util.Map;
import java.util.TreeMap;

public enum Type {
    UNKNOWN(-2),
    DONE(-1),
    NOTPRESENT(0),
    UINT16(1),
    UINT32(2),
    UINT64(3),
    HASH128(4),
    HASH256(5),
    AMOUNT(6),
    VL(7),
    ACCOUNT(8),
    OBJECT(14),
    ARRAY(15),
    UINT8(16),
    HASH160(17),
    PATHSET(18),
    VECTOR256(19),
    TRANSACTION(10001),
    LEDGERENTRY(10002),
    VALIDATION(10003);

    static private Map<Integer, Type> byInt = new TreeMap<Integer, Type>();
    static {
        for (Object a : Type.values()) {
           Type t = (Type) a;
            byInt.put(t.id, t);
        }
    }

    static public Type valueOf(Integer integer) {
        return byInt.get(integer);
    }

    final int id;

    Type(int type) {
        this.id = type;
    }

    public int getId() {
        return id;
    }
}
