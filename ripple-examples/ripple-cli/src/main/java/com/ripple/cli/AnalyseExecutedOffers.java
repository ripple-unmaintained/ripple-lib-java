package com.ripple.cli;

import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.STArray;
import com.ripple.core.coretypes.STObject;
import com.ripple.core.enums.LedgerEntryType;
import com.ripple.core.types.known.sle.LedgerEntry;
import com.ripple.core.types.known.sle.entries.Offer;
import com.ripple.core.types.known.tx.result.AffectedNode;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class AnalyseExecutedOffers {
    static public String getFileText(String filename) throws IOException {
        FileReader f = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(f);
        StringBuilder b = new StringBuilder();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            b.append(line);
        }
        return b.toString();
    }

    static public void main(String[] args) throws Exception {
        testOfferQuality();
    }

    public static void testOfferQuality() throws Exception {
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

        STArray nodes = meta.get(STArray.AffectedNodes);

        for (STObject raw : nodes) {
            AffectedNode node = (AffectedNode) raw;

            if (!node.isCreatedNode()) {
                // Merge fields from node / node.FinalFields && node.PreviousFields
                // to determine state of node prior to transaction.
                // Any fields that were in PreviousFields will be have their final values
                // in a nested STObject keyed by FinalFields.
                LedgerEntry asPrevious = (LedgerEntry) node.nodeAsPrevious();
                // If it's an offer
                if (asPrevious.ledgerEntryType() == LedgerEntryType.Offer) {
                    // We can down-cast this to use Offer specific methods
                    Offer offer = (Offer) asPrevious;
                    offersExecuted.add(offer);
                }
            } else {
                LedgerEntry asFinal = (LedgerEntry) node.nodeAsPrevious();
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
            STObject executed = offer.executed(offer.get(STObject.FinalFields));

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
}