package com.ripple.core.fields;

public class TypedFields {
    public abstract static class UInt8Field implements HasField {}
    public abstract static class Vector256Field implements HasField{}
    public abstract static class VariableLengthField implements HasField{}
    public abstract static class UInt64Field implements HasField {}
    public abstract static class UInt32Field implements HasField {}
    public abstract static class UInt16Field implements HasField {}
    public abstract static class PathSetField implements HasField{}
    public abstract static class STObjectField implements HasField{}
    public abstract static class Hash256Field implements HasField {}
    public abstract static class Hash160Field implements HasField {}
    public abstract static class Hash128Field implements HasField {}
    public abstract static class STArrayField implements HasField{}
    public abstract static class AmountField implements HasField {}
    public abstract static class AccountIDField implements HasField {}
}
