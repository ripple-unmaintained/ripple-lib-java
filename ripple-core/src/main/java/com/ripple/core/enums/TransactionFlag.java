package com.ripple.core.enums;

// Transaction Specific Flags
public class TransactionFlag {
    public static long
    FullyCanonicalSig = 0x80000000L,
    Universal = FullyCanonicalSig,
    UniversalMask = ~Universal,

    // AccountSet flags:
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
    asfRequireDest   = 1,
    asfRequireAuth   = 2,
    asfDisallowXRP   = 3,
    asfDisableMaster = 4,
    asfAccountTxnID  = 5,
    asfNoFreeze      = 6,
    asfGlobalFreeze  = 7,

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
    SetAuth = 0x00010000,
    SetNoRipple = 0x00020000,
    ClearNoRipple = 0x00040000,
    SetFreeze            = 0x00100000,
    ClearFreeze          = 0x00200000,
    TrustSetMask = ~(Universal | SetAuth | SetNoRipple | ClearNoRipple | SetFreeze | ClearFreeze);
}
