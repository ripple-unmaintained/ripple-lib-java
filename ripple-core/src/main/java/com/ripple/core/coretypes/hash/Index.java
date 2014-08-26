package com.ripple.core.coretypes.hash;

import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Currency;
import com.ripple.core.coretypes.Issue;
import com.ripple.core.coretypes.hash.prefixes.HashPrefix;
import com.ripple.core.coretypes.hash.prefixes.LedgerSpace;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.coretypes.uint.UInt64;
import com.ripple.core.serialized.SerializedType;

import java.util.Arrays;
import java.util.List;

import static com.ripple.core.coretypes.hash.HalfSha512.prefixed256;
import static java.util.Collections.sort;

public class Index {
    private static Hash256 createBookBase(Issue pays, Issue gets) {
        HalfSha512 hasher = prefixed256(LedgerSpace.bookDir);

        pays.currency().toBytesSink(hasher);
        gets.currency().toBytesSink(hasher);
        pays.issuer().toBytesSink(hasher);
        gets.issuer().toBytesSink(hasher);

        return hasher.finish();
    }

    public static Hash256 quality(Hash256 index, UInt64 quality) {
        byte[] qi = new byte[32];
        System.arraycopy(index.bytes(), 0, qi, 0, 24);
        if (quality != null) System.arraycopy(quality.toBytes(), 0, qi, 24, 8);
        return new Hash256(qi);
    }

    private static Hash256 zeroQuality(Hash256 fullIndex) {
        return quality(fullIndex, null);
    }
    public static Hash256 rippleState(AccountID a1, AccountID a2, Currency currency) {
        List<AccountID> accounts = Arrays.asList(a1, a2);
        sort(accounts);
        return rippleState(accounts, currency);
    }
    public static Hash256 rippleState(List<AccountID> accounts, Currency currency) {
        HalfSha512 hasher = prefixed256(LedgerSpace.ripple);
        // Low then High
        for (AccountID account : accounts) account.toBytesSink(hasher);
        // Currency
        currency.toBytesSink(hasher);

        return hasher.finish();
    }

    public static Hash256 directoryNode(Hash256 base, UInt64 nodeIndex) {
        if (nodeIndex == null || nodeIndex.isZero()) {
            return base;
        }

        HalfSha512 hash = prefixed256(LedgerSpace.dirNode);

        for (SerializedType component : new SerializedType[]{base, nodeIndex})
            component.toBytesSink(hash);

        return hash.finish();
    }

    public static Hash256 accountRoot(AccountID accountID) {
        HalfSha512 hash = prefixed256(LedgerSpace.account);
        accountID.toBytesSink(hash);
        return hash.finish();
    }

    public static Hash256 ownerDirectory(AccountID account) {
        return Hash256.prefixedHalfSha512(LedgerSpace.ownerDir, account.bytes());
    }

    public static Hash256 transactionID(byte[] blob) {
        return Hash256.prefixedHalfSha512(HashPrefix.transactionID, blob);
    }

    public static Hash256 bookStart(Issue pays, Issue gets) {
        return zeroQuality(createBookBase(pays, gets));
    }

    public static Hash256 bookStart(Hash256 indexFromBookRange) {
        return zeroQuality(indexFromBookRange);
    }

    public static Hash256 bookEnd(Hash256 base) {
        byte[] end = base.bigInteger().add(Hash256.bookBaseSize).toByteArray();
        return new Hash256(end);
    }

    public static Hash256 ledgerHashes(long prev) {
        return prefixed256(LedgerSpace.skipList)
                    .add(new UInt32(prev >> 16))
                    .finish();
    }
    public static Hash256 ledgerHashes() {
        return prefixed256(LedgerSpace.skipList).finish();
    }
}
