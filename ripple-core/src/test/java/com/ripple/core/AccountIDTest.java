package com.ripple.core;

import com.ripple.core.coretypes.AccountID;
import com.ripple.encodings.base58.EncodingFormatException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AccountIDTest {

    private String randomXqvWyhPcWjBE7nawXLTKH5YLNmSc = "randomXqvWyhPcWjBE7nawXLTKH5YLNmSc";

    @Test
    public void testAddress() {
        AccountID account = AccountID.fromSeedString(TestFixtures.master_seed);
        assertEquals(TestFixtures.master_seed_address, account.address);
    }

    @Test
    public void testBlackHoleAddy() {
        AccountID.fromAddress(randomXqvWyhPcWjBE7nawXLTKH5YLNmSc);
    }

    @Test(expected = EncodingFormatException.class)
    public void testBlackHoleAddyCheckSumFail() {
        AccountID.fromAddress("R" + randomXqvWyhPcWjBE7nawXLTKH5YLNmSc.substring(1));
    }

    @Test
    public void testHashCode() {
        AccountID a1 = AccountID.fromAddress(randomXqvWyhPcWjBE7nawXLTKH5YLNmSc);
        AccountID a2 = AccountID.fromAddress(randomXqvWyhPcWjBE7nawXLTKH5YLNmSc);
        assertEquals(a1.hashCode(), a2.hashCode());
    }
}
