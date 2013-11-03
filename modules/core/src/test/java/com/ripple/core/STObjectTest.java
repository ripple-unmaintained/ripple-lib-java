package com.ripple.core;

import com.ripple.core.enums.LedgerEntryType;
import com.ripple.core.enums.TransactionEngineResult;
import com.ripple.core.fields.Field;
import com.ripple.core.formats.TxFormat;
import com.ripple.core.types.*;
import com.ripple.core.types.uint.UInt16;
import com.ripple.core.types.uint.UInt32;
import com.ripple.core.types.uint.UInt64;
import com.ripple.core.types.uint.UInt8;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class STObjectTest {
    @Test
    public void testTypeInference() {

        STObject so = STObject.newInstance();
        so.put(Field.valueOf("LowLimit"), "10.0/USD");
        so.put(Amount.Balance, "125.0");

        assertEquals(so.get(Amount.Balance).toDropsString(), "125000000");
        assertEquals(so.get(Amount.LowLimit).currencyString(), "USD");

        assertNotNull(so.get(Amount.LowLimit));
        assertNull(so.get(Amount.HighLimit));
    }


    @Test
    /**
     * We just testing this won't blow up due to unknown `date` field!
     */
    public void testfromJSONObjectWithUnknownFields() throws JSONException {

        String json = "{\"date\": 434707820,\n" +
                "\"hash\": \"66347806574036FD3D3E9FDA20A411FA8B2D26AA3C3725A107FCF0050F1E4B86\"}";

        STObject so = STObject.fromJSONObject(new JSONObject(json));
    }

    String metaString = "{\"AffectedNodes\": [{\"ModifiedNode\": {\"FinalFields\": {\"Account\": \"rwMyB1diFJ7xqEKYGYgk9tKrforvTr33M5\","+
            "\"Balance\": \"286000447\","+
            "\"Flags\": 0,"+
            "\"OwnerCount\": 4,"+
            "\"Sequence\": 35},"+
            "\"LedgerEntryType\": \"AccountRoot\","+
            "\"LedgerIndex\": \"32FE2333B117B257F3AB58E1CB15A6533DC27FDD61FEB1027858D367B40B559A\","+
            "\"PreviousFields\": {\"Balance\": \"286000463\","+
            "\"Sequence\": 34},"+
            "\"PreviousTxnID\": \"33562B82489F263F173801272D02178C0018A40ACFDC84B59976CE7C163F41FC\","+
            "\"PreviousTxnLgrSeq\": 2681281}},"+
            "{\"ModifiedNode\": {\"FinalFields\": {\"Account\": \"rP1coskQzayaQ9geMdJgAV5f3tNZcHghzH\","+
            "\"Balance\": \"99249214171\","+
            "\"Flags\": 0,"+
            "\"OwnerCount\": 3,"+
            "\"Sequence\": 177},"+
            "\"LedgerEntryType\": \"AccountRoot\","+
            "\"LedgerIndex\": \"D66D0EC951FD5707633BEBE74DB18B6D2DDA6771BA0FBF079AD08BFDE6066056\","+
            "\"PreviousFields\": {\"Balance\": \"99249214170\"},"+
            "\"PreviousTxnID\": \"33562B82489F263F173801272D02178C0018A40ACFDC84B59976CE7C163F41FC\","+
            "\"PreviousTxnLgrSeq\": 2681281}}],"+
            "\"TransactionIndex\": 2,"+
            "\"TransactionResult\": \"tesSUCCESS\"}";

    @Test
    public void test_parsing_transaction_meta_with_STArray() throws Exception {
        STObject meta = STObject.fromJSONObject(new JSONObject(metaString));
        STArray nodes = meta.get(STArray.AffectedNodes);

        // Some helper methods to get enum fields
        assertEquals(TransactionEngineResult.tesSUCCESS,
                     meta.transactionResult());

        STObject firstAffected = nodes.get(0);
        assertEquals(LedgerEntryType.AccountRoot,
                     firstAffected.get(STObject.ModifiedNode).ledgerEntryType());

        assertTrue(firstAffected.has(STObject.ModifiedNode));
        assertEquals(new UInt32(35),  finalSequence(firstAffected));
        assertEquals(new UInt32(177), finalSequence(nodes.get(1)));
    }

    private UInt32 finalSequence(STObject affected) {
        return affected.get(STObject.ModifiedNode).get(STObject.FinalFields).get(UInt32.Sequence);
    }

    @Test
    public void testSerializedPaymentTransaction() throws JSONException {
        String expectedSerialization = "120000240000000561D4C44364C5BB00000000000000000000000000005553440000000000B5F762798A53D543A014CAF8B297CFF8F2F937E868400000000000000F73210330E7FC9D56BB25D6893BA3F317AE5BCF33B3291BD63DB32654A313222F7FD0208114B5F762798A53D543A014CAF8B297CFF8F2F937E88314FD94A75318DE40B1D513E6764ECBCB6F1E7056ED";

        AccountID ac = AccountID.fromSeedString(TestFixtures.master_seed);
        STObject fromSO = STObject.newInstance();

        fromSO.put(Field.TransactionType, "Payment");
        fromSO.put(AccountID.Account, ac.address);
        fromSO.put(UInt32.Sequence, 5);
        fromSO.put(Amount.Fee, "15");
        fromSO.put(VariableLength.SigningPubKey, ac.getKeyPair().pubHex());
        fromSO.put(AccountID.Destination, TestFixtures.bob_account.address);
        fromSO.put(Amount.Amount, "12/USD/" + ac.address);

        assertEquals(expectedSerialization, fromSO.toHex());
    }

    @Test
    public void testSerializedPaymentTransactionFromJSON() throws JSONException {
        String tx_json = "{\"Amount\":{\"issuer\":\"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\"," +
                                      "\"value\":\"12\"," +
                                      "\"currency\":\"USD\"}," +
                          "\"Fee\":\"15\"," +
                          "\"SigningPubKey\":\"0330e7fc9d56bb25d6893ba3f317ae5bcf33b3291bd63db32654a313222f7fd020\"," +
                          "\"Account\":\"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\"," +
                          "\"TransactionType\":\"Payment\"," +
                          "\"Sequence\":5," +
                          "\"Destination\":\"rQfFsw6w4wdymTCSfF2fZQv7SZzfGyzsyB\"}";

        String expectedSerialization = "120000240000000561D4C44364C5BB000000000000000000000000000055534" +
                                       "40000000000B5F762798A53D543A014CAF8B297CFF8F2F937E8684000000000" +
                                       "00000F73210330E7FC9D56BB25D6893BA3F317AE5BCF33B3291BD63DB32654A" +
                                       "313222F7FD0208114B5F762798A53D543A014CAF8B297CFF8F2F937E88314FD" +
                                       "94A75318DE40B1D513E6764ECBCB6F1E7056ED";

        STObject fromJSON = STObject.fromJSONObject(new JSONObject(tx_json));
        assertEquals(expectedSerialization, fromJSON.toHex());
    }

    @Test
    public void testUINT() throws JSONException {

        JSONObject json = new JSONObject("{\"Expiration\" : 21}");
        STObject so = STObject.translate.fromJSONObject(json);
        assertEquals(21, so.get(UInt32.Expiration).longValue());

        byte[] bytes =  UInt8.translate.toWireBytes(new UInt8(1));
        byte[] bytes2 = UInt16.translate.toWireBytes(new UInt16(1));
        byte[] bytes4 = UInt32.translate.toWireBytes(new UInt32(1));
        byte[] bytes8 = UInt64.translate.toWireBytes(new UInt64(1));

        assertEquals( bytes.length, 1);
        assertEquals(bytes2.length, 2);
        assertEquals(bytes4.length, 4);
        assertEquals(bytes8.length, 8);
    }

    @Test
    public void testSymbolics() throws JSONException {
        assertNotNull(TxFormat.fromString("Payment"));

        JSONObject json = new JSONObject("{\"Expiration\"        : 21, " +
                                          "\"TransactionResult\" : 0,  " +
                                          "\"TransactionType\"   : 0  }");

        STObject so = STObject.translate.fromJSONObject(json);
        assertEquals(so.getFormat(), TxFormat.Payment);
        so.setFormat(null); // Else it (SHOULD) attempt to validate something clearly unFormatted

        JSONObject object = STObject.translate.toJSONObject(so);

        assertEquals(object.get("TransactionResult"), "tesSUCCESS");
        assertEquals(object.get("TransactionType"), "Payment");

    }
}
