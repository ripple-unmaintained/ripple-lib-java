package com.ripple.core.types.known.tx.result;

import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.STArray;
import com.ripple.core.coretypes.STObject;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.Index;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.fields.Field;
import com.ripple.core.serialized.enums.EngineResult;
import com.ripple.core.serialized.enums.LedgerEntryType;
import com.ripple.core.serialized.enums.TransactionType;
import com.ripple.core.types.known.tx.Transaction;
import com.ripple.encodings.common.B16;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TransactionResult implements Comparable<TransactionResult>{
    // The json formatting of transaction results is a MESS
    public enum Source {
        request_tx_result,
        request_account_tx,
        request_account_tx_binary,
        request_tx_binary,
        ledger_transactions_expanded_with_ledger_index_injected,
        transaction_subscription_notification
    }

    public EngineResult engineResult;
    public UInt32 ledgerIndex;
    public Hash256 hash;

    // TODO, consider just killing this field, as not all have them
    @Deprecated
    public Hash256 ledgerHash;
    // TODO, in practice this class is only for validated results so ...
    @Deprecated
    public boolean validated;

    public TransactionResult(long ledgerIndex, Hash256 hash, Transaction txn, TransactionMeta meta) {
        this.ledgerIndex = new UInt32(ledgerIndex);
        this.hash = hash;
        this.txn = txn;
        this.meta = meta;
        this.engineResult = meta.engineResult();
        this.validated = true;
    }

    public Transaction txn;
    public TransactionMeta  meta;

    // This is deprecated because it's not always set.
    @Deprecated
    public JSONObject       message;

    public boolean isPayment() {
        return transactionType() == TransactionType.Payment;
    }

    public TransactionType transactionType() {
        return txn.transactionType();
    }

    public AccountID createdAccount() {
        AccountID destination    =  null;
        Hash256   destinationIndex =  null;

        if (transactionType() == TransactionType.Payment && meta.has(Field.AffectedNodes)) {
            STArray affected = meta.get(STArray.AffectedNodes);
            for (STObject node : affected) {
                if (node.has(STObject.CreatedNode)) {
                    STObject created = node.get(STObject.CreatedNode);
                    if (STObject.ledgerEntryType(created) == LedgerEntryType.AccountRoot) {
                        if (destination == null) {
                            destination = txn.get(AccountID.Destination);
                            destinationIndex = Index.accountRoot(destination);
                        }
                        if (destinationIndex.equals(created.get(Hash256.LedgerIndex))) {
                            return destination;
                        }
                    }
                }
            }
        }
        return null;
    }

    public Map<AccountID, STObject> modifiedRoots() {
        HashMap<AccountID, STObject> accounts = null;

        if (meta.has(Field.AffectedNodes)) {
            accounts = new HashMap<AccountID, STObject>();
            STArray affected = meta.get(STArray.AffectedNodes);
            for (STObject node : affected) {
                if (node.has(Field.ModifiedNode)) {
                    node = node.get(STObject.ModifiedNode);
                    if (STObject.ledgerEntryType(node) == LedgerEntryType.AccountRoot) {
                        STObject finalFields = node.get(STObject.FinalFields);
                        AccountID key;

                        if (finalFields != null) {
                            key = finalFields.get(AccountID.Account);
                            accounts.put(key, node);
                        }
                    }
                }
            }
        }
        return accounts;
    }

    public AccountID initiatingAccount() {
        return txn.get(AccountID.Account);
    }

    public int compareTo(TransactionResult o2) {
        TransactionResult o1 = this;
        int i = o1.ledgerIndex.compareTo(o2.ledgerIndex);
        if (i != 0) {
            return i;
        } else {
            return o1.meta.transactionIndex()
                        .compareTo(o2.meta.transactionIndex());
        }
    }

    public static TransactionResult fromJSON(JSONObject json) {
        boolean binary;

        String metaKey = json.has("meta") ? "meta" : "metaData";

        String txKey = json.has("transaction") ? "transaction" :
                       json.has("tx") ? "tx" :
                       json.has("tx_blob") ? "tx_blob" : null;

        if (txKey == null && !json.has("TransactionType")) {
            throw new RuntimeException("This json isn't a transaction " + json);
        }

        binary = txKey != null && json.get(txKey) instanceof String;

        Transaction txn;
        if (txKey == null) {
            // This should parse the `hash` field
            txn = (Transaction) STObject.fromJSONObject(json);
        } else {
            txn = (Transaction) parseObject(json, txKey, binary);
            if (json.has("hash")) {
                txn.put(Hash256.hash, Hash256.fromHex(json.getString("hash")));
            } else if (binary) {
                byte[] decode = B16.decode(json.getString(txKey));
                txn.put(Hash256.hash, Index.transactionID(decode));
            }
        }

        TransactionMeta meta = (TransactionMeta) parseObject(json, metaKey, binary);
        long ledger_index = json.optLong("ledger_index", 0);
        if (ledger_index == 0 && !binary) {
            ledger_index = json.getJSONObject(txKey).getLong("ledger_index");
        }

        TransactionResult tr = new TransactionResult(ledger_index, txn.get(Hash256.hash), txn, meta);
        if (json.has("ledger_hash")) {
            tr.ledgerHash = Hash256.fromHex(json.getString("ledger_hash"));
        }
        return tr;
    }

    private static STObject parseObject(JSONObject json, String key, boolean binary) {
        if (binary) {
            return STObject.translate.fromHex(json.getString(key));
        } else {
            JSONObject tx_json = json.getJSONObject(key);
            return STObject.translate.fromJSONObject(tx_json);
        }
    }

    public TransactionResult(JSONObject json, Source resultMessageSource) {
        message = json;

        if (resultMessageSource == Source.transaction_subscription_notification) {

            engineResult = EngineResult.valueOf(json.getString("engine_result"));
            validated = json.getBoolean("validated");
            ledgerHash = Hash256.translate.fromString(json.getString("ledger_hash"));
            ledgerIndex = new UInt32(json.getLong("ledger_index"));

            if (json.has("transaction")) {
                txn = (Transaction) STObject.fromJSONObject(json.getJSONObject("transaction"));
                hash = txn.get(Hash256.hash);
            }

            if (json.has("meta")) {
                meta = (TransactionMeta) STObject.fromJSONObject(json.getJSONObject("meta"));
            }
        }
        else if (resultMessageSource == Source.ledger_transactions_expanded_with_ledger_index_injected) {
            validated = true;
            meta = (TransactionMeta) STObject.translate.fromJSONObject(json.getJSONObject("metaData"));
            txn = (Transaction) STObject.translate.fromJSONObject(json);
            hash = txn.get(Hash256.hash);
            engineResult = meta.engineResult();
            ledgerIndex = new UInt32(json.getLong("ledger_index"));
            ledgerHash = null;

        } else if (resultMessageSource == Source.request_tx_result) {
            validated = json.optBoolean("validated", false);
            if (validated && !json.has("meta")) {
                throw new IllegalStateException("It's validated, why doesn't it have meta??");
            }
            if (validated) {
                meta = (TransactionMeta) STObject.fromJSONObject(json.getJSONObject("meta"));
                engineResult = meta.engineResult();
                txn = (Transaction) STObject.fromJSONObject(json);
                hash = txn.get(Hash256.hash);
                ledgerHash = null; // XXXXXX
                ledgerIndex = new UInt32(json.getLong("ledger_index"));

            }
        } else if (resultMessageSource == Source.request_account_tx) {
            validated = json.optBoolean("validated", false);
            if (validated && !json.has("meta")) {
                throw new IllegalStateException("It's validated, why doesn't it have meta??");
            }
            if (validated) {
                JSONObject tx = json.getJSONObject("tx");
                meta = (TransactionMeta) STObject.fromJSONObject(json.getJSONObject("meta"));
                engineResult = meta.engineResult();
                this.txn = (Transaction) STObject.fromJSONObject(tx);
                hash = this.txn.get(Hash256.hash);
                ledgerIndex = new UInt32(tx.getLong("ledger_index"));
                ledgerHash = null;
            }
        } else if (resultMessageSource == Source.request_account_tx_binary || resultMessageSource == Source.request_tx_binary) {
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
                boolean account_tx = resultMessageSource == Source.request_account_tx_binary;

                String tx = json.getString(account_tx ? "tx_blob" : "tx");
                byte[] decodedTx = B16.decode(tx);
                meta = (TransactionMeta) STObject.translate.fromHex(json.getString("meta"));
                this.txn = (Transaction) STObject.translate.fromBytes(decodedTx);

                if (account_tx) {
                    hash = Index.transactionID(decodedTx);
                } else {
                    hash = Hash256.translate.fromHex(json.getString("hash"));
                }
                this.txn.put(Field.hash, hash);

                engineResult = meta.engineResult();
                ledgerIndex = new UInt32(json.getLong("ledger_index"));
                ledgerHash = null;
            }
        }
    }

    @Override
    public String toString() {
        // message is deprecated
        if (message != null) {
            return message.toString();
        }
        else {
            JSONObject object = toJSON();
            return object.toString();
        }
    }

    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        o.put("tx", txn.toJSON());
        o.put("meta", meta.toJSON());
        o.put("ledger_index", ledgerIndex);
        o.put("hash", hash.toHex());
        return o;
    }

    public TransactionResult copy() {
        TransactionMeta metaCopy = (TransactionMeta) STObject.translate.fromBytes(meta.toBytes());
        Transaction txnCopy = (Transaction) STObject.translate.fromBytes(txn.toBytes());
        return new TransactionResult(ledgerIndex.longValue(), hash, txnCopy, metaCopy);
    }

    public JSONObject toJSONBinary() {
        JSONObject o = new JSONObject();

        o.put("hash", hash.toHex());
        o.put("meta", meta.toHex());
        o.put("tx", txn.toHex());
        o.put("ledger_index", ledgerIndex);

        return o;
    }
}
