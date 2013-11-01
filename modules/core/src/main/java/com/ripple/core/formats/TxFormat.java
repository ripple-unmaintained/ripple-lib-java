package com.ripple.core.formats;

import com.ripple.core.enums.TransactionType;
import com.ripple.core.fields.Field;

import java.util.EnumMap;

public class TxFormat extends Format {
    static public EnumMap<TransactionType, TxFormat> formats = new EnumMap<TransactionType, TxFormat>(TransactionType.class);
    public final TransactionType transactionType;

    static public TxFormat fromString(String name) {
        return getTxFormat(TransactionType.valueOf(name));
    }

    static public TxFormat fromInteger(int ord) {
        return getTxFormat(TransactionType.fromNumber(ord));
    }

    static public TxFormat fromValue(Object o) {
        if (o instanceof Number) {
            return fromInteger(((Number) o).intValue());
        } else if (o instanceof String){
            return fromString((String) o);
        }
        else {
            return null;
        }
    }

    private static TxFormat getTxFormat(TransactionType key) {
        if (key == null) return null;
        return formats.get(key);
    }

    public TxFormat(TransactionType type, Object... args) {
        super(args);
        transactionType = type;
        addCommonFields();
        formats.put(transactionType, this);
    }

    @Override
    public void addCommonFields() {
        put(Field.TransactionType,     Requirement.REQUIRED);
        put(Field.Account,             Requirement.REQUIRED);
        put(Field.Sequence,            Requirement.REQUIRED);
        put(Field.Fee,                 Requirement.REQUIRED);
        put(Field.SigningPubKey,       Requirement.REQUIRED);

        put(Field.Flags,               Requirement.OPTIONAL);
        put(Field.SourceTag,           Requirement.OPTIONAL);
        put(Field.PreviousTxnID,       Requirement.OPTIONAL);
        put(Field.OperationLimit,      Requirement.OPTIONAL);
        put(Field.TxnSignature,        Requirement.OPTIONAL);
    }

    static public TxFormat AccountSet = new TxFormat(
            TransactionType.AccountSet,
            Field.EmailHash,       Requirement.OPTIONAL,
            Field.WalletLocator,   Requirement.OPTIONAL,
            Field.WalletSize,      Requirement.OPTIONAL,
            Field.MessageKey,      Requirement.OPTIONAL,
            Field.Domain,          Requirement.OPTIONAL,
            Field.TransferRate,    Requirement.OPTIONAL,
            Field.SetFlag,         Requirement.OPTIONAL,
            Field.ClearFlag,       Requirement.OPTIONAL);

    static public TxFormat TrustSet = new TxFormat(
            TransactionType.TrustSet,
            Field.LimitAmount,     Requirement.OPTIONAL,
            Field.QualityIn,       Requirement.OPTIONAL,
            Field.QualityOut,      Requirement.OPTIONAL);

    static public TxFormat OfferCreate = new TxFormat(
            TransactionType.OfferCreate,
            Field.TakerPays,       Requirement.REQUIRED,
            Field.TakerGets,       Requirement.REQUIRED,
            Field.Expiration,      Requirement.OPTIONAL,
            Field.OfferSequence,   Requirement.OPTIONAL);

    static public TxFormat OfferCancel = new TxFormat(
            TransactionType.OfferCancel,
            Field.OfferSequence,   Requirement.REQUIRED);

    static public TxFormat SetRegularKey = new TxFormat(
            TransactionType.SetRegularKey,
            Field.RegularKey,  Requirement.OPTIONAL);

    static public TxFormat Payment = new TxFormat(
            TransactionType.Payment,
            Field.Destination,     Requirement.REQUIRED,
            Field.Amount,          Requirement.REQUIRED,
            Field.SendMax,         Requirement.OPTIONAL,
            Field.Paths,           Requirement.DEFAULT,
            Field.InvoiceID,       Requirement.OPTIONAL,
            Field.DestinationTag,  Requirement.OPTIONAL);

    static public TxFormat Contract = new TxFormat(
            TransactionType.Contract,
            Field.Expiration,      Requirement.REQUIRED,
            Field.BondAmount,      Requirement.REQUIRED,
            Field.StampEscrow,     Requirement.REQUIRED,
            Field.RippleEscrow,    Requirement.REQUIRED,
            Field.CreateCode,      Requirement.OPTIONAL,
            Field.FundCode,        Requirement.OPTIONAL,
            Field.RemoveCode,      Requirement.OPTIONAL,
            Field.ExpireCode,      Requirement.OPTIONAL);

    static public TxFormat RemoveContract = new TxFormat(
            TransactionType.RemoveContract,
            Field.Target,          Requirement.REQUIRED);

    static public TxFormat EnableFeature = new TxFormat(
            TransactionType.EnableFeature,
            Field.Feature,         Requirement.REQUIRED);

    static public TxFormat SetFee = new TxFormat(
            TransactionType.SetFee,
            Field.BaseFee,             Requirement.REQUIRED,
            Field.ReferenceFeeUnits,   Requirement.REQUIRED,
            Field.ReserveBase,         Requirement.REQUIRED,
            Field.ReserveIncrement,    Requirement.REQUIRED);
}
