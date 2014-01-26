
package com.ripple.client;

import com.ripple.client.transactions.ManagedTxn;
import com.ripple.core.enums.TransactionType;
import com.ripple.core.fields.Field;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.STObject;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt32;
import org.json.JSONObject;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class TransactionTest {

    @Test
    public void testTransactionIDCreation() throws Exception {
        String tx_json = "{"
                +
                "    \"Account\": \"rwMyB1diFJ7xqEKYGYgk9tKrforvTr33M5\","
                +
                "    \"Amount\": \"1\","
                +
                "    \"Destination\": \"rP1coskQzayaQ9geMdJgAV5f3tNZcHghzH\","
                +
                "    \"Fee\": \"12\","
                +
                "    \"Sequence\": 88,"
                +
                "    \"SigningPubKey\": \"02EEAF2C95B668D411FC490746C52071514F6D3A7B742D91D82CB591B5443D1C59\","
                +
                "    \"TransactionType\": \"Payment\","
                +
                "    \"TxnSignature\": \"304602210099A6999CD967ADD1E5A96EE62B2701AF57AA44191FE755D4976BCAFD95484238022100CAF57DB851B1AE174005CABECCBDF418AC33EF0C87C9BB4DEC2A9407F829207B\","
                +
                "    \"hash\": \"B01AC8EBCE6029AB1159258D249CBDCA139C4C6346592494D0E3C0DE6247219D\""
                +
                "}";
        STObject tx = STObject.fromJSONObject(new JSONObject(tx_json));
        Hash256 hash = (Hash256) tx.remove(Field.hash);
        Hash256 rehashed = Hash256.transactionID(tx.toBytes());
//        System.out.printf("hash: %s rehashed: %s \n", hash, rehashed);
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

        Hash256 expected = Hash256.translate
                .fromString("78794CD91D01F144FBEBE3ECB0690B159E04829CE576B75B7E8ABF8B8FA7DD97");
        STObject tx = STObject.fromJSONObject(new JSONObject(tx_json));
        Hash256 rehashed = Hash256.transactionID(tx.toBytes());
        assertEquals(expected, rehashed);

    }

    @Test
    public void testCreatePaymentTransaction() throws Exception {
        final String niqwit1Seed = "snSq7dKr5v39hJ8Enb45RpXFJL25h";
        final AccountID niqwit1 = AccountID.fromSeedString(niqwit1Seed);

        ManagedTxn transaction = new ManagedTxn(TransactionType.Payment);
        transaction.prepare(
                niqwit1.getKeyPair(),
                Amount.fromString("15"),
                new UInt32(1));
    }

    @Test
    public void testSerializationAndSigning() throws Exception {
        String tx = "{\"Account\": \"rwMyB1diFJ7xqEKYGYgk9tKrforvTr33M5\","
                +
                " \"Amount\": \"1\","
                +
                " \"Destination\": \"rP1coskQzayaQ9geMdJgAV5f3tNZcHghzH\","
                +
                " \"Fee\": \"15\","
                +
                " \"Flags\": 0,"
                +
                " \"Sequence\": 35,"
                +
                " \"SigningPubKey\": \"02EEAF2C95B668D411FC490746C52071514F6D3A7B742D91D82CB591B5443D1C59\","
                +
                " \"TransactionType\": \"Payment\","
                +
                " \"TxnSignature\": \"3046022100B03C1BCE9AB7304F331B0661B3E9440506AC206F6BD0738EEEF087EC5F0B6175022100A0CF6B2A6B23C57D1471AE77B8C122C5D6CCE68DD78786F355718CB43B7D5E29\","
                +
                " \"hash\": \"F9B68783CDA32F18DB28A5693DF65C5C037477EB7453AB7813051D1CECDEF9FF\"}";

        JSONObject j = new JSONObject(tx);
        String txnSignature = (String) j.remove("TxnSignature");
        String hashString = (String) j.remove("hash");
        STObject so = STObject.fromJSONObject(j);
        byte[] blob = so.toBytes();

        String expected = "1200002200000000240000002361400000000000000168400000000000000F732102EEAF2C95B668D411FC490746C52071514F6D3A7B742D91D82CB591B5443D1C59811466B05AAE728123957EF8411C44B787650C27231D8314FAE571D0D376CC2BFBB7D5C4E21374FA45BB3639";
        assertEquals(expected, so.toHex());

        Hash256 reHash = Hash256.signingHash(blob);
        assertEquals("63641BEDC50E9D2C1519042E78CFB53354DE94144ED67ED8C1F05A3621219209", reHash
                .toString().toUpperCase());
    }
}
