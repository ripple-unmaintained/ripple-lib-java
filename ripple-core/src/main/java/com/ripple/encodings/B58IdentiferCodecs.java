package com.ripple.encodings;


import com.ripple.encodings.base58.B58;

public class B58IdentiferCodecs {
    public static final int VER_ACCOUNT_ID        = 0;
    public static final int VER_FAMILY_SEED       = 33;

    public static final int VER_NONE              = 1;
    public static final int VER_NODE_PUBLIC       = 28;
    public static final int VER_NODE_PRIVATE      = 32;
    public static final int VER_ACCOUNT_PUBLIC    = 35;
    public static final int VER_ACCOUNT_PRIVATE   = 34;
    public static final int VER_FAMILY_GENERATOR  = 41;

    B58 b58;

    public B58IdentiferCodecs(B58 base58encoder) {
        this.b58 = base58encoder;
    }

    public byte[] decodeFamilySeed(String master_seed) {
        return b58.decodeChecked(master_seed, VER_FAMILY_SEED);
    }

    public String encodeFamilySeed(byte[] bytes) {
        return b58.encodeToStringChecked(bytes, VER_FAMILY_SEED);
    }

    public String encodeAddress(byte[] bytes) {
        return b58.encodeToStringChecked(bytes, VER_ACCOUNT_ID);
    }

    public byte[] decodeAddress(String address) {
        return b58.decodeChecked(address, VER_ACCOUNT_ID);
    }
}
