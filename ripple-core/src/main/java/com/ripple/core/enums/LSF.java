package com.ripple.core.enums;

// Ledger Specific Flags
public class LSF {
    // ltACCOUNT_ROOT
    public static int
            PasswordSpent = 0x00010000,   // True, if password set fee is spent.
            RequireDestTag = 0x00020000,   // True, to require a DestinationTag for payments.
            RequireAuth = 0x00040000,   // True, to require a authorization to hold IOUs.
            DisallowXRP = 0x00080000,   // True, to disallow sending XRP.
            DisableMaster = 0x00100000,   // True, force regular key

    // ltOFFER
    Passive = 0x00010000,
            Sell = 0x00020000,   // True, offer was placed as a sell.

    // ltRIPPLE_STATE
    LowReserve = 0x00010000,   // True, if entry counts toward reserve.
            HighReserve = 0x00020000,
            LowAuth = 0x00040000,
            HighAuth = 0x00080000,
            LowNoRipple = 0x00100000;
}
