package com.ripple.client.subscriptions;

import com.ripple.client.Client;
import com.ripple.client.pubsub.Publisher;
import com.ripple.core.types.*;
import com.ripple.core.types.hash.Hash256;
import com.ripple.core.types.uint.UInt32;
import org.json.JSONObject;

import java.math.BigInteger;

public class AccountRoot extends Publisher<AccountRoot.events> {
    public static abstract class events<T> extends Publisher.Callback<T> {}
    public static abstract class OnUpdate extends events<AccountRoot> {}
    boolean updated = false;

    public boolean primed() {
        return updated;
    }

    public void updateFromTransaction(Hash256 transactionHash, UInt32 transactionLedgerIndex, STObject rootUpdates) {
        if (PreviousTxnID.equals(rootUpdates.get(Hash256.PreviousTxnID)) ) {
            setFromSTObject(rootUpdates.get(STObject.FinalFields));
            PreviousTxnID = transactionHash;
            PreviousTxnLgrSeq = transactionLedgerIndex;
        } else {
            Client.log("hrmmm .... "); // We should keep track of these and try and form a chain
        }
    }

    public AccountID     Account;
    public Amount        Balance;
    public UInt32        Sequence;
    public UInt32        OwnerCount;
    public UInt32        Flags;
    public Hash256       PreviousTxnID;
    public UInt32        PreviousTxnLgrSeq;

    public AccountRoot(JSONObject object){setFromJSON(object);    }
    public AccountRoot(STObject   object){setFromSTObject(object);}
    public AccountRoot()   {}

    public void setFromJSON(JSONObject jsonObject) {
        setFromSTObject(STObject.translate.fromJSONObject(jsonObject));
    }

    public void setUnfundedAccount(AccountID account) {
        Account = account;
        Balance = Amount.fromString("0");
        Sequence = new UInt32(1);
        OwnerCount = new UInt32(0);
        Flags = new UInt32(0);
        PreviousTxnID = new Hash256(new byte[32]);
        PreviousTxnLgrSeq = new UInt32(0);

        notifyUpdate();
    }

    public void setFromSTObject(STObject so) {

        if (so.has(AccountID.Account))         Account            = so.get(AccountID.Account);
        if (so.has(Amount.Balance))            Balance            = so.get(Amount.Balance);
        if (so.has(UInt32.Sequence))           Sequence           = so.get(UInt32.Sequence);
        if (so.has(UInt32.OwnerCount))         OwnerCount         = so.get(UInt32.OwnerCount);
        if (so.has(UInt32.Flags))              Flags              = so.get(UInt32.Flags);
        if (so.has(Hash256.PreviousTxnID))     PreviousTxnID      = so.get(Hash256.PreviousTxnID);
        if (so.has(UInt32.PreviousTxnLgrSeq))  PreviousTxnLgrSeq  = so.get(UInt32.PreviousTxnLgrSeq);

        notifyUpdate();
    }

    private void notifyUpdate() {
        updated = true;
        emit(OnUpdate.class, this);
    }

}
