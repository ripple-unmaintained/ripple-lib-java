package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.Hash256;

public class TestHelpers {
    public static Hash256 H256(String hex) {
        hex = zeroPadAfterTo(hex, 64);
        return Hash256.fromHex(hex);
    }

    public static String zeroPadAfterTo(String hex, int i) {
        if (hex.length() == i) {
            return hex;
        }
        else if (hex.length() > i) {
            throw new AssertionError();
        }
        StringBuilder sb = new StringBuilder();

        for (int j = 0; j < i - hex.length(); j++) {
            sb.append('0');
        }
        String s = hex + sb.toString();
        if (s.length() != i) throw new AssertionError();
        return s;
    }

    public static ShaMapLeaf Leaf(String hex) {
        Hash256Item hash256Item = new Hash256Item(H256(hex));
        return new ShaMapLeaf(hash256Item.item, hash256Item);
    }
}
