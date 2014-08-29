package com.ripple.core.coretypes.hash;

import com.ripple.core.coretypes.AccountID;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class Hash256Test {
    @Test
    public void testAccountIDLedgerIndex() throws Exception {
        String addy = "rP1coskQzayaQ9geMdJgAV5f3tNZcHghzH";
        String LedgerIndex = "D66D0EC951FD5707633BEBE74DB18B6D2DDA6771BA0FBF079AD08BFDE6066056";
        Hash256 expectedLedgerIndex = Hash256.translate.fromString(LedgerIndex);
        AccountID accountID = AccountID.fromAddress(addy);
        Hash256 builtLedgerIndex = Index.accountID(accountID);
        assertEquals(expectedLedgerIndex, builtLedgerIndex);
    }
}
