package com.ripple.core.serialized;

public interface SerializedType {
    TypeTranslator<SerializedType> translator();
}
