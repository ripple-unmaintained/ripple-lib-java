package com.ripple.core.types;

import com.ripple.core.types.hash.Hash256;
import org.junit.Test;

public class QualityTest {
    @Test
    public void testFromBookDirectory() throws Exception {
        Hash256 hash256 = Hash256.translate.fromString("4627DFFCFF8B5A265EDBD8AE8C14A52325DBFEDAF4F5C32E5C08A1FB2E56F800");
        System.out.println(Quality.fromBookDirectory(hash256, true));

    }
}
