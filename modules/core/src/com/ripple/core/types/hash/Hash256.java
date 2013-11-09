package com.ripple.core.types.hash;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.HasField;
import com.ripple.core.serialized.TypeTranslator;
import com.ripple.core.types.AccountID;

import java.security.MessageDigest;

public class Hash256 extends HASH{

    public static final byte[] LEDGER_SPACE_ACCOUNT      = new byte[]{0, 'a'};
    public static final byte[] LEDGER_SPACE_DIR_NODE     = new byte[]{0, 'd'};
    public static final byte[] LEDGER_SPACE_GENERATOR    = new byte[]{0, 'g'};
    public static final byte[] LEDGER_SPACE_NICKNAME     = new byte[]{0, 'n'};
    public static final byte[] LEDGER_SPACE_RIPPLE       = new byte[]{0, 'r'};
    public static final byte[] LEDGER_SPACE_OFFER        = new byte[]{0, 'o'};  // Entry for an offer.
    public static final byte[] LEDGER_SPACE_OWNER_DIR    = new byte[]{0, 'O'};  // Directory of things owned by an account.
    public static final byte[] LEDGER_SPACE_BOOK_DIR     = new byte[]{0, 'B'};  // Directory of order books.
    public static final byte[] LEDGER_SPACE_CONTRACT     = new byte[]{0, 'c'};
    public static final byte[] LEDGER_SPACE_SKIP_LIST    = new byte[]{0, 's'};
    public static final byte[] LEDGER_SPACE_FEATURE      = new byte[]{0, 'f'};
    public static final byte[] LEDGER_SPACE_FEE          = new byte[]{0, 'e'};

    // transaction plus signature to give transaction ID
    public static final  byte[] HASH_PREFIX_TRANSACTION_ID           =  new byte[]{'T', 'X', 'N', 0};
    // inner transaction to sign
    public static final  byte[] HASH_PREFIX_TX_SIGN                  =  new byte[]{'S', 'T', 'X', 0};
    // transaction plus metadata
    public static final  byte[] HASH_PREFIX_TX_NODE                  =  new byte[]{'T', 'N', 'D', 0};
    // account state
    public static final  byte[] HASH_PREFIX_LEAF_NODE                =  new byte[]{'M', 'L', 'N', 0};
    // inner node in tree
    public static final  byte[] HASH_PREFIX_INNER_NODE               =  new byte[]{'M', 'I', 'N', 0};
    // ledger master data for signing
    public static final  byte[] HASH_PREFIX_LEDGER_MASTER            =  new byte[]{'L', 'G', 'R', 0};
    // validation for signing
    public static final  byte[] HASH_PREFIX_VALIDATION               =  new byte[]{'V', 'A', 'L', 0};
    // proposal for signing
    public static final  byte[] HASH_PREFIX_PROPOSAL                 =  new byte[]{'P', 'R', 'P', 0};
    // inner transaction to sign (TESTNET)
    public static final  byte[] HASH_PREFIX_TX_SIGN_TESTNET          =  new byte[]{'s', 't', 'x', 0};
    // validation for signing (TESTNET)
    public static final  byte[] HASH_PREFIX_VALIDATION_TESTNET       =  new byte[]{'v', 'a', 'l', 0};
    // proposal for signing (TESTNET)
    public static final  byte[] HASH_PREFIX_PROPOSAL_TESTNET         =  new byte[]{'p', 'r', 'p', 0};

    public Hash256(byte[] bytes) {
        super(bytes, 32);
    }

    public static Hash256 signingHash(byte[] blob) {
        return prefixedHalfSha512(HASH_PREFIX_TX_SIGN, blob);
    }

    public static class HalfSha512 {
        MessageDigest messageDigest;

        public HalfSha512() {
            try {
                messageDigest = MessageDigest.getInstance("SHA-512", "BC");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void update(byte[] bytes) {
            messageDigest.update(bytes);
        }

        public void update(Hash256 hash) {
            messageDigest.update(hash.getBytes());
        }

        public Hash256 finish() {
            byte[] digest = messageDigest.digest();
            byte[] half = new byte[32];
            System.arraycopy(digest, 0, half, 0, 32);

            return new Hash256(half);
        }
    }

    public static Hash256 prefixedHalfSha512(byte[] prefix, byte[] blob) {
        HalfSha512 messageDigest = new HalfSha512();
        messageDigest.update(prefix);
        messageDigest.update(blob);
        return messageDigest.finish();
    }

    public int nibblet(int depth) {
        int byte_ix = depth > 0 ? depth / 2 : 0;
        int b = super.hash[byte_ix];
        if (depth % 2 == 0) {
            b = (b & 0xF0) >> 4;
        } else {
            b = b & 0x0F;
        }
        return b;
    }

    @Override
    public TypeTranslator translator() {
        return translate;
    }

    public static Hash256 transactionID(byte[] blob) {
        return prefixedHalfSha512(HASH_PREFIX_TRANSACTION_ID, blob);
    }

    public static Hash256 accountIDLedgerIndex(AccountID accountID) {
        return prefixedHalfSha512(LEDGER_SPACE_ACCOUNT, accountID.bytes());
    }

    public static class Translator extends HashTranslator<Hash256> {
        @Override
        public Hash256 newInstance(byte[] b) {
            return new Hash256(b);
        }
    }
    public static Translator translate = new Translator();
    private Hash256(){}

    public abstract static class Hash256Field extends Hash256 implements HasField {}
    public static Hash256Field hash256Field(final Field f) {
        return new Hash256Field(){ @Override public Field getField() {return f;}};
    }

    static public Hash256Field LedgerHash = hash256Field(Field.LedgerHash);
    static public Hash256Field ParentHash = hash256Field(Field.ParentHash);
    static public Hash256Field TransactionHash = hash256Field(Field.TransactionHash);
    static public Hash256Field AccountHash = hash256Field(Field.AccountHash);
    static public Hash256Field PreviousTxnID = hash256Field(Field.PreviousTxnID);
    static public Hash256Field LedgerIndex = hash256Field(Field.LedgerIndex);
    static public Hash256Field WalletLocator = hash256Field(Field.WalletLocator);
    static public Hash256Field RootIndex = hash256Field(Field.RootIndex);
    static public Hash256Field BookDirectory = hash256Field(Field.BookDirectory);
    static public Hash256Field InvoiceID = hash256Field(Field.InvoiceID);
    static public Hash256Field Nickname = hash256Field(Field.Nickname);
    static public Hash256Field Feature = hash256Field(Field.Feature);

    static public Hash256Field hash = hash256Field(Field.hash);
    static public Hash256Field index = hash256Field(Field.index);
}
