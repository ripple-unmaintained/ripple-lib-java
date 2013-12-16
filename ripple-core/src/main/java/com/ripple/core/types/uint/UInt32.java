package com.ripple.core.types.uint;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.TypedFields;
import com.ripple.core.serialized.TypeTranslator;

import java.math.BigInteger;

public class UInt32 extends UINT<UInt32> {
    public static TypeTranslator<UInt32> translate = new UINTTranslator<UInt32>() {
        @Override
        public UInt32 newInstance(BigInteger i) {
            return new UInt32(i);
        }

        @Override
        public int byteWidth() {
            return 4;
        }
    };

    public UInt32(byte[] bytes) {
        super(bytes);
    }

    public UInt32(BigInteger value) {
        super(value);
    }

    public UInt32(Number s) {
        super(s);
    }
    public UInt32(String s) {
        super(s);
    }

    public UInt32(String s, int radix) {
        super(s, radix);
    }

    @Override
    public int getByteWidth() {
        return 4;
    }

    @Override
    public UInt32 instanceFrom(BigInteger n) {
        return new UInt32(n);
    }

    @Override
    public Long value() {
        return longValue();
    }

    private UInt32(){}

    private static TypedFields.UInt32Field int32Field(final Field f) {
        return new TypedFields.UInt32Field(){ @Override public Field getField() {return f;}};
    }

    static public TypedFields.UInt32Field Flags = int32Field(Field.Flags);
    static public TypedFields.UInt32Field SourceTag = int32Field(Field.SourceTag);
    static public TypedFields.UInt32Field Sequence = int32Field(Field.Sequence);
    static public TypedFields.UInt32Field PreviousTxnLgrSeq = int32Field(Field.PreviousTxnLgrSeq);
    static public TypedFields.UInt32Field LedgerSequence = int32Field(Field.LedgerSequence);
    static public TypedFields.UInt32Field CloseTime = int32Field(Field.CloseTime);
    static public TypedFields.UInt32Field ParentCloseTime = int32Field(Field.ParentCloseTime);
    static public TypedFields.UInt32Field SigningTime = int32Field(Field.SigningTime);
    static public TypedFields.UInt32Field Expiration = int32Field(Field.Expiration);
    static public TypedFields.UInt32Field TransferRate = int32Field(Field.TransferRate);
    static public TypedFields.UInt32Field WalletSize = int32Field(Field.WalletSize);
    static public TypedFields.UInt32Field OwnerCount = int32Field(Field.OwnerCount);
    static public TypedFields.UInt32Field DestinationTag = int32Field(Field.DestinationTag);
    static public TypedFields.UInt32Field HighQualityIn = int32Field(Field.HighQualityIn);
    static public TypedFields.UInt32Field HighQualityOut = int32Field(Field.HighQualityOut);
    static public TypedFields.UInt32Field LowQualityIn = int32Field(Field.LowQualityIn);
    static public TypedFields.UInt32Field LowQualityOut = int32Field(Field.LowQualityOut);
    static public TypedFields.UInt32Field QualityIn = int32Field(Field.QualityIn);
    static public TypedFields.UInt32Field QualityOut = int32Field(Field.QualityOut);
    static public TypedFields.UInt32Field StampEscrow = int32Field(Field.StampEscrow);
    static public TypedFields.UInt32Field BondAmount = int32Field(Field.BondAmount);
    static public TypedFields.UInt32Field LoadFee = int32Field(Field.LoadFee);
    static public TypedFields.UInt32Field OfferSequence = int32Field(Field.OfferSequence);
    static public TypedFields.UInt32Field FirstLedgerSequence = int32Field(Field.FirstLedgerSequence);
    static public TypedFields.UInt32Field LastLedgerSequence = int32Field(Field.LastLedgerSequence);
    static public TypedFields.UInt32Field TransactionIndex = int32Field(Field.TransactionIndex);
    static public TypedFields.UInt32Field OperationLimit = int32Field(Field.OperationLimit);
    static public TypedFields.UInt32Field ReferenceFeeUnits = int32Field(Field.ReferenceFeeUnits);
    static public TypedFields.UInt32Field ReserveBase = int32Field(Field.ReserveBase);
    static public TypedFields.UInt32Field ReserveIncrement = int32Field(Field.ReserveIncrement);
    static public TypedFields.UInt32Field SetFlag = int32Field(Field.SetFlag);
    static public TypedFields.UInt32Field ClearFlag = int32Field(Field.ClearFlag);
}
