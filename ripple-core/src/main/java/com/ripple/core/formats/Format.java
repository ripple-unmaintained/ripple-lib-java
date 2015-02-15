package com.ripple.core.formats;

import com.ripple.core.fields.Field;

import java.util.EnumMap;

abstract public class Format {
    public void addCommonFields(){}

    EnumMap<Field, Requirement> requirementEnumMap = new EnumMap<Field, Requirement>(Field.class);

    public EnumMap<Field, Requirement> requirements() {
        return requirementEnumMap;
    }

    abstract  public String name ();

    public Format(Object[] args) {
        if ((!(args.length % 2 == 0)) || args.length < 2) {
            throw new IllegalArgumentException("Varargs length should be a minimum multiple of 2");
        }
        for (int i = 0; i < args.length; i+= 2) {
            Field f = (Field) args[i];
            Requirement r = (Requirement) args[i + 1];
            put(f, r);
        }
    }

    protected void put(Field f, Requirement r) {
        requirementEnumMap.put(f, r);
    }

    public static enum Requirement {
        INVALID(-1),
        REQUIRED( 0),
        OPTIONAL( 1),
        DEFAULT( 2);
        Requirement(int i) {}
    }
}
