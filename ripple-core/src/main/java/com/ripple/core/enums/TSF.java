package com.ripple.core.enums;

public class TSF {
    public static long
    FullyCanonicalSig = 0x80000000,
    Universal = FullyCanonicalSig,
    UniversalMask = ~Universal,

    // AccountSet flags:
    // VFALCO TODO Javadoc comment every one of these constants
    RequireDestTag = 0x00010000,
    OptionalDestTag = 0x00020000,
    RequireAuth = 0x00040000,
    OptionalAuth = 0x00080000,
    DisallowXRP = 0x00100000,
    AllowXRP = 0x00200000,
    AccountSetMask = ~(Universal | RequireDestTag | OptionalDestTag
            | RequireAuth | OptionalAuth
            | DisallowXRP | AllowXRP),

    // AccountSet SetFlag/ClearFlag values
    asfRequireDest = 1,
    asfRequireAuth = 2,
    asfDisallowXRP = 3,
    asfDisableMaster = 4,
    asfAccountTxnID = 5,

    // OfferCreate flags:
    Passive = 0x00010000,
    ImmediateOrCancel = 0x00020000,
    FillOrKill = 0x00040000,
    Sell = 0x00080000,
    OfferCreateMask = ~(Universal | Passive | ImmediateOrCancel | FillOrKill | Sell),

    // Payment flags:
    NoRippleDirect = 0x00010000,
    PartialPayment = 0x00020000,
    LimitQuality = 0x00040000,
    PaymentMask = ~(Universal | PartialPayment | LimitQuality | NoRippleDirect),

    // TrustSet flags:
    SetfAuth = 0x00010000,
    SetNoRipple = 0x00020000,
    ClearNoRipple = 0x00040000,
    TrustSetMask = ~(Universal | SetfAuth | SetNoRipple | ClearNoRipple);
}