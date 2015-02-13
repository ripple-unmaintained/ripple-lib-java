package com.ripple.core.coretypes.hash;

import com.ripple.core.coretypes.AccountID;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class Hash256Test {
    @Test
    public void testAccountIDLedgerIndex() throws Exception {
        String addy = "rP1coskQzayaQ9geMdJgAV5f3tNZcHghzH";
        String LedgerIndex = "D66D0EC951FD5707633BEBE74DB18B6D2DDA6771BA0FBF079AD08BFDE6066056";
        Hash256 expectedLedgerIndex = Hash256.translate.fromString(LedgerIndex);
        AccountID accountID = AccountID.fromAddress(addy);
        Hash256 builtLedgerIndex = Index.accountRoot(accountID);
        assertEquals(expectedLedgerIndex, builtLedgerIndex);
    }

    @Test
    public void testCompareTo() throws Exception {

        Hash256 base = Hash256.fromHex("FA745475043B71C0C25A0C7ACA18E9E8CF99500CEAE366B00000000000000000"),
                leaf = Hash256.fromHex("FA745475043B71C0C25A0C7ACA18E9E8CF99500CEAE366B05319606406AE6DB7");

        assertTrue(leaf.compareTo(base) == 1);
    }
}
