package com.ripple.core.formats;

import com.ripple.core.enums.LedgerEntryType;
import com.ripple.core.fields.Field;

import java.util.EnumMap;

public class SLEFormat extends Format {
    static public EnumMap<LedgerEntryType, SLEFormat> formats = new EnumMap<LedgerEntryType, SLEFormat>(LedgerEntryType.class);

    static public SLEFormat fromString(String name) {
        return getLedgerFormat(LedgerEntryType.valueOf(name));
    }

    static public SLEFormat fromInteger(int ord) {
        return getLedgerFormat(LedgerEntryType.fromNumber(ord));
    }

    static public SLEFormat fromValue(Object o) {
        if (o instanceof Number) {
            return fromInteger(((Number) o).intValue());
        } else if (o instanceof String){
            return fromString((String) o);
        }
        else {
            return null;
        }
    }

    private static SLEFormat getLedgerFormat(LedgerEntryType key) {
        if (key == null) return null;
        return formats.get(key);
    }

    public final LedgerEntryType ledgerEntryType;

    public SLEFormat(LedgerEntryType type, Object... args) {
        super(args);
        ledgerEntryType = type;
        addCommonFields();
        formats.put(type, this);
    }

    @Override
    public void addCommonFields() {
        put(Field.LedgerIndex,             Requirement.OPTIONAL);
        put(Field.LedgerEntryType,         Requirement.REQUIRED);
        put(Field.Flags,                   Requirement.REQUIRED);
    }

    public static SLEFormat AccountRoot = new SLEFormat(
            LedgerEntryType.AccountRoot,
            Field.Account,             Requirement.REQUIRED,
            Field.Sequence,            Requirement.REQUIRED,
            Field.Balance,             Requirement.REQUIRED,
            Field.OwnerCount,          Requirement.REQUIRED,
            Field.PreviousTxnID,       Requirement.REQUIRED,
            Field.PreviousTxnLgrSeq,   Requirement.REQUIRED,
            Field.RegularKey,          Requirement.OPTIONAL,
            Field.EmailHash,           Requirement.OPTIONAL,
            Field.WalletLocator,       Requirement.OPTIONAL,
            Field.WalletSize,          Requirement.OPTIONAL,
            Field.MessageKey,          Requirement.OPTIONAL,
            Field.TransferRate,        Requirement.OPTIONAL,
            Field.Domain,              Requirement.OPTIONAL
    );

    public static SLEFormat Contract = new SLEFormat(
            LedgerEntryType.Contract,
            Field.Account,             Requirement.REQUIRED,
            Field.Balance,             Requirement.REQUIRED,
            Field.PreviousTxnID,       Requirement.REQUIRED,
            Field.PreviousTxnLgrSeq,   Requirement.REQUIRED,
            Field.Issuer,              Requirement.REQUIRED,
            Field.Owner,               Requirement.REQUIRED,
            Field.Expiration,          Requirement.REQUIRED,
            Field.BondAmount,          Requirement.REQUIRED,
            Field.CreateCode,          Requirement.OPTIONAL,
            Field.FundCode,            Requirement.OPTIONAL,
            Field.RemoveCode,          Requirement.OPTIONAL,
            Field.ExpireCode,          Requirement.OPTIONAL
    );

    public static SLEFormat DirectoryNode = new SLEFormat(
            LedgerEntryType.DirectoryNode,
            Field.Owner,               Requirement.OPTIONAL,  // for owner directories
            Field.TakerPaysCurrency,   Requirement.OPTIONAL,  // for order book directories
            Field.TakerPaysIssuer,     Requirement.OPTIONAL,  // for order book directories
            Field.TakerGetsCurrency,   Requirement.OPTIONAL,  // for order book directories
            Field.TakerGetsIssuer,     Requirement.OPTIONAL,  // for order book directories
            Field.ExchangeRate,        Requirement.OPTIONAL,  // for order book directories
            Field.Indexes,             Requirement.REQUIRED,
            Field.RootIndex,           Requirement.REQUIRED,
            Field.IndexNext,           Requirement.OPTIONAL,
            Field.IndexPrevious,       Requirement.OPTIONAL
    );

    public static SLEFormat GeneratorMap = new SLEFormat(
            LedgerEntryType.GeneratorMap,
            Field.Generator,           Requirement.REQUIRED
    );

    public static SLEFormat Nickname = new SLEFormat(
            LedgerEntryType.Nickname,
            Field.Account,             Requirement.REQUIRED,
            Field.MinimumOffer,        Requirement.OPTIONAL
    );

    public static SLEFormat Offer = new SLEFormat(
            LedgerEntryType.Offer,
            Field.Account,             Requirement.REQUIRED,
            Field.Sequence,            Requirement.REQUIRED,
            Field.TakerPays,           Requirement.REQUIRED,
            Field.TakerGets,           Requirement.REQUIRED,
            Field.BookDirectory,       Requirement.REQUIRED,
            Field.BookNode,            Requirement.REQUIRED,
            Field.OwnerNode,           Requirement.REQUIRED,
            Field.PreviousTxnID,       Requirement.REQUIRED,
            Field.PreviousTxnLgrSeq,   Requirement.REQUIRED,
            Field.Expiration,          Requirement.OPTIONAL
    );

    public static SLEFormat RippleState = new SLEFormat(
            LedgerEntryType.RippleState,
            Field.Balance,             Requirement.REQUIRED,
            Field.LowLimit,            Requirement.REQUIRED,
            Field.HighLimit,           Requirement.REQUIRED,
            Field.PreviousTxnID,       Requirement.REQUIRED,
            Field.PreviousTxnLgrSeq,   Requirement.REQUIRED,
            Field.LowNode,             Requirement.OPTIONAL,
            Field.LowQualityIn,        Requirement.OPTIONAL,
            Field.LowQualityOut,       Requirement.OPTIONAL,
            Field.HighNode,            Requirement.OPTIONAL,
            Field.HighQualityIn,       Requirement.OPTIONAL,
            Field.HighQualityOut,      Requirement.OPTIONAL
    );

    public static SLEFormat LedgerHashes = new SLEFormat(
            LedgerEntryType.LedgerHashes,
            Field.FirstLedgerSequence, Requirement.OPTIONAL, // Remove if we do a ledger restart
            Field.LastLedgerSequence,  Requirement.OPTIONAL,
            Field.Hashes,              Requirement.REQUIRED
    );

    public static SLEFormat EnabledFeatures = new SLEFormat(
            LedgerEntryType.EnabledFeatures,
            Field.Features, Requirement.REQUIRED
    );

    public static SLEFormat FeeSettings = new SLEFormat(
            LedgerEntryType.FeeSettings,
            Field.BaseFee,             Requirement.REQUIRED,
            Field.ReferenceFeeUnits,   Requirement.REQUIRED,
            Field.ReserveBase,         Requirement.REQUIRED,
            Field.ReserveIncrement,    Requirement.REQUIRED
    );
}
