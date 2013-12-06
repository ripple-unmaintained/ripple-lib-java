package com.ripple.core.known.sle;

import com.ripple.core.fields.Field;
import com.ripple.core.types.Amount;
import com.ripple.core.types.STObject;
import org.json.JSONObject;
import org.junit.Test;

public class OfferTest {
    @Test
    public void testHarness() throws Exception {
        // TODO: ...
        String offer = "{\"Account\": \"rEssC7sBh8ZCytFXBW4jpGfcb8XMRTgUmT\"," +
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
                " \"quality\": \"149980000\"}";

        STObject fields = STObject.fromJSONObject(new JSONObject(offer));

        Amount quality = (Amount) fields.get(Field.quality);
        Amount divide = fields.get(Amount.TakerPays)
                              .divide(
                                fields.get(Amount.TakerGets));

        Amount m = Amount.fromString("5/USD");

        System.out.println(divide);
        System.out.println(quality);
        System.out.println(quality.equals(divide));
        String s = quality.valueText();
        System.out.println(quality.value().floatValue());

        Amount thatMany = m.multiply(quality);
        System.out.println(thatMany);


//        for (Field field : fields) {
//            System.out.println(field);
//        }

    }
}
