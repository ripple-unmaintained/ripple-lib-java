package com.ripple.core.formats;

import com.ripple.core.fields.Field;
import com.ripple.core.serialized.enums.TransactionType;

import java.util.EnumMap;

public class TxFormat extends Format<TxFormat> {
    static public EnumMap<TransactionType, TxFormat> formats = new EnumMap<TransactionType, TxFormat>(TransactionType.class);
    public final TransactionType transactionType;

    static public TxFormat fromString(String name) {
        return getTxFormat(TransactionType.valueOf(name));
    }

    static public TxFormat fromNumber(Number ord) {
        return getTxFormat(TransactionType.fromNumber(ord));
    }

    static public TxFormat fromValue(Object o) {
        if (o instanceof Number) {
            return fromNumber(((Number) o).intValue());
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

    public TxFormat(TransactionType type) {
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
        put(Field.AccountTxnID,        Requirement.OPTIONAL);
        put(Field.LastLedgerSequence,  Requirement.OPTIONAL);
        put(Field.Memos,               Requirement.OPTIONAL);
        put(Field.Signers,             Requirement.OPTIONAL);
    }

    @Override
    public String name() {
        return transactionType.toString();
    }

    private static TxFormat makeFormat(TransactionType tt) {
        return new TxFormat(tt);
    }

    static public TxFormat AccountSet = makeFormat(
            TransactionType.AccountSet)
                .optional(Field.EmailHash)
                .optional(Field.EmailHash)
                .optional(Field.WalletLocator)
                .optional(Field.WalletSize)
                .optional(Field.MessageKey)
                .optional(Field.Domain)
                .optional(Field.TransferRate)
                .optional(Field.SetFlag)
                .optional(Field.TickSize)
                .optional(Field.ClearFlag);

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

    static public TxFormat TicketCreate = new TxFormat(
            TransactionType.TicketCreate,
            Field.Target,     Requirement.OPTIONAL,
            Field.Expiration, Requirement.OPTIONAL);

    static public TxFormat TicketCancel = new TxFormat(
            TransactionType.TicketCancel,
            Field.TicketID,   Requirement.REQUIRED);

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
            Field.DestinationTag,  Requirement.OPTIONAL,
            Field.DeliverMin,  Requirement.OPTIONAL
    );


    static public TxFormat SuspendedPaymentCreate = new TxFormat(
            TransactionType.SuspendedPaymentCreate,
            Field.Destination,      Requirement.REQUIRED,
            Field.Amount,           Requirement.REQUIRED,
            Field.Condition,        Requirement.OPTIONAL,
            Field.CancelAfter,      Requirement.OPTIONAL,
            Field.FinishAfter,      Requirement.OPTIONAL,
            Field.DestinationTag,   Requirement.OPTIONAL);

    static public TxFormat SuspendedPaymentFinish = new TxFormat(
            TransactionType.SuspendedPaymentFinish,
            Field.Owner,            Requirement.REQUIRED,
            Field.OfferSequence,    Requirement.REQUIRED,
            Field.Fulfillment,      Requirement.OPTIONAL,
            Field.Condition,        Requirement.OPTIONAL);

    static public TxFormat SuspendedPaymentCancel = new TxFormat(
            TransactionType.SuspendedPaymentCancel,
            Field.Owner,          Requirement.REQUIRED,
            Field.OfferSequence,  Requirement.REQUIRED);

    static public TxFormat EnableAmendment = new TxFormat(
            TransactionType.EnableAmendment,
            Field.LedgerSequence,      Requirement.REQUIRED,
            Field.Amendment,      Requirement.REQUIRED);

    static public TxFormat SetFee = new TxFormat(
            TransactionType.SetFee,
            Field.BaseFee,              Requirement.REQUIRED,
            Field.ReferenceFeeUnits,    Requirement.REQUIRED,
            Field.ReserveBase,          Requirement.REQUIRED,
            Field.LedgerSequence,       Requirement.OPTIONAL,
            Field.ReserveIncrement,     Requirement.REQUIRED
    );

    static public TxFormat SignerListSet = new TxFormat(
            TransactionType.SignerListSet,
            Field.SignerQuorum,              Requirement.REQUIRED,
            Field.SignerEntries,              Requirement.OPTIONAL
    );
    static public TxFormat PaymentChannelCreate = new TxFormat(
            TransactionType.PaymentChannelCreate,
            Field.Destination,              Requirement.REQUIRED,
            Field.Amount,              Requirement.REQUIRED,
            Field.SettleDelay,              Requirement.REQUIRED,
            Field.PublicKey,              Requirement.REQUIRED,
            Field.CancelAfter,              Requirement.OPTIONAL,
            Field.DestinationTag,              Requirement.OPTIONAL
    );
    static public TxFormat PaymentChannelFund = new TxFormat(
            TransactionType.PaymentChannelFund,
            Field.Channel,              Requirement.REQUIRED,
            Field.Amount,              Requirement.REQUIRED,
            Field.Expiration,              Requirement.OPTIONAL
    );
    static public TxFormat PaymentChannelClaim = new TxFormat(
            TransactionType.PaymentChannelClaim,
            Field.Channel,              Requirement.REQUIRED,
            Field.Amount,              Requirement.OPTIONAL,
            Field.Balance,              Requirement.OPTIONAL,
            Field.Signature,              Requirement.OPTIONAL,
            Field.PublicKey,              Requirement.OPTIONAL
    );
}
