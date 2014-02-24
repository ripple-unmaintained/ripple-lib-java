package com.ripple.core.types.known.tx.result;

import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.STObject;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.coretypes.uint.UInt8;
import com.ripple.core.enums.TransactionEngineResult;
import com.ripple.core.enums.TransactionType;
import com.ripple.core.fields.Field;
import com.ripple.encodings.common.B16;
import org.json.JSONException;
import org.json.JSONObject;

public class TransactionResult {

    public TransactionEngineResult engineResult;
    public Hash256 ledgerHash;
    public Hash256 hash;

    public UInt32 ledgerIndex;
    public boolean validated;

    public STObject         transaction;
    public TransactionMeta  meta;
    public JSONObject       message;

    public boolean isPayment() {
        return transactionType() == TransactionType.Payment;
    }

    public AccountID destinationAccount() {
        return transaction.get(AccountID.Destination);
    }

    public TransactionType transactionType() {
        return transaction.transactionType();
    }

    public enum Source {
        request_tx_result,
        request_account_tx,
        request_account_tx_binary,
        transaction_subscription_notification
    }

    public TransactionResult(JSONObject json, Source resultMessageSource) {
        message = json;

        try {
            if (resultMessageSource == Source.transaction_subscription_notification) {

                engineResult = TransactionEngineResult.valueOf(json.getString("engine_result"));
                validated = json.getBoolean("validated");
                ledgerHash = Hash256.translate.fromString(json.getString("ledger_hash"));
                ledgerIndex = new UInt32(json.getLong("ledger_index"));

                if (json.has("transaction")) {
                    transaction = STObject.fromJSONObject(json.getJSONObject("transaction"));
                    hash = transaction.get(Hash256.hash);
                }

                if (json.has("meta")) {
                    meta = (TransactionMeta) STObject.fromJSONObject(json.getJSONObject("meta"));
                }
            }  else if (resultMessageSource == Source.request_tx_result) {
                validated = json.optBoolean("validated", false);
                if (validated && !json.has("meta")) {
                    throw new IllegalStateException("It's validated, why doesn't it have meta??");
                }
                if (validated) {
                    meta = (TransactionMeta) STObject.fromJSONObject(json.getJSONObject("meta"));
                    engineResult = TransactionEngineResult.fromNumber(meta.get(UInt8.TransactionResult));
                    transaction = STObject.fromJSONObject(json);
                    hash = transaction.get(Hash256.hash);
                    ledgerHash = null; // XXXXXX
                }
            } else if (resultMessageSource == Source.request_account_tx) {
                validated = json.optBoolean("validated", false);
                if (validated && !json.has("meta")) {
                    throw new IllegalStateException("It's validated, why doesn't it have meta??");
                }
                if (validated) {
                    JSONObject tx = json.getJSONObject("tx");
                    meta = (TransactionMeta) STObject.fromJSONObject(json.getJSONObject("meta"));
                    engineResult = TransactionEngineResult.fromNumber(meta.get(UInt8.TransactionResult));
                    transaction = STObject.fromJSONObject(tx);
                    hash = transaction.get(Hash256.hash);
                    ledgerIndex = new UInt32(tx.getLong("ledger_index"));
                    ledgerHash = null;
                }
            } else if (resultMessageSource == Source.request_account_tx_binary) {
                validated = json.optBoolean("validated", false);
                if (validated && !json.has("meta")) {
                    throw new IllegalStateException("It's validated, why doesn't it have meta??");
                }
                if (validated) {
                    /*
                    {
                      "ledger_index": 3378767,
                      "meta": "201 ...",
                      "tx_blob": "120 ...",
                      "validated": true
                    },
                    */

                    String tx = json.getString("tx_blob");
                    byte[] decodedTx = B16.decode(tx);
                    meta = (TransactionMeta) STObject.translate.fromHex(json.getString("meta"));
                    transaction = STObject.translate.fromBytes(decodedTx);
                    hash = Hash256.transactionID(decodedTx);
                    transaction.put(Field.hash, hash);

                    engineResult = meta.transactionResult();
                    ledgerIndex = new UInt32(json.getLong("ledger_index"));
                    ledgerHash = null;
                }
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
