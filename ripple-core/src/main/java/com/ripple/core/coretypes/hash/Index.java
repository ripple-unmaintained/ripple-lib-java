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
        return prefixed256(LedgerSpace.bookDir)
                .add(pays.currency())
                .add(gets.currency())
                .add(pays.issuer())
                .add(gets.issuer())
                .finish();
    }

    /**
     *
     * @return a copy of index, with quality overlaid in lowest 8 bytes
     */
    public static Hash256 quality(Hash256 index, UInt64 quality) {
        byte[] qi = new byte[32];
        System.arraycopy(index.bytes(), 0, qi, 0, 24);
        if (quality != null) System.arraycopy(quality.toBytes(), 0, qi, 24, 8);
        return new Hash256(qi);
    }

    /**
     * @return A copy of index, with the lowest 8 bytes all zeroed.
     */
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

    /**
     *
     * @param rootIndex The RootIndex index for the directory node
     * @param nodeIndex nullable LowNode, HighNode, OwnerNode, BookNode etc
     *                  defining a `page` number.
     *
     * @return  A hash of rootIndex and nodeIndex when nodeIndex is non default
     *          else the rootIndex. This hash is used as an index for the next
     *          DirectoryNode page.
     */
    public static Hash256 directoryNode(Hash256 rootIndex, UInt64 nodeIndex) {
        if (nodeIndex == null || nodeIndex.isZero()) {
            return rootIndex;
        }

        return prefixed256(LedgerSpace.dirNode)
                .add(rootIndex)
                .add(nodeIndex)
                .finish();
    }

    public static Hash256 accountRoot(AccountID accountID) {
        return prefixed256(LedgerSpace.account).add(accountID).finish();
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
        if (end.length > 32) {
            byte[] source = end;
            end = new byte[32];
            System.arraycopy(source, source.length - 32, end, 0, 32);
        }
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
