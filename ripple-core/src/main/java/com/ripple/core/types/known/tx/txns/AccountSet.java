package com.ripple.core.types.known.tx.txns;

import com.ripple.core.coretypes.VariableLength;
import com.ripple.core.coretypes.hash.Hash128;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.serialized.enums.TransactionType;
import com.ripple.core.fields.Field;
import com.ripple.core.types.known.tx.Transaction;

public class AccountSet extends Transaction{
    public AccountSet() {
        super(TransactionType.AccountSet);
    }
    public UInt32 transferRate() {return get(UInt32.TransferRate);}
    public UInt32 walletSize() {return get(UInt32.WalletSize);}
    public UInt32 setFlag() {return get(UInt32.SetFlag);}
    public UInt32 clearFlag() {return get(UInt32.ClearFlag);}
    public Hash128 emailHash() {return get(Hash128.EmailHash);}
    public Hash256 walletLocator() {return get(Hash256.WalletLocator);}
    public VariableLength messageKey() {return get(VariableLength.MessageKey);}
    public VariableLength domain() {return get(VariableLength.Domain);}
    public void transferRate(UInt32 val) {put(Field.TransferRate, val);}
    public void walletSize(UInt32 val) {put(Field.WalletSize, val);}
    public void setFlag(UInt32 val) {put(Field.SetFlag, val);}
    public void clearFlag(UInt32 val) {put(Field.ClearFlag, val);}
    public void emailHash(Hash128 val) {put(Field.EmailHash, val);}
    public void walletLocator(Hash256 val) {put(Field.WalletLocator, val);}
    public void messageKey(VariableLength val) {put(Field.MessageKey, val);}
    public void domain(VariableLength val) {put(Field.Domain, val);}

}
