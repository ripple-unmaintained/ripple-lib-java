package com.ripple.core.serialized;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface SerializedType {
    public static ObjectMapper objectMapper = new ObjectMapper();

    JsonNode toJackson();
    Object toJSON();
    byte[] toBytes();
    String toHex();
    void toBytesSink(BytesSink to);
}
