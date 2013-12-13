package com.ripple.core.types;

import static com.ripple.config.Config.getB58IdentiferCodecs;

import java.util.HashMap;
import java.util.Map;

import com.ripple.encodings.common.B16;
import org.ripple.bouncycastle.util.encoders.Hex;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.TypedFields;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesTree;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import com.ripple.core.types.hash.Hash160;
import com.ripple.core.types.uint.UInt32;
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.crypto.ecdsa.Seed;
import com.ripple.utils.Utils;

public class AccountID implements SerializedType, Comparable<AccountID> {
    public String masterSeed;
    public String address;
    protected IKeyPair keyPair;
    protected byte[] addressBytes;

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

    static public AccountID fromSeedString(String masterSeed) {
        AccountID a = new AccountID();
        populateFieldsFromKeyPair(a, keyPairFromSeedString(masterSeed));
        return a;
    }

    static public AccountID fromSeedBytes(byte[] masterSeed) {
        AccountID a = new AccountID();
        populateFieldsFromKeyPair(a, keyPairFromSeedBytes(masterSeed));
        return a;
    }

    private static void populateFieldsFromKeyPair(AccountID a, IKeyPair kp) {
        a.keyPair = kp;
        a.addressBytes = Utils.SHA256_RIPEMD160(kp.pub().toByteArray());
        a.address = getB58IdentiferCodecs().encodeAddress(a.bytes());
    }

    static public AccountID fromInteger(Integer n) {
        AccountID a = new AccountID();
        a.addressBytes = new Hash160(new UInt32(n).toByteArray()).bytes();
        a.address = getB58IdentiferCodecs().encodeAddress(a.bytes());
        return a;
    }

    static public AccountID fromAddress(String address) {
        AccountID a = new AccountID();
        a.keyPair = null;
        a.addressBytes = getB58IdentiferCodecs().decodeAddress(address);
        a.address = address;
        return a;
    }

    static public AccountID fromAddressBytes(byte[] bytes) {
        AccountID a = new AccountID();
        a.keyPair = null;
        a.addressBytes = bytes;
        a.address = getB58IdentiferCodecs().encodeAddress(bytes);
        return a;
    }

    public static IKeyPair keyPairFromSeedString(String master_seed) {
        return keyPairFromSeedBytes(getB58IdentiferCodecs().decodeFamilySeed(master_seed));
    }

    public static IKeyPair keyPairFromSeedBytes(byte[] master_seed) {
        return Seed.createKeyPair(master_seed);
    }

    public IKeyPair getKeyPair() {
        return keyPair;
    }

    public byte[] bytes() {
        return addressBytes;
    }

    @Override
    public int compareTo(AccountID o) {
        return address.compareTo(o.address);
    }

    public Issue issue(String code) {
        return new Issue(Currency.fromString(code), this);
    }

    static class Translator extends TypeTranslator<AccountID> {

        @Override
        public AccountID fromParser(BinaryParser parser, Integer hint) {
            return AccountID.fromAddressBytes(parser.read(20));
        }

        @Override
        public Object toJSON(AccountID obj) {
            return toString(obj);
        }

        @Override
        public String toString(AccountID obj) {
            return obj.toString();
        }

        @Override
        public AccountID fromString(String value) {
            return AccountID.fromString(value);
        }

//        @Override
//        public byte[] toBytesTree(AccountID obj) {
//            return obj.bytes();
//        }

        @Override
        public void toBytesTree(AccountID obj, BytesTree to) {
            to.add(obj.bytes());
        }
    }

    public static AccountID fromString(String value) {
        // TODO No valid addresses should ever fail below condition
        if (value.startsWith("r") && value.length() >= 26) {
            return AccountID.fromAddress(value);
        } else if (value.length() == 160 / 4) {
            return AccountID.fromAddressBytes(B16.decode(value));
        } else {
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

    protected AccountID() {
    }

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
