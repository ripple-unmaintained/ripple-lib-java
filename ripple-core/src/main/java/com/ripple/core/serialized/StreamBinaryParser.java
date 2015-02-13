package com.ripple.core.serialized;

import java.io.*;
import java.util.zip.GZIPInputStream;

public class StreamBinaryParser extends BinaryParser {
    final BufferedInputStream stream;

    public StreamBinaryParser(InputStream stream, long size) {
        super((int) size);
        this.stream = new BufferedInputStream(stream);
    }

    private static boolean isGZip(File fio) {
        return fio.getName().endsWith("gz");
    }
    private static int getUncompressedSize(File fio) {
        if (isGZip(fio)) {
            int val;
            try {
                RandomAccessFile raf = new RandomAccessFile(fio, "r");
                raf.seek(raf.length() - 4);
                int b4 = raf.read();
                int b3 = raf.read();
                int b2 = raf.read();
                int b1 = raf.read();
                val = (b1 << 24) | (b2 << 16) + (b3 << 8) + b4;
                raf.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return val;
        } else {
            return (int) fio.length();
        }
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
            File f = new File(path);
            FileInputStream fstream = new FileInputStream(path);
            InputStream stream = fstream;
            long s = fstream.getChannel().size();

            if (isGZip(f)) {
                s = getUncompressedSize(f);
                stream = new GZIPInputStream(fstream);
            }
            return new StreamBinaryParser(stream, s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
