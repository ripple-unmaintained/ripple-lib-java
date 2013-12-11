package com.ripple.client;

import com.ripple.client.subscriptions.AccountRoot;
import com.ripple.core.types.Amount;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class ServerAccountInfoTest {
    @Test
    public void  test_construction() throws JSONException {
        String info= "{\"Account\": \"rP1coskQzayaQ9geMdJgAV5f3tNZcHghzH\"," +
                "\"Balance\": \"99249214166\"," +
                "\"Flags\": 0," +
                "\"LedgerEntryType\": \"AccountRoot\"," +
                "\"OwnerCount\": 4," +
                "\"PreviousTxnID\": \"3AB06D64BABBF93FBF256663B1CF307A52066497E5C7379AB4E8CCAA9406AC24\"," +
                "\"PreviousTxnLgrSeq\": 2663174," +
                "\"Sequence\": 177," +
                "\"index\": \"D66D0EC951FD5707633BEBE74DB18B6D2DDA6771BA0FBF079AD08BFDE6066056\"}";

        JSONObject json = new JSONObject(info);
        AccountRoot accountRoot = new AccountRoot(json);
        assertEquals(accountRoot.Balance, Amount.fromDropString("99249214166"));
    }
}
