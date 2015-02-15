package com.ripple.core.types.known.sle.entries;

import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Currency;
import com.ripple.core.coretypes.Issue;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.Index;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.coretypes.uint.UInt64;
import com.ripple.core.enums.LedgerFlag;
import com.ripple.core.fields.AmountField;
import com.ripple.core.fields.Field;
import com.ripple.core.serialized.enums.LedgerEntryType;
import com.ripple.core.types.known.sle.ThreadedLedgerEntry;

import java.util.Arrays;
import java.util.List;

public class RippleState extends ThreadedLedgerEntry {
    /**
    The RippleState is a ledger entry which roughly speaking defines the balance and
    trust limits between two accounts.

    Like all current ledger entries, it has a canonical form for hashing that
    doesn't necessarily communicate the information in a way that is clear or
    obvious to a human. The current json format for some ledger entries is very
    close to the hashing format, and isn't any better.

    The two accounts on the link are categorized into a low account, and a high
    account by comparing the 160bits of their account id as a big endian unsigned
    integer.

    There is one and only one `Balance` stored, using an amount struct with a
    neutral `issuer`. ( A uint160 with the numerical value of `1` was chosen as a
    placeholder, as the canonical way to represent a null account )

    The Balance can be negative, zero, or positive and is in terms of the Low
    account, such that when it's positive, it defines how much credit the High
    account has issued.

    Between any account, there can be two types of balance changes, issuance and
    redemption. A redemption is the transferal of previously issued IOUs back to the
    owner.

    This implies that for any account, that can they can make a transferal using
    funds from two distinct balances. After making this distinction it becomes
    nonsensical to say that an account holds negative IOUs from the opposite line.

    Thus, the one Balance in a ripple state actually implies 4 distinct balances.

    ```
    {
     ...
     "Balance" : {
        "currency" : "USD",
        "issuer" : "rrrrrrrrrrrrrrrrrrrrBZbvji",
        "value" : "100"
     },
     "HighLimit" : {
        "currency" : "USD",
        "issuer" : "rPMh7Pi9ct699iZUTWaytJUoHcJ7cgyziK",
        "value" : "500"
     },
     "LowLimit" : {
        "currency" : "USD",
        "issuer" : "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",
        "value" : "500"
     },
    }
    ```

    lowAccount = LowLimit.issuer
    hiAccount = HighLimit.issuer

    Balances:
        lowAccount has 100/USD/highAccount
        lowAccount has 0/USD/lowAccount on the line

        highAccount has 0/USD/lowAccount
        highAccount has -100/USD/highAccount on the line


    * */

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

    public AmountField limitFieldFor(AccountID source) {
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

    private Amount issuedBy(boolean hi) {
        Amount balance;

        if (hi) {
            balance = balance().newIssuer(highAccount());
        } else {
            balance = balance().negate().newIssuer(lowAccount());
        }

        if (!balance.isPositive()) {
            balance = balance.issue().amount(0);
        }
        return balance;
    }

    public Amount issuedByHigh() {
        return issuedBy(true);
    }
    public Amount issuedByLow() {
        return issuedBy(false);
    }

    public Amount issuedTo(AccountID accountID) {
        return issuedBy(isLowAccount(accountID));
    }

    @Deprecated() // "not deprecated but needs fixing"
    public boolean authorizedBy(AccountID account) {
        UInt32 flags = flags();
        return flags == null || flags.testBit(isHighAccount(account) ? LedgerFlag.HighAuth : LedgerFlag.LowAuth);
    }

    private boolean isBitSet(int flags, int flag) {
        return (flags & flag) != 0;
    }

    private boolean isHighAccount(AccountID account) {
        return highAccount().equals(account);
    }
    private boolean isLowAccount(AccountID account) {
        return lowAccount().equals(account);
    }


    public Hash256 lowNodeOwnerDirectory() {
        Hash256 ownerDir = Index.ownerDirectory(lowAccount());
        return Index.directoryNode(ownerDir, lowNode());
    }
    public Hash256 highNodeOwnerDirectory() {
        Hash256 ownerDir = Index.ownerDirectory(highAccount());
        return Index.directoryNode(ownerDir, highNode());
    }

    public Hash256[] directoryIndexes() {
        return new Hash256[]{lowNodeOwnerDirectory(), highNodeOwnerDirectory()};
    }

    @Override
    public void setDefaults() {
        super.setDefaults();

        if (lowNode() == null) {
            lowNode(UInt64.ZERO);
        }
        if (highNode() == null) {
            highNode(UInt64.ZERO);
        }
    }
}
