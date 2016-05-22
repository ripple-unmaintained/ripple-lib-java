package com.ripple.core.fields;

import java.util.*;

public enum Field {
    // These are all presorted (verified in a static block below)
    // They can then be used in a TreeMap, using the Enum (private) ordinal
    // comparator
    Generic(0, Type.Unknown),
    Invalid(-1, Type.Unknown),

    LedgerEntryType(1, Type.UInt16),
    TransactionType(2, Type.UInt16),
    SignerWeight(3, Type.UInt16),

    Flags(2, Type.UInt32),
    SourceTag(3, Type.UInt32),
    Sequence(4, Type.UInt32),
    PreviousTxnLgrSeq(5, Type.UInt32),
    LedgerSequence(6, Type.UInt32),
    CloseTime(7, Type.UInt32),
    ParentCloseTime(8, Type.UInt32),
    SigningTime(9, Type.UInt32),
    Expiration(10, Type.UInt32),
    TransferRate(11, Type.UInt32),
    WalletSize(12, Type.UInt32),
    OwnerCount(13, Type.UInt32),
    DestinationTag(14, Type.UInt32),

    HighQualityIn(16, Type.UInt32),
    HighQualityOut(17, Type.UInt32),
    LowQualityIn(18, Type.UInt32),
    LowQualityOut(19, Type.UInt32),
    QualityIn(20, Type.UInt32),
    QualityOut(21, Type.UInt32),
    StampEscrow(22, Type.UInt32),
    BondAmount(23, Type.UInt32),
    LoadFee(24, Type.UInt32),
    OfferSequence(25, Type.UInt32),
    FirstLedgerSequence(26, Type.UInt32), // Deprecated: do not use
    // Added new semantics in 9486fc416ca7c59b8930b734266eed4d5b714c50
    LastLedgerSequence(27, Type.UInt32),
    TransactionIndex(28, Type.UInt32),
    OperationLimit(29, Type.UInt32),
    ReferenceFeeUnits(30, Type.UInt32),
    ReserveBase(31, Type.UInt32),
    ReserveIncrement(32, Type.UInt32),
    SetFlag(33, Type.UInt32),
    ClearFlag(34, Type.UInt32),
    SignerQuorum(35, Type.UInt32),
    CancelAfter(36, Type.UInt32),
    FinishAfter(37, Type.UInt32),
    SignerListID(38, Type.UInt32),


    IndexNext(1, Type.UInt64),
    IndexPrevious(2, Type.UInt64),
    BookNode(3, Type.UInt64),
    OwnerNode(4, Type.UInt64),
    BaseFee(5, Type.UInt64),
    ExchangeRate(6, Type.UInt64),
    LowNode(7, Type.UInt64),
    HighNode(8, Type.UInt64),

    EmailHash(1, Type.Hash128),

    LedgerHash(1, Type.Hash256),
    ParentHash(2, Type.Hash256),
    TransactionHash(3, Type.Hash256),
    AccountHash(4, Type.Hash256),
    PreviousTxnID(5, Type.Hash256),
    LedgerIndex(6, Type.Hash256),
    WalletLocator(7, Type.Hash256),
    RootIndex(8, Type.Hash256),
    // Added in rippled commit: 9486fc416ca7c59b8930b734266eed4d5b714c50
    AccountTxnID(9, Type.Hash256),
    BookDirectory(16, Type.Hash256),
    InvoiceID(17, Type.Hash256),
    Nickname(18, Type.Hash256),
    Amendment(19, Type.Hash256),
    TicketID(20, Type.Hash256),
    Digest(21, Type.Hash256),

    hash(257, Type.Hash256),
    index(258, Type.Hash256),

    Amount(1, Type.Amount),
    Balance(2, Type.Amount),
    LimitAmount(3, Type.Amount),
    TakerPays(4, Type.Amount),
    TakerGets(5, Type.Amount),
    LowLimit(6, Type.Amount),
    HighLimit(7, Type.Amount),
    Fee(8, Type.Amount),
    SendMax(9, Type.Amount),
    DeliverMin(10, Type.Amount),

    MinimumOffer(16, Type.Amount),
    RippleEscrow(17, Type.Amount),
    // Added in rippled commit: e7f0b8eca69dd47419eee7b82c8716b3aa5a9e39
    DeliveredAmount(18, Type.Amount),
    // These are auxiliary fields
//    quality(257, Type.AMOUNT),
    taker_gets_funded(258, Type.Amount),
    taker_pays_funded(259, Type.Amount),

    PublicKey(1, Type.Blob),
    MessageKey(2, Type.Blob),
    SigningPubKey(3, Type.Blob),
    TxnSignature(4, Type.Blob),
    Signature(6, Type.Blob),
    Domain(7, Type.Blob),
    FundCode(8, Type.Blob),
    RemoveCode(9, Type.Blob),
    ExpireCode(10, Type.Blob),
    CreateCode(11, Type.Blob),
    MemoType(12, Type.Blob),
    MemoData(13, Type.Blob),
    MemoFormat(14, Type.Blob),

    Proof(17, Type.Blob),

    Account(1, Type.AccountID),
    Owner(2, Type.AccountID),
    Destination(3, Type.AccountID),
    Issuer(4, Type.AccountID),
    Target(7, Type.AccountID),
    RegularKey(8, Type.AccountID),

    ObjectEndMarker(1, Type.STObject),
    TransactionMetaData(2, Type.STObject),
    CreatedNode(3, Type.STObject),
    DeletedNode(4, Type.STObject),
    ModifiedNode(5, Type.STObject),
    PreviousFields(6, Type.STObject),
    FinalFields(7, Type.STObject),
    NewFields(8, Type.STObject),
    TemplateEntry(9, Type.STObject),
    Memo(10, Type.STObject),
    SignerEntry(11, Type.STObject),
    Signer(16, Type.STObject),
    // 17 unused
    Majority(18, Type.STObject),

    ArrayEndMarker(1, Type.STArray),
//    SigningAccounts(2, Type.STArray),
    Signers(3, Type.STArray),
    SignerEntries(4, Type.STArray),
    Template(5, Type.STArray),
    Necessary(6, Type.STArray),
    Sufficient(7, Type.STArray),
    AffectedNodes(8, Type.STArray),
    Memos(9, Type.STArray),
    Majorities(16, Type.STArray),

    CloseResolution(1, Type.UInt8),
    Method(2, Type.UInt8),
    TransactionResult(3, Type.UInt8),

    TakerPaysCurrency(1, Type.Hash160),
    TakerPaysIssuer(2, Type.Hash160),
    TakerGetsCurrency(3, Type.Hash160),
    TakerGetsIssuer(4, Type.Hash160),

    Paths(1, Type.PathSet),

    Indexes(1, Type.Vector256),
    Hashes(2, Type.Vector256),
    Amendments(3, Type.Vector256),

    Transaction(1, Type.Transaction),
    LedgerEntry(1, Type.LedgerEntry),
    Validation(1, Type.Validation);

    final int id;

    // defaults
    boolean signingField = true;
    boolean isSerialized = true;
    boolean isVlEncoded = false;

    public static Field fromString(String key) {
        Field f;
        try {
            f = valueOf(key);
        } catch (IllegalArgumentException e) {
            f = null;
        }
        return f;
    }

    public static byte[] asBytes(Field field) {
        int name = field.getId(), type = field.getType().getId();
        ArrayList<Byte> header = new ArrayList<Byte>(3);

        if (type < 16)
        {
            if (name < 16) // common type, common name
                header.add((byte)((type << 4) | name));
            else
            {
                // common type, uncommon name
                header.add((byte)(type << 4));
                header.add((byte)(name));
            }
        }
        else if (name < 16)
        {
            // uncommon type, common name
            header.add((byte)(name));
            header.add((byte)(type));
        }
        else
        {
            // uncommon type, uncommon name
            header.add((byte)(0));
            header.add((byte)(type));
            header.add((byte)(name));
        }

        byte[] headerBytes = new byte[header.size()];
        for (int i = 0; i < header.size(); i++) {
            headerBytes[i] = header.get(i);
        }

        return headerBytes;
    }

    public int getId() {
        return id;
    }

    final int code;
    final Type type;
    private final byte[] bytes;
    public Object tag = null;
    
    Field(int fid, Type tid) {
        id = fid;
        type = tid;
        code = (type.id << 16) | fid;
        if (isSerialized()) {
            bytes = asBytes(this);
        } else {
            bytes = null;
        }
        isSerialized = isSerialized(this);
    }

    static private HashMap<Integer, Field> byCode = new HashMap<Integer, Field>();

    public static Iterator<Field> sorted(Collection<Field> fields) {
        ArrayList<Field> fieldList = new ArrayList<Field>(fields);
        Collections.sort(fieldList, comparator);
        return fieldList.iterator();
    }

    static public Field fromCode(Integer integer) {
        return byCode.get(integer);
    }
    
    public Type getType() {
      return type;
    }

    public boolean isSerialized() {
        return isSerialized;
    }
    public boolean isVLEncoded() {
        return isVlEncoded;
    }
    public boolean isSigningField() {
        return signingField;
    }
    private static boolean isSerialized(Field f) {
        // This should screen out `hash` and `index` and the like
        return ((f.type.id > 0) && (f.type.id < 256) && (f.id > 0) && (f.id < 256));
    }

    static public Comparator<Field> comparator = new Comparator<Field>() {
        @Override
        public int compare(Field o1, Field o2) {
            return o1.code - o2.code;
        }
    };

    static {
        for (Field f : Field.values()) {
            byCode.put(f.code, f);
            f.isSerialized = isSerialized(f);
            f.signingField = f.isSerialized;

            switch (f.type) {
                case Blob:
                case AccountID:
                case Vector256:
                    f.isVlEncoded = true;
                    break;
                default:
                    break;
            }

        }

        TxnSignature.signingField = false;
        Signers.signingField = false;

        ArrayList<Field> sortedFields;
        Field[] values = Field.values();
        sortedFields = new ArrayList<Field>(Arrays.asList(values));
        Collections.sort(sortedFields, comparator);

        for (int i = 0; i < values.length; i++) {
            Field av = values[i];
            Field lv = sortedFields.get(i);
            if (av.code != lv.code) {
                throw new RuntimeException("Field enum declaration isn't presorted");
            }
        }
    }

    public byte[] getBytes() {
        return bytes;
    }
}