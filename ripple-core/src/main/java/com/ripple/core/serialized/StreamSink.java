package com.ripple.core.serialized;

import java.io.IOException;
import java.io.OutputStream;

public class StreamSink implements BytesSink {
    OutputStream out;

    public StreamSink(OutputStream out) {
        this.out = out;
    }

    @Override
    public void add(byte aByte) {
        try {
            out.write(aByte);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void add(byte[] bytes) {
        try {
            out.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

