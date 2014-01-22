package com.ripple.core.types.known.sle;

import com.ripple.core.enums.LedgerEntryType;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.STArray;
import com.ripple.core.coretypes.STObject;
import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import static junit.framework.TestCase.*;

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

//    @Test
    // TODO: move this into an example
    public void testOfferQuality() throws Exception {
        JSONObject transaction = new JSONObject(getFileText("offer-create-txn.json"));
        JSONObject metaJSON = (JSONObject) transaction.remove("meta");
        STObject meta = STObject.translate.fromJSONObject(metaJSON);
        STObject txn = STObject.translate.fromJSONObject(transaction);

        ArrayList<Offer> offersExecuted = new ArrayList<Offer>();

        Amount gets = txn.get(Amount.TakerGets);
        Amount pays = txn.get(Amount.TakerPays);

        System.out.println("---------------------------------------------------------------");
        System.out.println("OfferCreate ");
        System.out.println("---------------------------------------------------------------");
        System.out.println("Get/Pay:    " + gets.currencyString() + "/" + pays.currencyString());
        System.out.println("Bid:        " + gets.computeQuality(pays));
        System.out.println("TakerPays:  " + pays);
        System.out.println("TakerGets:  " + gets);
        System.out.println(txn.prettyJSON());

        STArray affectedNodes = meta.get(STArray.AffectedNodes);
        for (STObject node : affectedNodes) {
            if (!node.isCreatedNode()) {
                // Merge fields from node / node.FinalFields && node.PreviousFields
                // to determine state of node prior to transaction.
                // Any fields that were in PreviousFields will be have their final values
                // in a nested STObject keyed by FinalFields.
                STObject asPrevious = node.nodeAsPrevious();
                // If it's an offer
                if (asPrevious.ledgerEntryType() == LedgerEntryType.Offer) {
                    // We can down-cast this to use Offer specific methods
                    Offer offer = (Offer) asPrevious;
                    offersExecuted.add(offer);
                }
            } else {
                STObject asFinal = node.nodeAsPrevious();
                if (asFinal.ledgerEntryType() == LedgerEntryType.Offer) {
                    Offer offer = (Offer) asFinal;

                    System.out.println("---------------------------------------------------------------");
                    System.out.println("Offer Created");
                    System.out.println("---------------------------------------------------------------");
                    System.out.println("Get/Pay:    " + offer.getPayCurrencyPair());
                    System.out.println("Bid:        " + offer.bidQuality());
                    System.out.println("TakerPays:  " + offer.takerPays());
                    System.out.println("TakerGets:  " + offer.takerGets());
                    System.out.println("---------------------------------------------------------------");
                    System.out.println(offer.prettyJSON());


//                    System.out.println(offer.getPayCurrencyPair());
//                    System.out.println(offer.bidQuality());
//                    System.out.println(BigDecimal.ONE.divide(offer.directoryAskQuality(), Amount.MATH_CONTEXT));
                }
            }
        }

        Collections.sort(offersExecuted, Offer.qualityAscending);

        for (Offer offer : offersExecuted) {
            STObject executed = offer.inOut(offer.get(STObject.FinalFields));

            // This will be computed from the BookDirectory field
            System.out.println("---------------------------------------------------------------");
            System.out.println("Offer Executed");
            System.out.println("---------------------------------------------------------------");
            System.out.println("Get/Pay: " + offer.getPayCurrencyPair());
            System.out.println("Ask:     " + offer.directoryAskQuality().stripTrailingZeros().toPlainString());
            System.out.println("Paid:    " + executed.get(Amount.TakerPays));
            System.out.println("Got:     " + executed.get(Amount.TakerGets));
            System.out.println("---------------------------------------------------------------");
            System.out.println(offer.prettyJSON());
        }

    }

    public String getFileText(String filename) throws IOException {
        FileReader f = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(f);
        StringBuilder b = new StringBuilder();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            b.append(line);
        }
        return b.toString();
    }
}
