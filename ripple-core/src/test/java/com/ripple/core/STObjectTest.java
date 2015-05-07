package com.ripple.core;

import com.ripple.core.binary.STReader;
import com.ripple.core.binary.STWriter;
import com.ripple.core.coretypes.*;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.Index;
import com.ripple.core.coretypes.uint.UInt16;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.coretypes.uint.UInt64;
import com.ripple.core.coretypes.uint.UInt8;
import com.ripple.core.fields.Field;
import com.ripple.core.formats.TxFormat;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BinarySerializer;
import com.ripple.core.serialized.BytesList;
import com.ripple.core.serialized.enums.EngineResult;
import com.ripple.core.serialized.enums.LedgerEntryType;
import com.ripple.core.types.known.sle.LedgerEntry;
import com.ripple.core.types.known.sle.entries.AccountRoot;
import com.ripple.core.types.known.sle.entries.DirectoryNode;
import com.ripple.core.types.known.sle.entries.Offer;
import com.ripple.core.types.known.tx.result.TransactionMeta;
import com.ripple.core.types.known.tx.txns.Payment;
import com.ripple.core.types.shamap.AccountState;
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.crypto.ecdsa.Seed;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

public class STObjectTest {
    @Test
    public void binaryParsingSerializingSanityTest2() throws FileNotFoundException {
//        File f = new File("/home/nick/dumps/ledger-full-120000.json");
        File f = new File("/home/nick/dumps/ledger-full-6230000.json");
        if (!f.exists()) {
            return;
        }

        JSONTokener tok = new JSONTokener(new FileReader(f));
        JSONObject ledgerJSON = new JSONObject(tok);
        JSONArray accountState = ledgerJSON.getJSONArray("accountState");
        AccountState sm = new AccountState();
        JSONObject stateObject = null;

        int i = 0;
        while (i < accountState.length()) {
            final STObject fromJSON;
            stateObject = accountState.getJSONObject(i);
            assertEncodeable(stateObject);
            fromJSON = STObject.fromJSONObject(stateObject);
            sm.addLE((LedgerEntry) fromJSON);
            i++;
        }
        Hash256 hash = sm.hash();
        assertEquals(hash.toHex(), ledgerJSON.getString("account_hash"));
    }


    @Test
    public void binaryParsingSerializingSanityTest() throws FileNotFoundException {
        // TODO, add this as a zipfile to the repo, so can test from it
        File f = new File("ripple-core/src/test/java/com/ripple/resources/ledgers-full.json");
        if (!f.exists()) {
            return;
        }

        JSONTokener tok = new JSONTokener(new FileReader(f));
        JSONObject parsed = new JSONObject(tok);
        Iterator iterator = parsed.keys();

        while (iterator.hasNext()) {
            String next = (String) iterator.next();
            JSONObject ledgerJSON = parsed.getJSONObject(next).getJSONObject("ledger");
            JSONArray accountState = ledgerJSON.getJSONArray("accountState");
            int i = 0;
            while (i < accountState.length()) {
                try {
                    assertEncodeable(accountState.getJSONObject(i));
                } catch (RuntimeException e) {
                    // See `TAKER_PAYS_FOR_THAT_DAMN_OFFER`
                    if (!e.getMessage().split("\n")[0].equals(
                            "Couldn't put `1000000000000000100` into field `TakerPays`")) {
                        throw e;
                    }
                } finally {
                    i++;
                }
            }
        }

    }

    public void assertEncodeable(JSONObject stateObject) {
        STObject fromJSON;

        Object index = stateObject.remove("index");
        fromJSON = STObject.fromJSONObject(stateObject);
        String hexFromJSON = fromJSON.toHex();
        JSONObject fromJsonToJson = fromJSON.toJSONObject();
        STObject fromJsonToJsonAndBack = STObject.fromJSONObject(fromJsonToJson);
        String hexFromJsonToToJsonToHex = fromJsonToJsonAndBack.toHex();
        STObject rebuiltFromHex = STObject.fromHex(hexFromJSON);
        assertEquals(hexFromJSON, rebuiltFromHex.toHex());
        assertEquals(hexFromJSON, hexFromJsonToToJsonToHex);
        assertEquals(fromJsonToJsonAndBack.toJSONObject().toString(), fromJsonToJson.toString());
        assertEquals(stateObject.length(), fromJsonToJson.length());
        stateObject.put("index", index);
    }


    @Test
    public void testVlEncoding() {
        for (int i = 0; i < 918744; i++) {
            byte[] bytes = BinarySerializer.encodeVL(i);
            BinaryParser parser= new BinaryParser(bytes);
            int i1 = parser.readVLLength();
            assertEquals(i1, i);
        }
    }

    @Test
    public void testUintEncoding() {
        for (int i = 0; i < 10000; i++) {
            BytesList list = new BytesList();
            STWriter writer = new STWriter(list);
            UInt32 obj = new UInt32(i);
            writer.write(obj);
            STReader reader = new STReader(list.bytesHex());
            assertEquals(obj, reader.uInt32());
        }
    }

    @Test
    public void testNestedObjectSerialization() throws Exception {
        String rippleLibHex = "120007220000000024000195F964400000170A53AC2065D5460561EC9DE000000000000000000000000000" +
                "494C53000000000092D705968936C419CE614BF264B5EEB1CEA47FF468400000000000000A7321028472865" +
                "AF4CB32AA285834B57576B7290AA8C31B459047DB27E16F418D6A71667447304502202ABE08D5E78D1E74A4" +
                "C18F2714F64E87B8BD57444AFA5733109EB3C077077520022100DB335EE97386E4C0591CAC024D50E9230D8" +
                "F171EEB901B5E5E4BD6D1E0AEF98C811439408A69F0895E62149CFCC006FB89FA7D1E6E5D";


        String rippledHex = "120007220000000024000195F964400000170A53AC2065D5460561EC9DE000000000000000000000000000494C53000000000092D705968936C419CE614BF264B5EEB1CEA47FF468400000000000000A7321028472865AF4CB32AA285834B57576B7290AA8C31B459047DB27E16F418D6A71667447304502202ABE08D5E78D1E74A4C18F2714F64E87B8BD57444AFA5733109EB3C077077520022100DB335EE97386E4C0591CAC024D50E9230D8F171EEB901B5E5E4BD6D1E0AEF98C811439408A69F0895E62149CFCC006FB89FA7D1E6E5D";

        String json = "{" +
                "  \"Account\": \"raD5qJMAShLeHZXf9wjUmo6vRK4arj9cF3\"," +
                "  \"Fee\": \"10\"," +
                "  \"Flags\": 0," +
                "  \"Sequence\": 103929," +
                "  \"SigningPubKey\": \"028472865AF4CB32AA285834B57576B7290AA8C31B459047DB27E16F418D6A7166\"," +
                "  \"TakerGets\": {" +
                "    \"currency\": \"ILS\"," +
                "    \"issuer\": \"rNPRNzBB92BVpAhhZr4iXDTveCgV5Pofm9\"," +
                "    \"value\": \"1694.768\"" +
                "  }," +
                "  \"TakerPays\": \"98957503520\"," +
                "  \"TransactionType\": \"OfferCreate\"," +
                "  \"TxnSignature\": \"304502202ABE08D5E78D1E74A4C18F2714F64E87B8BD57444AFA5733109EB3C077077520022100DB335EE97386E4C0591CAC024D50E9230D8F171EEB901B5E5E4BD6D1E0AEF98C\"," +
                "  \"hash\": \"232E91912789EA1419679A4AA920C22CFC7C6B601751D6CBE89898C26D7F4394\"," +
                "  \"metaData\": {" +
                "    \"AffectedNodes\": [" +
                "      {" +
                "        \"CreatedNode\": {" +
                "          \"LedgerEntryType\": \"Offer\"," +
                "          \"LedgerIndex\": \"3596CE72C902BAFAAB56CC486ACAF9B4AFC67CF7CADBB81A4AA9CBDC8C5CB1AA\"," +
                "          \"NewFields\": {" +
                "            \"Account\": \"raD5qJMAShLeHZXf9wjUmo6vRK4arj9cF3\"," +
                "            \"BookDirectory\": \"62A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F000\"," +
                "            \"OwnerNode\": \"000000000000000E\"," +
                "            \"Sequence\": 103929," +
                "            \"TakerGets\": {" +
                "              \"currency\": \"ILS\"," +
                "              \"issuer\": \"rNPRNzBB92BVpAhhZr4iXDTveCgV5Pofm9\"," +
                "              \"value\": \"1694.768\"" +
                "            }," +
                "            \"TakerPays\": \"98957503520\"" +
                "          }" +
                "        }" +
                "      }," +
                "      {" +
                "        \"CreatedNode\": {" +
                "          \"LedgerEntryType\": \"DirectoryNode\"," +
                "          \"LedgerIndex\": \"62A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F000\"," +
                "          \"NewFields\": {" +
                "            \"ExchangeRate\": \"5C14BE8A20D7F000\"," +
                "            \"RootIndex\": \"62A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F000\"," +
                "            \"TakerGetsCurrency\": \"000000000000000000000000494C530000000000\"," +
                "            \"TakerGetsIssuer\": \"92D705968936C419CE614BF264B5EEB1CEA47FF4\"" +
                "          }" +
                "        }" +
                "      }," +
                "      {" +
                "        \"ModifiedNode\": {" +
                "          \"FinalFields\": {" +
                "            \"Flags\": 0," +
                "            \"IndexPrevious\": \"0000000000000000\"," +
                "            \"Owner\": \"raD5qJMAShLeHZXf9wjUmo6vRK4arj9cF3\"," +
                "            \"RootIndex\": \"801C5AFB5862D4666D0DF8E5BE1385DC9B421ED09A4269542A07BC0267584B64\"" +
                "          }," +
                "          \"LedgerEntryType\": \"DirectoryNode\"," +
                "          \"LedgerIndex\": \"AB03F8AA02FFA4635E7CE2850416AEC5542910A2B4DBE93C318FEB08375E0DB5\"" +
                "        }" +
                "      }," +
                "      {" +
                "        \"ModifiedNode\": {" +
                "          \"FinalFields\": {" +
                "            \"Account\": \"raD5qJMAShLeHZXf9wjUmo6vRK4arj9cF3\"," +
                "            \"Balance\": \"106861218302\"," +
                "            \"Flags\": 0," +
                "            \"OwnerCount\": 9," +
                "            \"Sequence\": 103930" +
                "          }," +
                "          \"LedgerEntryType\": \"AccountRoot\"," +
                "          \"LedgerIndex\": \"CF23A37E39A571A0F22EC3E97EB0169936B520C3088963F16C5EE4AC59130B1B\"," +
                "          \"PreviousFields\": {" +
                "            \"Balance\": \"106861218312\"," +
                "            \"OwnerCount\": 8," +
                "            \"Sequence\": 103929" +
                "          }," +
                "          \"PreviousTxnID\": \"DE15F43F4A73C4F6CB1C334D9E47BDE84467C0902796BB81D4924885D1C11E6D\"," +
                "          \"PreviousTxnLgrSeq\": 3225338" +
                "        }" +
                "      }" +
                "    ]," +
                "    \"TransactionIndex\": 0," +
                "    \"TransactionResult\": \"tesSUCCESS\"" +
                "  }" +
                "}";

        JSONObject txJson = new JSONObject(json);
        STObject meta = STObject.fromJSONObject((JSONObject) txJson.remove("metaData"));
        STObject tx = STObject.fromJSONObject(txJson);

        String rippledMetaHex = "201C00000000F8E311006F563596CE72C902BAFAAB56CC486ACAF9B4AFC67CF7CADBB81A4AA9CBDC8C5CB1AAE824000195F934000000000000000E501062A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F00064400000170A53AC2065D5460561EC9DE000000000000000000000000000494C53000000000092D705968936C419CE614BF264B5EEB1CEA47FF4811439408A69F0895E62149CFCC006FB89FA7D1E6E5DE1E1E31100645662A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F000E8365C14BE8A20D7F0005862A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F0000311000000000000000000000000494C530000000000041192D705968936C419CE614BF264B5EEB1CEA47FF4E1E1E511006456AB03F8AA02FFA4635E7CE2850416AEC5542910A2B4DBE93C318FEB08375E0DB5E7220000000032000000000000000058801C5AFB5862D4666D0DF8E5BE1385DC9B421ED09A4269542A07BC0267584B64821439408A69F0895E62149CFCC006FB89FA7D1E6E5DE1E1E511006125003136FA55DE15F43F4A73C4F6CB1C334D9E47BDE84467C0902796BB81D4924885D1C11E6D56CF23A37E39A571A0F22EC3E97EB0169936B520C3088963F16C5EE4AC59130B1BE624000195F92D000000086240000018E16CCA08E1E7220000000024000195FA2D000000096240000018E16CC9FE811439408A69F0895E62149CFCC006FB89FA7D1E6E5DE1E1F1031000";
        String actual = tx.toHex();

        assertEquals(rippledHex, rippleLibHex);
        assertEquals(rippledHex, actual);
        assertEquals(rippledMetaHex.length(), meta.toHex().length());
        assertEquals(rippledMetaHex, meta.toHex());
    }

    @Test
    public void testNestedObjectSerialization2() throws Exception {
        String json = "{" +
                "  \"Account\": \"rMWUykAmNQDaM9poSes8VLDZDDKEbmo7MX\"," +
                "  \"Fee\": \"10\"," +
                "  \"Flags\": 0," +
                "  \"OfferSequence\": 1130290," +
                "  \"Sequence\": 1130447," +
                "  \"SigningPubKey\": \"0256C64F0378DCCCB4E0224B36F7ED1E5586455FF105F760245ADB35A8B03A25FD\"," +
                "  \"TransactionType\": \"OfferCancel\"," +
                "  \"TxnSignature\": \"304502200A8BED7B8955F45633BA4E9212CE386C397E32ACFF6ECE08EB74B5C86200C606022100EF62131FF50B288244D9AB6B3D18BACD44924D2BAEEF55E1B3232B7E033A2791\"," +
                "  \"hash\": \"A197ECCF23E55193CBE292F7A373F0DE0F521D4DCAE32484E20EC634C1ACE528\"," +
                "  \"metaData\": {" +
                "    \"AffectedNodes\": [" +
                "      {" +
                "        \"ModifiedNode\": {" +
                "          \"FinalFields\": {" +
                "            \"Account\": \"rMWUykAmNQDaM9poSes8VLDZDDKEbmo7MX\"," +
                "            \"Balance\": \"1988695002\"," +
                "            \"Flags\": 0," +
                "            \"OwnerCount\": 68," +
                "            \"Sequence\": 1130448" +
                "          }," +
                "          \"LedgerEntryType\": \"AccountRoot\"," +
                "          \"LedgerIndex\": \"56091AD066271ED03B106812AD376D48F126803665E3ECBFDBBB7A3FFEB474B2\"," +
                "          \"PreviousFields\": {" +
                "            \"Balance\": \"1988695012\"," +
                "            \"OwnerCount\": 69," +
                "            \"Sequence\": 1130447" +
                "          }," +
                "          \"PreviousTxnID\": \"610A3178D0A69167DF32E28990FD60D50F5610A5CF5C832CBF0C7FCC0913516B\"," +
                "          \"PreviousTxnLgrSeq\": 3225338" +
                "        }" +
                "      }," +
                "      {" +
                "        \"ModifiedNode\": {" +
                "          \"FinalFields\": {" +
                "            \"ExchangeRate\": \"561993D688DA919A\"," +
                "            \"Flags\": 0," +
                "            \"RootIndex\": \"5943CB2C05B28743AADF0AE47E9C57E9C15BD23284CF6DA9561993D688DA919A\"," +
                "            \"TakerGetsCurrency\": \"0000000000000000000000004254430000000000\"," +
                "            \"TakerGetsIssuer\": \"92D705968936C419CE614BF264B5EEB1CEA47FF4\"," +
                "            \"TakerPaysCurrency\": \"0000000000000000000000004C54430000000000\"," +
                "            \"TakerPaysIssuer\": \"92D705968936C419CE614BF264B5EEB1CEA47FF4\"" +
                "          }," +
                "          \"LedgerEntryType\": \"DirectoryNode\"," +
                "          \"LedgerIndex\": \"5943CB2C05B28743AADF0AE47E9C57E9C15BD23284CF6DA9561993D688DA919A\"" +
                "        }" +
                "      }," +
                "      {" +
                "        \"DeletedNode\": {" +
                "          \"FinalFields\": {" +
                "            \"Account\": \"rMWUykAmNQDaM9poSes8VLDZDDKEbmo7MX\"," +
                "            \"BookDirectory\": \"5943CB2C05B28743AADF0AE47E9C57E9C15BD23284CF6DA9561993D688DA919A\"," +
                "            \"BookNode\": \"0000000000000000\"," +
                "            \"Flags\": 0," +
                "            \"OwnerNode\": \"0000000000003292\"," +
                "            \"PreviousTxnID\": \"C7D1671589B1B4AB1071E38299B8338632DAD19A7D0F8D28388F40845AF0BCC5\"," +
                "            \"PreviousTxnLgrSeq\": 3225110," +
                "            \"Sequence\": 1130290," +
                "            \"TakerGets\": {" +
                "              \"currency\": \"BTC\"," +
                "              \"issuer\": \"rNPRNzBB92BVpAhhZr4iXDTveCgV5Pofm9\"," +
                "              \"value\": \"0.299233659\"" +
                "            }," +
                "            \"TakerPays\": {" +
                "              \"currency\": \"LTC\"," +
                "              \"issuer\": \"rNPRNzBB92BVpAhhZr4iXDTveCgV5Pofm9\"," +
                "              \"value\": \"21.5431\"" +
                "            }" +
                "          }," +
                "          \"LedgerEntryType\": \"Offer\"," +
                "          \"LedgerIndex\": \"78812E6E2AB80D5F291F8033D7BC23F0A6E4EA80C998BFF38E80E2A09D2C4D93\"" +
                "        }" +
                "      }," +
                "      {" +
                "        \"ModifiedNode\": {" +
                "          \"FinalFields\": {" +
                "            \"Flags\": 0," +
                "            \"IndexNext\": \"0000000000003293\"," +
                "            \"IndexPrevious\": \"0000000000000000\"," +
                "            \"Owner\": \"rMWUykAmNQDaM9poSes8VLDZDDKEbmo7MX\"," +
                "            \"RootIndex\": \"2114A41BB356843CE99B2858892C8F1FEF634B09F09AF2EB3E8C9AA7FD0E3A1A\"" +
                "          }," +
                "          \"LedgerEntryType\": \"DirectoryNode\"," +
                "          \"LedgerIndex\": \"F78A0FFA69890F27C2A79C495E1CEB187EE8E677E3FDFA5AD0B8FCFC6E644E38\"" +
                "        }" +
                "      }" +
                "    ]," +
                "    \"TransactionIndex\": 1," +
                "    \"TransactionResult\": \"tesSUCCESS\"" +
                "  }" +
                "}";

        JSONObject txJson = new JSONObject(json);
        STObject meta = STObject.fromJSONObject((JSONObject) txJson.remove("metaData"));
        STObject tx = STObject.fromJSONObject(txJson);

        String rippledMetaHex = "201C00000001F8E511006125003136FA55610A3178D0A69167DF32E28990FD60D50F5610A5CF5C832CBF0C7FCC0913516B5656091AD066271ED03B106812AD376D48F126803665E3ECBFDBBB7A3FFEB474B2E62400113FCF2D000000456240000000768913E4E1E722000000002400113FD02D000000446240000000768913DA8114E0E893E991B2142E74486F7D3331CF711EA84213E1E1E5110064565943CB2C05B28743AADF0AE47E9C57E9C15BD23284CF6DA9561993D688DA919AE7220000000036561993D688DA919A585943CB2C05B28743AADF0AE47E9C57E9C15BD23284CF6DA9561993D688DA919A01110000000000000000000000004C54430000000000021192D705968936C419CE614BF264B5EEB1CEA47FF403110000000000000000000000004254430000000000041192D705968936C419CE614BF264B5EEB1CEA47FF4E1E1E411006F5678812E6E2AB80D5F291F8033D7BC23F0A6E4EA80C998BFF38E80E2A09D2C4D93E722000000002400113F32250031361633000000000000000034000000000000329255C7D1671589B1B4AB1071E38299B8338632DAD19A7D0F8D28388F40845AF0BCC550105943CB2C05B28743AADF0AE47E9C57E9C15BD23284CF6DA9561993D688DA919A64D4C7A75562493C000000000000000000000000004C5443000000000092D705968936C419CE614BF264B5EEB1CEA47FF465D44AA183A77ECF80000000000000000000000000425443000000000092D705968936C419CE614BF264B5EEB1CEA47FF48114E0E893E991B2142E74486F7D3331CF711EA84213E1E1E511006456F78A0FFA69890F27C2A79C495E1CEB187EE8E677E3FDFA5AD0B8FCFC6E644E38E72200000000310000000000003293320000000000000000582114A41BB356843CE99B2858892C8F1FEF634B09F09AF2EB3E8C9AA7FD0E3A1A8214E0E893E991B2142E74486F7D3331CF711EA84213E1E1F1031000";
        String rippledHex = "12000822000000002400113FCF201900113F3268400000000000000A73210256C64F0378DCCCB4E0224B36F7ED1E5586455FF105F760245ADB35A8B03A25FD7447304502200A8BED7B8955F45633BA4E9212CE386C397E32ACFF6ECE08EB74B5C86200C606022100EF62131FF50B288244D9AB6B3D18BACD44924D2BAEEF55E1B3232B7E033A27918114E0E893E991B2142E74486F7D3331CF711EA84213";

        String actual = tx.toHex();

        assertEquals(rippledHex, actual);
        assertEquals(rippledMetaHex, meta.toHex());
    }

    @Test
    public void testTypeInference() {

        STObject so = new STObject();
        so.putTranslated(Field.valueOf("LowLimit"), "10.0/USD");
        so.putTranslated(Amount.Balance, "125.0");

        assertEquals(so.get(Amount.Balance).toDropsString(), "125000000");
        assertEquals(so.get(Amount.LowLimit).currencyString(), "USD");

        assertNotNull(so.get(Amount.LowLimit));
        assertNull(so.get(Amount.HighLimit));
    }

    @Test
    /**
     * We just testing this won't blow up due to unknown `date` field!
     */
    public void testfromJSONObjectWithUnknownFields() {

        String json = "{\"date\": 434707820,\n" +
                "\"hash\": \"66347806574036FD3D3E9FDA20A411FA8B2D26AA3C3725A107FCF0050F1E4B86\"}";
        STObject.fromJSON(json);
    }

    String metaJson = "{\"AffectedNodes\": [{\"ModifiedNode\": {\"FinalFields\": {\"Account\": \"rwMyB1diFJ7xqEKYGYgk9tKrforvTr33M5\"," +
            "\"Balance\": \"286000447\"," +
            "\"Flags\": 0," +
            "\"OwnerCount\": 4," +
            "\"Sequence\": 35}," +
            "\"LedgerEntryType\": \"AccountRoot\"," +
            "\"LedgerIndex\": \"32FE2333B117B257F3AB58E1CB15A6533DC27FDD61FEB1027858D367B40B559A\"," +
            "\"PreviousFields\": {\"Balance\": \"286000463\"," +
            "\"Sequence\": 34}," +
            "\"PreviousTxnID\": \"33562B82489F263F173801272D02178C0018A40ACFDC84B59976CE7C163F41FC\"," +
            "\"PreviousTxnLgrSeq\": 2681281}}," +
            "{\"ModifiedNode\": {\"FinalFields\": {\"Account\": \"rP1coskQzayaQ9geMdJgAV5f3tNZcHghzH\"," +
            "\"Balance\": \"99249214171\"," +
            "\"Flags\": 0," +
            "\"OwnerCount\": 3," +
            "\"Sequence\": 177}," +
            "\"LedgerEntryType\": \"AccountRoot\"," +
            "\"LedgerIndex\": \"D66D0EC951FD5707633BEBE74DB18B6D2DDA6771BA0FBF079AD08BFDE6066056\"," +
            "\"PreviousFields\": {\"Balance\": \"99249214170\"}," +
            "\"PreviousTxnID\": \"33562B82489F263F173801272D02178C0018A40ACFDC84B59976CE7C163F41FC\"," +
            "\"PreviousTxnLgrSeq\": 2681281}}]," +
            "\"TransactionIndex\": 2," +
            "\"TransactionResult\": \"tesSUCCESS\"}";

    @Test
    public void testDryWetDry() throws Exception {
        String jsonHexed = "201C00000001F8E511006125003136FA55610A3178D0A69167DF32E28990FD60D50F5610A5CF5C832CBF0C7FCC0913516B5656091AD066271ED03B106812AD376D48F126803665E3ECBFDBBB7A3FFEB474B2E62400113FCF2D000000456240000000768913E4E1E722000000002400113FD02D000000446240000000768913DA8114E0E893E991B2142E74486F7D3331CF711EA84213E1E1E5110064565943CB2C05B28743AADF0AE47E9C57E9C15BD23284CF6DA9561993D688DA919AE7220000000036561993D688DA919A585943CB2C05B28743AADF0AE47E9C57E9C15BD23284CF6DA9561993D688DA919A01110000000000000000000000004C54430000000000021192D705968936C419CE614BF264B5EEB1CEA47FF403110000000000000000000000004254430000000000041192D705968936C419CE614BF264B5EEB1CEA47FF4E1E1E411006F5678812E6E2AB80D5F291F8033D7BC23F0A6E4EA80C998BFF38E80E2A09D2C4D93E722000000002400113F32250031361633000000000000000034000000000000329255C7D1671589B1B4AB1071E38299B8338632DAD19A7D0F8D28388F40845AF0BCC550105943CB2C05B28743AADF0AE47E9C57E9C15BD23284CF6DA9561993D688DA919A64D4C7A75562493C000000000000000000000000004C5443000000000092D705968936C419CE614BF264B5EEB1CEA47FF465D44AA183A77ECF80000000000000000000000000425443000000000092D705968936C419CE614BF264B5EEB1CEA47FF48114E0E893E991B2142E74486F7D3331CF711EA84213E1E1E511006456F78A0FFA69890F27C2A79C495E1CEB187EE8E677E3FDFA5AD0B8FCFC6E644E38E72200000000310000000000003293320000000000000000582114A41BB356843CE99B2858892C8F1FEF634B09F09AF2EB3E8C9AA7FD0E3A1A8214E0E893E991B2142E74486F7D3331CF711EA84213E1E1F1031000";

        STObject meta = STObject.fromHex(jsonHexed);
        String actual = meta.toHex();
        assertEquals(jsonHexed.length(), actual.length());
        assertEquals(jsonHexed, actual);
    }

    @Test
    public void testParsingVector256() throws Exception {
        // This was a test case for a bug found in ripple-lib js
        String jsonHexed = "110064220000000058000360186E008422E06B72D5B275E29EE3BE9D87A370F424E0E7BF613C4659098214289D19799C892637306AAAF03805EDFCDF6C28B8011320081342A0AB45459A54D8E4FA1842339A102680216CF9A152BCE4F4CE467D8246";
        DirectoryNode dn = (DirectoryNode) STObject.fromHex(jsonHexed);
        assertEquals("[\"081342A0AB45459A54D8E4FA1842339A102680216CF9A152BCE4F4CE467D8246\"]",
                      dn.indexes().toJSONArray().toString());
    }

    @Test
    public void testFormatted() throws Exception {
        String json = "{\"TakerPays\" : \"2.0\", \"TakerGets\" : \"1.0\", \"LedgerEntryType\" : \"Offer\"}";
        STObject offer = STObject.fromJSON(json);
        Offer casted = (Offer) STObject.formatted(offer);
    }

    @Test
    public void testParsingTransactionMetaWithSTArray() throws Exception {
        TransactionMeta meta = (TransactionMeta) STObject.fromJSON(metaJson);
        STArray nodes = meta.get(STArray.AffectedNodes);

        // Some helper methods to get enum fields
        assertEquals(EngineResult.tesSUCCESS, meta.engineResult());

        STObject firstAffected = nodes.get(0);
        STObject secondAffected = nodes.get(1);
        assertEquals(LedgerEntryType.AccountRoot,
                ((AccountRoot) firstAffected.get(STObject.ModifiedNode)).ledgerEntryType());

        assertTrue(firstAffected.has(STObject.ModifiedNode));
        assertEquals(35, finalSequence(firstAffected).longValue());
        assertEquals(177, finalSequence(secondAffected).longValue());
    }

    private UInt32 finalSequence(STObject affected) {
        return affected.get(STObject.ModifiedNode).get(STObject.FinalFields).get(UInt32.Sequence);
    }

    @Test
    public void testSerializedPaymentTransaction() {
        String expectedSerialization = "120000240000000561D4C44364C5BB00000000000000000000000000005553440000000000B5F762798A53D543A014CAF8B297CFF8F2F937E868400000000000000F73210330E7FC9D56BB25D6893BA3F317AE5BCF33B3291BD63DB32654A313222F7FD0208114B5F762798A53D543A014CAF8B297CFF8F2F937E88314FD94A75318DE40B1D513E6764ECBCB6F1E7056ED";

        IKeyPair kp = Seed.getKeyPair(TestFixtures.master_seed);
        AccountID ac = AccountID.fromKeyPair(kp);

        Payment payment = new Payment();

        payment.put(AccountID.Account, ac);
        payment.put(AccountID.Destination, TestFixtures.bob_account);

        payment.putTranslated(UInt32.Sequence, 5);
        payment.putTranslated(Amount.Fee, "15");
        payment.putTranslated(Blob.SigningPubKey, kp.canonicalPubHex());
        payment.putTranslated(Amount.Amount, "12/USD/" + ac.address);

        assertEquals(expectedSerialization, payment.toHex());
    }

    @Test
    public void testSerializedPaymentTransactionFromJSON() {
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
    public void testBinaryParsing() throws Exception {
        /*
        * TransactionType
          Sequence
          Amount
          Fee
          SigningPubKey
          Account
          Destination
        * */

        String expectedSerialization = "120000240000000561D4C44364C5BB000000000000000000000000000055534" +
                "40000000000B5F762798A53D543A014CAF8B297CFF8F2F937E8684000000000" +
                "00000F73210330E7FC9D56BB25D6893BA3F317AE5BCF33B3291BD63DB32654A" +
                "313222F7FD0208114B5F762798A53D543A014CAF8B297CFF8F2F937E88314FD" +
                "94A75318DE40B1D513E6764ECBCB6F1E7056ED";

        BinaryParser binaryParser = new BinaryParser(expectedSerialization);
        Field field;

        field = binaryParser.readField();
        assertEquals(Field.TransactionType, field);
        assertEquals(Field.TransactionType, UInt16.TransactionType.getField());
        UInt16 uInt16 = UInt16.translate.fromParser(binaryParser);
        assertEquals(0, uInt16.intValue());

        field = binaryParser.readField();
        assertEquals(Field.Sequence, field);
        UInt32 sequence = UInt32.translate.fromParser(binaryParser);
        assertEquals(5, sequence.intValue());

        field = binaryParser.readField();
        assertEquals(Field.Amount, field);

        binaryParser = new BinaryParser(expectedSerialization);
        STObject so = STObject.translate.fromParser(binaryParser);
        assertEquals(expectedSerialization, so.toHex());
    }

    @Test
    public void testUINT() {

        STObject so = STObject.fromJSON("{\"Expiration\" : 21}");
        assertEquals(21, so.get(UInt32.Expiration).longValue());

        byte[] bytes =  (new UInt8 (1)).toBytes();
        byte[] bytes2 = (new UInt16(1)).toBytes();
        byte[] bytes4 = (new UInt32(1)).toBytes();
        byte[] bytes8 = (new UInt64(1)).toBytes();

        assertEquals(bytes.length, 1);
        assertEquals(bytes2.length, 2);
        assertEquals(bytes4.length, 4);
        assertEquals(bytes8.length, 8);
    }

    @Test
    public void testSymbolics() {
        assertNotNull(TxFormat.fromString("Payment"));

        String json = "{\"Expiration\"        : 21, " +
                "\"TransactionResult\" : 0,  " +
                "\"TransactionType\"   : 0  }";

        STObject so = STObject.fromJSON(json);
        assertEquals(so.getFormat(), TxFormat.Payment);
        so.setFormat(null); // Else it (SHOULD) attempt to validate something clearly unFormatted

        JSONObject object = so.toJSONObject();

        assertEquals(object.get("TransactionResult"), "tesSUCCESS");
        assertEquals(object.get("TransactionType"), "Payment");

    }

    @Test
    public void testTransactionIDCreation() throws Exception {
        String tx_json = "{"+
                "    \"Account\": \"rwMyB1diFJ7xqEKYGYgk9tKrforvTr33M5\","+
                "    \"Amount\": \"1\","+
                "    \"Destination\": \"rP1coskQzayaQ9geMdJgAV5f3tNZcHghzH\","+
                "    \"Fee\": \"12\","+
                "    \"Sequence\": 88," +
                "    \"SigningPubKey\": \"02EEAF2C95B668D411FC490746C52071514F6D3A7B742D91D82CB591B5443D1C59\","+
                "    \"TransactionType\": \"Payment\","+
                "    \"TxnSignature\": \"304602210099A6999CD967ADD1E5A96EE62B2701AF57AA44191FE755D4976BCAFD95484238022100CAF57DB851B1AE174005CABECCBDF418AC33EF0C87C9BB4DEC2A9407F829207B\","+
                "    \"hash\": \"B01AC8EBCE6029AB1159258D249CBDCA139C4C6346592494D0E3C0DE6247219D\"}";
        STObject tx = STObject.fromJSON(tx_json);
        Hash256 hash = (Hash256) tx.remove(Field.hash);
        Hash256 rehashed = Index.transactionID(tx.toBytes());
        assertEquals(hash, rehashed);
    }

    @Test
    public void testTransactionIDCreation2() throws Exception {
        String tx_json = "{" +
                "    \"Account\": \"rwMyB1diFJ7xqEKYGYgk9tKrforvTr33M5\"," +
                "    \"Amount\": \"1\"," +
                "    \"Destination\": \"rP1coskQzayaQ9geMdJgAV5f3tNZcHghzH\"," +
                "    \"Fee\": \"12\"," +
                "    \"Sequence\": 91," +
                "    \"SigningPubKey\": \"02eeaf2c95b668d411fc490746c52071514f6d3a7b742d91d82cb591b5443d1c59\"," +
                "    \"TransactionType\": \"Payment\"," +
                "    \"TxnSignature\": \"3045022100f1b54ed137dc491240b93c4b34a97ca6063490cca784c9c2f5d5b8593f10f0410220338e9e0f6dfacc739172d0473b5023f068c5fbbcbd66e65ed5ec6f4421781194\"" +
                "}";

        Hash256 expected = Hash256.fromHex("78794CD91D01F144FBEBE3ECB0690B159E04829CE576B75B7E8ABF8B8FA7DD97");
        STObject tx = STObject.fromJSON(tx_json);
        Hash256 rehashed = Index.transactionID(tx.toBytes());
        assertEquals(expected, rehashed);
    }

    @Test
    public void testSigningHashCreation() throws Exception {
        String tx = "{\"Account\": \"rwMyB1diFJ7xqEKYGYgk9tKrforvTr33M5\"," +
                " \"Amount\": \"1\"," +
                " \"Destination\": \"rP1coskQzayaQ9geMdJgAV5f3tNZcHghzH\"," +
                " \"Fee\": \"15\"," +
                " \"Flags\": 0," +
                " \"Sequence\": 35," +
                " \"SigningPubKey\": \"02EEAF2C95B668D411FC490746C52071514F6D3A7B742D91D82CB591B5443D1C59\"," +
                " \"TransactionType\": \"Payment\"," +
                " \"TxnSignature\": \"3046022100B03C1BCE9AB7304F331B0661B3E9440506AC206F6BD0738EEEF087EC5F0B6175022100A0CF6B2A6B23C57D1471AE77B8C122C5D6CCE68DD78786F355718CB43B7D5E29\"," +
                " \"hash\": \"F9B68783CDA32F18DB28A5693DF65C5C037477EB7453AB7813051D1CECDEF9FF\"}";

        JSONObject j = new JSONObject(tx);
        j.remove("TxnSignature");
        j.remove("hash");

        STObject so = STObject.fromJSONObject(j);
        byte[] blob = so.toBytes();

        String expected = "1200002200000000240000002361400000000000000168400000000000000F732102EEAF2C95B668D411FC490746C52071514F6D3A7B742D91D82CB591B5443D1C59811466B05AAE728123957EF8411C44B787650C27231D8314FAE571D0D376CC2BFBB7D5C4E21374FA45BB3639";
        assertEquals(expected, so.toHex());

        Hash256 reHash = Hash256.signingHash(blob);
        assertEquals("63641BEDC50E9D2C1519042E78CFB53354DE94144ED67ED8C1F05A3621219209", reHash.toHex());
    }
}
