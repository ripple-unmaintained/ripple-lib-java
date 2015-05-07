package com.ripple.core.coretypes;

import com.ripple.core.coretypes.hash.Hash160;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.Index;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.fields.AccountIDField;
import com.ripple.core.fields.Field;
import com.ripple.core.fields.Type;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesSink;
import com.ripple.core.serialized.TypeTranslator;
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.crypto.ecdsa.Seed;
import com.ripple.encodings.common.B16;

import java.util.HashMap;
import java.util.Map;

import static com.ripple.config.Config.getB58IdentiferCodecs;

/**
 * Originally it was intended that AccountIDs would be variable length so that's
 * why they are variable length encoded as top level field objects.
 *
 * Note however, that in practice, all account ids are just 160 bit hashes.
 * Consider the fields TakerPaysIssuer and fixed length encoding of issuers in
 * amount serializations.
 *
 * Thus, we extend Hash160 which affords us some functionality.
 */
public class AccountID extends Hash160 {
    // We can set aliases, so fromString(x) will return a given AccountID
    // this is currently only used for tests, and not recommended to be used
    // elsewhere.
    public static Map<String, AccountID> aliases = new HashMap<String, AccountID>();
    //
    public static AccountID NEUTRAL = fromInteger(1), XRP_ISSUER = fromInteger(0);
    final public String address;

    public AccountID(byte[] bytes) {
        this(bytes, encodeAddress(bytes));
    }

    public AccountID(byte[] bytes, String address) {
        super(bytes);
        this.address = address;
    }

    // Static from* constructors
    public static AccountID fromString(String value) {
        if (value.length() == 160 / 4) {
            return fromAddressBytes(B16.decode(value));
        } else {
            if (value.startsWith("r") && value.length() >= 26) {
                return fromAddress(value);
            }
            AccountID accountID = accountForAlias(value);
            if (accountID == null) {
                throw new UnknownAlias("Alias unset: " + value);
            }
            return accountID;
        }
    }

    static public AccountID fromAddress(String address) {
        byte[] bytes = getB58IdentiferCodecs().decodeAddress(address);
        return new AccountID(bytes, address);
    }

    public static AccountID fromKeyPair(IKeyPair kp) {
        byte[] bytes = kp.pub160Hash();
        return new AccountID(bytes, encodeAddress(bytes));
    }

    public static AccountID fromPassPhrase(String phrase) {
        return fromKeyPair(Seed.fromPassPhrase(phrase).keyPair());
    }

    static public AccountID fromSeedString(String seed) {
        return fromKeyPair(Seed.getKeyPair(seed));
    }

    static public AccountID fromSeedBytes(byte[] seed) {
        return fromKeyPair(Seed.getKeyPair(seed));
    }

    static public AccountID fromInteger(Integer n) {
        // The hash160 constructor will extend the 4bytes address
        return fromBytes(new Hash160(new UInt32(n).toByteArray()).bytes());
    }

    public static AccountID fromBytes(byte[] bytes) {
        return new AccountID(bytes, encodeAddress(bytes));
    }

    static public AccountID fromAddressBytes(byte[] bytes) {
        return fromBytes(bytes);
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }

    @Override
    public String toString() {
        return address;
    }

    public Issue issue(String code) {
        return new Issue(Currency.fromString(code), this);
    }

    public Issue issue(Currency c) {
        return new Issue(c, this);
    }

    public boolean isNativeIssuer() {
        return equals(XRP_ISSUER);
    }

    // SerializedType interface implementation
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

    @Override
    public Type type() {
        return Type.AccountID;
    }

    public Hash256 lineIndex(Issue issue) {
        if (issue.isNative()) throw new AssertionError();
        return Index.rippleState(this, issue.issuer(), issue.currency());
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

    //

    static public Translator translate = new Translator();

    // helpers

    private static String encodeAddress(byte[] a) {
        return getB58IdentiferCodecs().encodeAddress(a);
    }

    public static AccountID addAliasFromPassPhrase(String n, String n2) {
        return aliases.put(n, fromPassPhrase(n2));
    }

    public static AccountID accountForAlias(String value) {
        return aliases.get(value);
    }

    // Typed field definitions
    public static AccountIDField accountField(final Field f) {
        return new AccountIDField() {
            @Override
            public Field getField() {
                return f;
            }
        };
    }

    static public AccountIDField Account = accountField(Field.Account);
    static public AccountIDField Owner = accountField(Field.Owner);
    static public AccountIDField Destination = accountField(Field.Destination);
    static public AccountIDField Issuer = accountField(Field.Issuer);
    static public AccountIDField Target = accountField(Field.Target);
    static public AccountIDField RegularKey = accountField(Field.RegularKey);

    // Exceptions
    public static class UnknownAlias extends RuntimeException {
        /**
         *
         */
        private static final long serialVersionUID = -8042838677708510072L;

        public UnknownAlias(String s) {
            super(s);
        }
    }
}
