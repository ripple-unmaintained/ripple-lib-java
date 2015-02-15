package com.ripple.core.types.known.sle;

import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.STObject;
import com.ripple.core.types.known.sle.entries.Offer;
import org.json.JSONObject;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class OfferTest {
    @Test
    public void testHarness() throws Exception {
        // TODO: ...
        JSONObject offerJson = new JSONObject("{\"Account\": \"rEssC7sBh8ZCytFXBW4jpGfcb8XMRTgUmT\"," +
                " \"BookDirectory\": \"4627DFFCFF8B5A265EDBD8AE8C14A52325DBFEDAF4F5C32E5D05540F663BF000\"," +
                " \"BookNode\": \"0000000000000000\"," +
                " \"Flags\": 131072," +
                " \"LedgerEntryType\": \"Offer\"," +
                " \"OwnerNode\": \"0000000000000000\"," +
                " \"PreviousTxnID\": \"1E94B1C1CCAA94F92F43546833522D7CA7259AF42E0FC9B4FD1F1CD322EC2529\"," +
                " \"PreviousTxnLgrSeq\": 3441312," +
                " \"Sequence\": 86," +
                " \"TakerGets\": {\"currency\": \"USD\"," +
                "               \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\"," +
                "               \"value\": \"79\"}," +
                " \"TakerPays\": \"11848420000\"," +
                " \"index\": \"630579D43A800A7B8DE70F85C40536640DF94678A8018818BAD2986A17B1DC0A\"," +
                " \"quality\": \"149980000\"}");

        Offer offer = (Offer) STObject.fromJSONObject(offerJson);
        assertEquals(offer.askQuality(), Amount.fromString(offerJson.getString("quality")).value());
        assertEquals("USD", offer.getsOne().currencyString());
        assertEquals("XRP", offer.paysOne().currencyString());
    }
}
