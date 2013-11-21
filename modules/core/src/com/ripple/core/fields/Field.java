package com.ripple.core.fields;
import java.util.*;

public enum Field {
    // These are all presorted (verified in a static block below)
    // They can then be used in a TreeMap, using the Enum (private) ordinal
    // comparator
    Generic(0, Type.UNKNOWN),
    Invalid(-1, Type.UNKNOWN),
    LedgerEntryType(1, Type.UINT16),
    TransactionType(2, Type.UINT16),
    Flags(2, Type.UINT32),
    SourceTag(3, Type.UINT32),
    Sequence(4, Type.UINT32),
    PreviousTxnLgrSeq(5, Type.UINT32),
    LedgerSequence(6, Type.UINT32),
    CloseTime(7, Type.UINT32),
    ParentCloseTime(8, Type.UINT32),
    SigningTime(9, Type.UINT32),
    Expiration(10, Type.UINT32),
    TransferRate(11, Type.UINT32),
    WalletSize(12, Type.UINT32),
    OwnerCount(13, Type.UINT32),
    DestinationTag(14, Type.UINT32),
    HighQualityIn(16, Type.UINT32),
    HighQualityOut(17, Type.UINT32),
    LowQualityIn(18, Type.UINT32),
    LowQualityOut(19, Type.UINT32),
    QualityIn(20, Type.UINT32),
    QualityOut(21, Type.UINT32),
    StampEscrow(22, Type.UINT32),
    BondAmount(23, Type.UINT32),
    LoadFee(24, Type.UINT32),
    OfferSequence(25, Type.UINT32),
    FirstLedgerSequence(26, Type.UINT32),
    LastLedgerSequence(27, Type.UINT32),
    TransactionIndex(28, Type.UINT32),
    OperationLimit(29, Type.UINT32),
    ReferenceFeeUnits(30, Type.UINT32),
    ReserveBase(31, Type.UINT32),
    ReserveIncrement(32, Type.UINT32),
    SetFlag(33, Type.UINT32),
    ClearFlag(34, Type.UINT32),
    IndexNext(1, Type.UINT64),
    IndexPrevious(2, Type.UINT64),
    BookNode(3, Type.UINT64),
    OwnerNode(4, Type.UINT64),
    BaseFee(5, Type.UINT64),
    ExchangeRate(6, Type.UINT64),
    LowNode(7, Type.UINT64),
    HighNode(8, Type.UINT64),
    EmailHash(1, Type.HASH128),
    LedgerHash(1, Type.HASH256),
    ParentHash(2, Type.HASH256),
    TransactionHash(3, Type.HASH256),
    AccountHash(4, Type.HASH256),
    PreviousTxnID(5, Type.HASH256),
    LedgerIndex(6, Type.HASH256),
    WalletLocator(7, Type.HASH256),
    RootIndex(8, Type.HASH256),
    BookDirectory(16, Type.HASH256),
    InvoiceID(17, Type.HASH256),
    Nickname(18, Type.HASH256),
    Feature(19, Type.HASH256),
    hash(257, Type.HASH256),
    index(258, Type.HASH256),
    Amount(1, Type.AMOUNT),
    Balance(2, Type.AMOUNT),
    LimitAmount(3, Type.AMOUNT),
    TakerPays(4, Type.AMOUNT),
    TakerGets(5, Type.AMOUNT),
    LowLimit(6, Type.AMOUNT),
    HighLimit(7, Type.AMOUNT),
    Fee(8, Type.AMOUNT),
    SendMax(9, Type.AMOUNT),
    MinimumOffer(16, Type.AMOUNT),
    RippleEscrow(17, Type.AMOUNT),
    PublicKey(1, Type.VL),
    MessageKey(2, Type.VL),
    SigningPubKey(3, Type.VL),
    TxnSignature(4, Type.VL),
    Generator(5, Type.VL),
    Signature(6, Type.VL),
    Domain(7, Type.VL),
    FundCode(8, Type.VL),
    RemoveCode(9, Type.VL),
    ExpireCode(10, Type.VL),
    CreateCode(11, Type.VL),
    Account(1, Type.ACCOUNT),
    Owner(2, Type.ACCOUNT),
    Destination(3, Type.ACCOUNT),
    Issuer(4, Type.ACCOUNT),
    Target(7, Type.ACCOUNT),
    RegularKey(8, Type.ACCOUNT),
    TransactionMetaData(2, Type.OBJECT),
    CreatedNode(3, Type.OBJECT),
    DeletedNode(4, Type.OBJECT),
    ModifiedNode(5, Type.OBJECT),
    PreviousFields(6, Type.OBJECT),
    FinalFields(7, Type.OBJECT),
    NewFields(8, Type.OBJECT),
    TemplateEntry(9, Type.OBJECT),
    SigningAccounts(2, Type.ARRAY),
    TxnSignatures(3, Type.ARRAY),
    Signatures(4, Type.ARRAY),
    Template(5, Type.ARRAY),
    Necessary(6, Type.ARRAY),
    Sufficient(7, Type.ARRAY),
    AffectedNodes(8, Type.ARRAY),
    CloseResolution(1, Type.UINT8),
    TemplateEntryType(2, Type.UINT8),
    TransactionResult(3, Type.UINT8),
    TakerPaysCurrency(1, Type.HASH160),
    TakerPaysIssuer(2, Type.HASH160),
    TakerGetsCurrency(3, Type.HASH160),
    TakerGetsIssuer(4, Type.HASH160),
    Paths(1, Type.PATHSET),
    Indexes(1, Type.VECTOR256),
    Hashes(2, Type.VECTOR256),
    Features(3, Type.VECTOR256),
    Transaction(1, Type.TRANSACTION),
    LedgerEntry(1, Type.LEDGERENTRY),
    Validation(1, Type.VALIDATION);

    final int id;

    public int getId() {
        return id;
    }

    final int code;
    final Type type;
    public Object tag = null;
    
    Field(int fid, Type tid) {
        id = fid;
        type = tid;
        code = (type.id << 16) | fid;
    }
    
    static private Map<Integer, Field> byCode = new TreeMap<Integer, Field>();

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
        // This should screen out `hash` and `index`
        return ((type.id > 0) && (type.id < 256) && (id > 0) && (id < 256));
    }


    public boolean isVLEncoded() {
        return type == Type.VL || type == Type.ACCOUNT || type == Type.VECTOR256;
    }

    //
    public boolean isSigningField() {
        return isSerialized() && this != TxnSignature;
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
        }

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

}