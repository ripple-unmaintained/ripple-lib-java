package com.ripple.core.fields;

import com.ripple.core.enums.LedgerEntryType;
import com.ripple.core.enums.TransactionEngineResult;
import com.ripple.core.enums.TransactionType;

import java.util.EnumMap;

public class FieldSymbolics {
    static public EnumMap<Field, Enums> lookup = new EnumMap<Field, Enums>(Field.class);

    static public interface Enums {
        String asString(int i);

        Integer asInteger(String s);
    }

    static {
        lookup.put(Field.TransactionType, new Enums() {
            @Override
            public String asString(int i) {
                return TransactionType.fromNumber(i).name();
            }

            @Override
            public Integer asInteger(String s) {
                return TransactionType.valueOf(s).asInteger();
            }
        });
        lookup.put(Field.LedgerEntryType, new Enums() {
            @Override
            public String asString(int i) {
                return LedgerEntryType.fromNumber(i).name();
            }

            @Override
            public Integer asInteger(String s) {
                return LedgerEntryType.valueOf(s).asInteger();
            }
        });
        lookup.put(Field.TransactionResult, new Enums() {
            @Override
            public String asString(int i) {
                return TransactionEngineResult.fromNumber(i).name();
            }

            @Override
            public Integer asInteger(String s) {
                return TransactionEngineResult.valueOf(s).asInteger();
            }
        });
    }

    public static boolean isSymbolicField(Field field) {
        return lookup.containsKey(field);
    }

    public static String asString(Field f, Integer i) {
        return lookup.get(f).asString(i);
    }
    public static Integer asInteger(Field f, String s) {
        return lookup.get(f).asInteger(s);
    }
}
