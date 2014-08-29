package com.ripple.core.types.known.sle.entries;

import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Currency;
import com.ripple.core.coretypes.Issue;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.coretypes.uint.UInt64;
import com.ripple.core.enums.LedgerEntryType;
import com.ripple.core.fields.Field;
import com.ripple.core.fields.TypedFields;
import com.ripple.core.types.known.sle.ThreadedLedgerEntry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RippleState extends ThreadedLedgerEntry {
    public RippleState() {
        super(LedgerEntryType.RippleState);
    }

    public UInt32 highQualityIn() {return get(UInt32.HighQualityIn);}
    public UInt32 highQualityOut() {return get(UInt32.HighQualityOut);}
    public UInt32 lowQualityIn() {return get(UInt32.LowQualityIn);}
    public UInt32 lowQualityOut() {return get(UInt32.LowQualityOut);}
    public UInt64 lowNode() {return get(UInt64.LowNode);}
    public UInt64 highNode() {return get(UInt64.HighNode);}
    public Amount balance() {return get(Amount.Balance);}
    public Amount lowLimit() {return get(Amount.LowLimit);}
    public Amount highLimit() {return get(Amount.HighLimit);}
    public void highQualityIn(UInt32 val) {put(Field.HighQualityIn, val);}
    public void highQualityOut(UInt32 val) {put(Field.HighQualityOut, val);}
    public void lowQualityIn(UInt32 val) {put(Field.LowQualityIn, val);}
    public void lowQualityOut(UInt32 val) {put(Field.LowQualityOut, val);}
    public void lowNode(UInt64 val) {put(Field.LowNode, val);}
    public void highNode(UInt64 val) {put(Field.HighNode, val);}
    public void balance(Amount val) {put(Field.Balance, val);}
    public void lowLimit(Amount val) {put(Field.LowLimit, val);}
    public void highLimit(Amount val) {put(Field.HighLimit, val);}


    public AccountID lowAccount() {
        return lowLimit().issuer();
    }

    public AccountID highAccount() {
        return highLimit().issuer();
    }

    public List<AccountID> sortedAccounts() {
        return Arrays.asList(lowAccount(), highAccount());
    }

    public TypedFields.AmountField limitFieldFor(AccountID source) {
        if (lowAccount().equals(source)) {
            return Amount.LowLimit;
        }
        if (highAccount().equals(source)) {
            return Amount.HighLimit;
        } else {
            return null;
        }
    }

    public boolean isFor(AccountID source) {
        return lowAccount().equals(source) || highAccount().equals(source);
    }

    public boolean isFor(Issue issue) {
        return isFor(issue.issuer()) && balance().currency().equals(issue.currency());
    }

    // TODO, can optimize this
    public boolean isFor(AccountID s1, AccountID s2, Currency currency) {
        return currency.equals(balance().currency()) && isFor(s1) && isFor(s2);
    }

    public Currency currency() {
        return balance().currency();
    }

    public Amount balanceFor(AccountID owner) {
        TypedFields.AmountField field = limitFieldFor(owner);
        Amount balance = balance();
        AccountID issuer = lowAccount();
        if (field == Amount.HighLimit) {
            balance = balance.negate();
            issuer = highAccount();
        }
        return balance.newIssuer(issuer);
    }

    public Amount issued() {
        Amount balance = balance();
        if (balance.isNegative()) {
            // Balance is in terms of the LowAccount, so if the
            // balance is negative, that means it has issued
            return balance.negate().newIssuer(lowAccount());
        } else {
            // If it's positive, then the LowAccount has money
            // issued by the highAccount
            return balance.newIssuer(highAccount());
        }
    }

    //TODO, this logic needs looking over
    // owed in terms of which issuer?
//    public Amount owed() {
//        Amount balance = balance();
//        if (balance.isNegative()) {
//            return balance.newIssuer(lowAccount());
//        } else {
//            return balance.negate().newIssuer(highAccount());
//        }
//    }

}
