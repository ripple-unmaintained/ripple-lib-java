package com.ripple.core.coretypes;

import com.ripple.core.coretypes.hash.Hash160;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.fields.Field;
import com.ripple.core.fields.TypedFields;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesSink;
import com.ripple.core.serialized.TypeTranslator;
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.crypto.ecdsa.Seed;
import com.ripple.encodings.common.B16;

import java.util.HashMap;
import java.util.Map;

import static com.ripple.config.Config.getB58IdentiferCodecs;

public class AccountID extends Hash160 {
    final public String address;

    public AccountID(byte[] bytes) {
        this(bytes, encodeAddress(bytes));
    }

    public AccountID(byte[] bytes, String address) {
        super(bytes);
        this.address = address;
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }

    public static AccountID ONE,
                            ZERO;

    static {
        ZERO = fromInteger(0);
        ONE = fromInteger(1);
    }

    @Override
    public String toString() {
        return address;
    }

    @Deprecated
    static public AccountID fromSeedString(String seed) {
        return fromKeyPair(Seed.getKeyPair(seed));
    }

    @Deprecated
    static public AccountID fromSeedBytes(byte[] seed) {
        return fromKeyPair(Seed.getKeyPair(seed));
    }

    public static AccountID fromKeyPair(IKeyPair kp) {
        byte[] bytes = kp.sha256_Ripemd160_Pub();
        return new AccountID(bytes, encodeAddress(bytes));
    }

    private static String encodeAddress(byte[] a) {
        return getB58IdentiferCodecs().encodeAddress(a);
    }

    static public AccountID fromInteger(Integer n) {
        // The hash160 will extend the address
        return fromBytes(new Hash160(new UInt32(n).toByteArray()).bytes());
    }

    public static AccountID fromBytes(byte[] bytes) {
        return new AccountID(bytes, encodeAddress(bytes));
    }

    static public AccountID fromAddress(String address) {
        byte[] bytes = getB58IdentiferCodecs().decodeAddress(address);
        return new AccountID(bytes, address);
    }

    static public AccountID fromAddressBytes(byte[] bytes) {
        return fromBytes(bytes);
    }

    public Issue issue(String code) {
        return new Issue(Currency.fromString(code), this);
    }

    @Override
    public Object toJSON() {
        return toString();
    }

    @Override
    public byte[] toBytes() {
        return translate.toBytes(this);
    }

    @Override
    public String toHex() {
        return translate.toHex(this);
    }

    @Override
    public void toBytesSink(BytesSink to) {
        to.add(bytes());
    }

    public static class Translator extends TypeTranslator<AccountID> {
        @Override
        public AccountID fromParser(BinaryParser parser, Integer hint) {
            if (hint == null) {
                hint = 20;
            }
            return AccountID.fromAddressBytes(parser.read(hint));
        }

        @Override
        public String toString(AccountID obj) {
            return obj.toString();
        }

        @Override
        public AccountID fromString(String value) {
            return AccountID.fromString(value);
        }
    }

    public static AccountID fromString(String value) {
        if (value.length() == 160 / 4) {
            return fromAddressBytes(B16.decode(value));
        } else {
            if (value.startsWith("r") && value.length() >= 26) {
                return fromAddress(value);
            }
            // This is potentially dangerous but fromString in
            // generic sense is used by Amount for parsing strings
            return accountForPassPhrase(value);
        }
    }

    static public Map<String, AccountID> accounts = new HashMap<String, AccountID>();

    public static AccountID accountForPassPhrase(String value) {

        if (accounts.get(value) == null) {
            accounts.put(value, accountForPass(value));
        }

        return accounts.get(value);
    }

    private static AccountID accountForPass(String value) {
        return AccountID.fromSeedBytes(Seed.passPhraseToSeedBytes(value));
    }

    static {
        accounts.put("root", accountForPass("masterpassphrase"));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AccountID) {
            return address.equals(((AccountID) obj).address);
        }
        else {
            return super.equals(obj);
        }
    }

    static public Translator translate = new Translator();

    public static TypedFields.AccountIDField accountField(final Field f) {
        return new TypedFields.AccountIDField() {
            @Override
            public Field getField() {
                return f;
            }
        };
    }

    static public TypedFields.AccountIDField Account = accountField(Field.Account);
    static public TypedFields.AccountIDField Owner = accountField(Field.Owner);
    static public TypedFields.AccountIDField Destination = accountField(Field.Destination);
    static public TypedFields.AccountIDField Issuer = accountField(Field.Issuer);
    static public TypedFields.AccountIDField Target = accountField(Field.Target);
    static public TypedFields.AccountIDField RegularKey = accountField(Field.RegularKey);
}
