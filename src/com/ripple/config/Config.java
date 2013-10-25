package com.ripple.config;

import com.ripple.encodings.B58IdentiferCodecs;
import com.ripple.encodings.base58.Base58;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

// Somewhat of a global registry, dependency injection ala guice would be nicer, but trying to KISS
public class Config {
    public  static final String DEFAULT_ALPHABET = "rpshnaf39wBUDNEGHJKLM4PQRST7VWXYZ2bcdeCg65jkm8oFqi1tuvAxyz";
    public final static long HASH_SIGN = 0x53545800, HASH_SIGN_TESTNET = 0x73747800;

    private static B58IdentiferCodecs b58IdentiferCodecs;
    private static double feeCushion;
    private static long signingPrefix;

    public static void setAlphabet(String alphabet) {
        Base58 base58 = new Base58(alphabet);
        b58IdentiferCodecs = new B58IdentiferCodecs(base58);
    }


    /**
     * @return the configured B58IdentiferCodecs object
     */
    public static B58IdentiferCodecs getB58IdentiferCodecs() {
        return b58IdentiferCodecs;
    }
    public static long getSigningPrefix() {
        return signingPrefix;
    }
    static public void initBouncy() {
        Security.addProvider(new BouncyCastleProvider());
    }
    static public void setSigningPrefix(long prefix) {
        signingPrefix = prefix;
    }

    /***
     * We set up all the defaults here
     */
    static {
        setAlphabet(DEFAULT_ALPHABET); // XXX: what about AccountID class `static` block caching of seeds?
        setFeeCushion(1.5);
        initBouncy();
        setSigningPrefix(HASH_SIGN);
    }

    public static double getFeeCushion() {
        return feeCushion;
    }

    public static void setFeeCushion(double fee_cushion) {
        feeCushion = fee_cushion;
    }

}
