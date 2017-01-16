package com.ripple.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;

public class TestHelpers {
    private static final ClassLoader classLoader = TestHelpers
                                                    .class.getClassLoader();
    public static FileReader getResourceReader(String name) {
        URL resource = classLoader.getResource(name);
        try {
            if (resource == null) throw new AssertionError();
            return new FileReader(resource.getFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
