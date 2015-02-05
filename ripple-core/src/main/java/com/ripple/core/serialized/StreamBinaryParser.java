package com.ripple.core.serialized;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamBinaryParser extends BinaryParser {
    final BufferedInputStream stream;

    public StreamBinaryParser(InputStream stream, long size) {
        super((int) size);
        this.stream = new BufferedInputStream(stream);
    }

    public void skip(int n) {
        try {
            long skipped = stream.skip(n);
            if (skipped != n) {
                throw new RuntimeException("Expected to skip more bytes");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public byte readOne() {
        return read(1)[0];
    }
    public byte[] read(int n) {
        byte[] ret = new byte[n];
        try {
            int read = stream.read(ret);
            if (read != n) {
                throw new RuntimeException("Expected to read more bytes");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        cursor += n;
        return ret;
    }
    public static StreamBinaryParser fromFile(String path) {
        try {
            FileInputStream stream = new FileInputStream(path);
            long s = stream.getChannel().size();
            return new StreamBinaryParser(stream, s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
