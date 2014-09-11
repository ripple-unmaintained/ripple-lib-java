package com.ripple.core;

import com.ripple.core.coretypes.AccountID;
import com.ripple.crypto.ecdsa.Seed;

public class TestFixtures {
    /*
    * From wallet_propose masterpassphrase
    * */
    public static String master_seed_address = "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh";
    public static String master_seed = "snoPBrXtMeMyMHUVTgbuqAfg1SUTb";
    public static byte[] master_seed_bytes = new byte[] {
            (byte) 0xde,
            (byte)0xdc,
            (byte)0xe9,
            (byte)0xce,
            (byte)0x67,
            (byte)0xb4,
            (byte)0x51,
            (byte)0xd8,
            (byte)0x52,
            (byte)0xfd,
            (byte)0x4e,
            (byte)0x84,
            (byte)0x6f,
            (byte)0xcd,
            (byte)0xe3,
            (byte)0x1c };
    public static String singed_master_seed_bytes = "3046022100eb46f96961453219b5e2baa263f01d66c3c7d3ca0672623f13b9a24508d0a56c022100b1b64510c7f415e902f00071d821a112fe489f0fb4fb6f87b411e88a73b40e70";

    public static AccountID root_account = AccountID.fromKeyPair(Seed.fromBase58(master_seed).keyPair());
    public static AccountID bob_account = AccountID.fromKeyPair(Seed.fromBase58("shn6zJ8zzkaoFSfsEWvJLZf3V344C").keyPair());
}
