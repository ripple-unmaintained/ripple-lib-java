package com.ripple.encodings.base58;

import com.ripple.config.Config;
import com.ripple.encodings.common.B16;
import org.junit.Test;
import org.ripple.bouncycastle.util.Arrays;

import static org.junit.Assert.assertEquals;

public class B58Test {
    B58 b58 = Config.getB58();

    @Test
    public void testFindsEdPrefix() throws Exception {
        String prefix = "sEd";
        byte[] versionBytes = b58.findPrefix(16, prefix);
        testStability(16, prefix, versionBytes);
        assertEncodesTo("01E14B", versionBytes);
    }

    @Test
    public void testFindsecp256k1Prefix() throws Exception {
        String prefix = "secp256k1";
        byte[] versionBytes = b58.findPrefix(16, prefix);
        testStability(16, prefix, versionBytes);
        assertEncodesTo("13984B20F2BD93", versionBytes);
    }

    private void testStability(int length, String prefix, byte[] versionBytes) {
        testStabilityWithAllByteValuesAtIx(0, length, prefix, versionBytes);
        testStabilityWithAllByteValuesAtIx(length -1, length, prefix, versionBytes);
    }

    private void testStabilityWithAllByteValuesAtIx(int ix,
                                                    int length,
                                                    String prefix,
                                                    byte[] version) {
        byte[] sample = new byte[length];
        Arrays.fill(sample, (byte) 0xff);

        for (int i = 0; i < 256; i++) {
            sample[ix] = (byte) i;
            String s = b58.encodeToStringChecked(sample, version);
            assertEquals(prefix, s.substring(0, prefix.length()));
        }
    }

    public void assertEncodesTo(String expected, byte[] actual) {
        assertEquals(expected, B16.toString(actual));
    }
}
