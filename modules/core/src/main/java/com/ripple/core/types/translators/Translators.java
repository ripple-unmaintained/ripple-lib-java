package com.ripple.core.types.translators;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.Type;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import com.ripple.core.types.*;
import com.ripple.core.types.hash.Hash128;
import com.ripple.core.types.hash.Hash160;
import com.ripple.core.types.hash.Hash256;
import com.ripple.core.types.uint.UInt16;
import com.ripple.core.types.uint.UInt32;
import com.ripple.core.types.uint.UInt64;
import com.ripple.core.types.uint.UInt8;

public class Translators {
    public static TypeTranslator forType(Type type) {
        switch (type) {
            case AMOUNT:     return Amount.translate;
            case UINT16:     return UInt16.translate;
            case UINT32:     return UInt32.translate;
            case UINT64:     return UInt64.translate;
            case HASH128:    return Hash128.translate;
            case HASH256:    return Hash256.translate;
            case VL:         return VariableLength.translate;
            case ACCOUNT:    return AccountID.translate;
            case OBJECT:     return STObject.translate;
            case ARRAY:      return STArray.translate;
            case UINT8:      return UInt8.translate;
            case HASH160:    return Hash160.translate;
            case PATHSET:    return PathSet.translate;
            case VECTOR256:  return Vector256.translate;
            default:         throw new RuntimeException("Unknown type");
        }
    }

    public static TypeTranslator<SerializedType> forField(Field field) {
        if (field.tag == null) {
            field.tag = forType(field.getType());
        }
        return getCastedTag(field);
    }

    @SuppressWarnings("unchecked")
    private static TypeTranslator<SerializedType> getCastedTag(Field field) {
        return (TypeTranslator<SerializedType>) field.tag;
    }
}
