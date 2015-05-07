package com.ripple.encodings.base58;

import com.ripple.utils.HashUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;


public class B58 {
    public static class Decoded {
        public final byte[] version;
        public final byte[] payload;

        public Decoded(byte[] version, byte[] payload) {
            this.version = version;
            this.payload = payload;
        }
    }

    private int[] mIndexes;
    private char[] mAlphabet;

    public B58(String alphabet) {
        setAlphabet(alphabet);
        buildIndexes();
    }

    public byte[] findPrefix(int payLoadLength, String desiredPrefix) {
        int totalLength = payLoadLength + 4; // for the checksum
        double chars = Math.log(Math.pow(256, totalLength)) / Math.log(58);
        int requiredChars = (int) Math.ceil(chars + 0.2D);
        // Mess with this to see stability tests fail
        int charPos = (mAlphabet.length / 2) - 1;
        char padding = mAlphabet[(charPos)];
        String template = desiredPrefix + repeat(requiredChars, padding);
        byte[] decoded = decode(template);
        return copyOfRange(decoded, 0, decoded.length - totalLength);
    }

    private static String repeat(int times, char repeated) {
        char[] chars = new char[times];
        Arrays.fill(chars, repeated);
        return new String(chars);
    }

    private void setAlphabet(String alphabet) {
        mAlphabet = alphabet.toCharArray();
    }

    private void buildIndexes() {
        mIndexes = new int[128];

        for (int i = 0; i < mIndexes.length; i++) {
            mIndexes[i] = -1;
        }
        for (int i = 0; i < mAlphabet.length; i++) {
            mIndexes[mAlphabet[i]] = i;
        }
    }

    public String encodeToStringChecked(byte[] input, int version) {
        return encodeToStringChecked(input, new byte[]{(byte) version});
    }
    public String encodeToStringChecked(byte[] input, byte[] version) {
        try {
            return new String(encodeToBytesChecked(input, version), "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    public byte[] encodeToBytesChecked(byte[] input, int version) {
        return encodeToBytesChecked(input, new byte[]{(byte) version});
    }

    public byte[] encodeToBytesChecked(byte[] input, byte[] version) {
        byte[] buffer = new byte[input.length + version.length];
        System.arraycopy(version, 0, buffer, 0, version.length);
        System.arraycopy(input, 0, buffer, version.length, input.length);
        byte[] checkSum = copyOfRange(HashUtils.doubleDigest(buffer), 0, 4);
        byte[] output = new byte[buffer.length + checkSum.length];
        System.arraycopy(buffer, 0, output, 0, buffer.length);
        System.arraycopy(checkSum, 0, output, buffer.length, checkSum.length);
        return encodeToBytes(output);
    }

    public String encodeToString(byte[] input) {
        byte[] output = encodeToBytes(input);
        try {
            return new String(output, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    /**
     * Encodes the given bytes in base58. No checksum is appended.
     */
    public byte[] encodeToBytes(byte[] input) {
        if (input.length == 0) {
            return new byte[0];
        }
        input = copyOfRange(input, 0, input.length);
        // Count leading zeroes.
        int zeroCount = 0;
        while (zeroCount < input.length && input[zeroCount] == 0) {
            ++zeroCount;
        }
        // The actual encoding.
        byte[] temp = new byte[input.length * 2];
        int j = temp.length;

        int startAt = zeroCount;
        while (startAt < input.length) {
            byte mod = divmod58(input, startAt);
            if (input[startAt] == 0) {
                ++startAt;
            }
            temp[--j] = (byte) mAlphabet[mod];
        }

        // Strip extra '1' if there are some after decoding.
        while (j < temp.length && temp[j] == mAlphabet[0]) {
            ++j;
        }
        // Add as many leading '1' as there were leading zeros.
        while (--zeroCount >= 0) {
            temp[--j] = (byte) mAlphabet[0];
        }

        byte[] output;
        output = copyOfRange(temp, j, temp.length);
        return output;
    }

    public byte[] decode(String input) throws EncodingFormatException {
        if (input.length() == 0) {
            return new byte[0];
        }
        byte[] input58 = new byte[input.length()];
        // Transform the String to a base58 byte sequence
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);

            int digit58 = -1;
            if (c >= 0 && c < 128) {
                digit58 = mIndexes[c];
            }
            if (digit58 < 0) {
                throw new EncodingFormatException("Illegal character " + c + " at " + i);
            }

            input58[i] = (byte) digit58;
        }
        // Count leading zeroes
        int zeroCount = 0;
        while (zeroCount < input58.length && input58[zeroCount] == 0) {
            ++zeroCount;
        }
        // The encoding
        byte[] temp = new byte[input.length()];
        int j = temp.length;

        int startAt = zeroCount;
        while (startAt < input58.length) {
            byte mod = divmod256(input58, startAt);
            if (input58[startAt] == 0) {
                ++startAt;
            }

            temp[--j] = mod;
        }
        // Do no add extra leading zeroes, move j to first non null byte.
        while (j < temp.length && temp[j] == 0) {
            ++j;
        }

        return copyOfRange(temp, j - zeroCount, temp.length);
    }

    public BigInteger decodeToBigInteger(String input) throws EncodingFormatException {
        return new BigInteger(1, decode(input));

    }

    /**
     * Uses the checksum in the last 4 bytes of the decoded data to verify the rest are correct. The checksum is
     * removed from the returned data.
     *
     * @throws EncodingFormatException if the input is not baseFields 58 or the checksum does not validate.
     */
    public byte[] decodeChecked(String input, int version) throws EncodingFormatException {
        byte[] buffer = decodeAndCheck(input);

        byte actualVersion = buffer[0];
        if (actualVersion != version) {
            throw new EncodingFormatException("Bro, version is wrong yo");
        }


        return copyOfRange(buffer, 1, buffer.length - 4);
    }

    public Decoded decodeMulti(String input,
                               int expectedLength,
                               byte[]... possibleVersions) throws EncodingFormatException {

        byte[] buffer = decodeAndCheck(input);
        int versionLength = buffer.length - 4 - expectedLength;
        byte[] versionBytes = copyOfRange(buffer, 0, versionLength);

        byte[] foundVersion = null;
        for (byte[] possible : possibleVersions) {
            if (Arrays.equals(possible, versionBytes)) {
                foundVersion = possible;
                break;
            }
        }
        if (foundVersion == null) {
            throw new EncodingFormatException("Bro, version is wrong yo");
        }
        byte[] bytes = copyOfRange(buffer, versionLength, buffer.length - 4);
        return new Decoded(foundVersion, bytes);
    }

    private byte[] decodeAndCheck(String input) {
        byte buffer[] = decode(input);
        if (buffer.length < 4)
            throw new EncodingFormatException("Input too short");

        byte[] toHash = copyOfRange(buffer, 0, buffer.length - 4);
        byte[] hashed = copyOfRange(HashUtils.doubleDigest(toHash), 0, 4);
        byte[] checksum = copyOfRange(buffer, buffer.length - 4, buffer.length);

        if (!Arrays.equals(checksum, hashed))
            throw new EncodingFormatException("Checksum does not validate");
        return buffer;
    }

    //
    // number -> number / 58, returns number % 58
    //
    private byte divmod58(byte[] number, int startAt) {
        int remainder = 0;
        for (int i = startAt; i < number.length; i++) {
            int digit256 = (int) number[i] & 0xFF;
            int temp = remainder * 256 + digit256;

            number[i] = (byte) (temp / 58);

            remainder = temp % 58;
        }

        return (byte) remainder;
    }

    //
    // number -> number / 256, returns number % 256
    //
    private byte divmod256(byte[] number58, int startAt) {
        int remainder = 0;
        for (int i = startAt; i < number58.length; i++) {
            int digit58 = (int) number58[i] & 0xFF;
            int temp = remainder * 58 + digit58;

            number58[i] = (byte) (temp / 256);

            remainder = temp % 256;
        }

        return (byte) remainder;
    }

    private byte[] copyOfRange(byte[] source, int from, int to) {
        byte[] range = new byte[to - from];
        System.arraycopy(source, from, range, 0, range.length);

        return range;
    }
}
