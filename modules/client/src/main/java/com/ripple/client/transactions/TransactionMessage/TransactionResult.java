package com.ripple.client.transactions.TransactionMessage;

import com.ripple.core.enums.LedgerEntryType;
import com.ripple.core.enums.TransactionEngineResult;
import com.ripple.core.enums.TransactionType;
import com.ripple.core.fields.Field;
import com.ripple.core.types.AccountID;
import com.ripple.core.types.STArray;
import com.ripple.core.types.STObject;
import com.ripple.core.types.hash.Hash256;
import com.ripple.core.types.uint.UInt32;
import com.ripple.core.types.uint.UInt8;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TransactionResult {
    public TransactionEngineResult engineResult;
    public Hash256 ledgerHash;
    public Hash256 hash;

    public UInt32 ledgerIndex;
    public boolean validated;

    public STObject transaction;
    STObject meta;

    public JSONObject message;

    public TransactionType transactionType() {
        return transaction.transactionType();
    }

    public enum Source {
        request_tx_result,
        request_account_tx, transaction_subscription_notification
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
                    meta = STObject.fromJSONObject(json.getJSONObject("meta"));
                }
            }  else if (resultMessageSource == Source.request_tx_result) {
                validated = json.optBoolean("validated", false);
                if (validated && !json.has("meta")) {
                    throw new IllegalStateException("It's validated, why doesn't it have meta??");
                }
                if (validated) {
                    meta = STObject.fromJSONObject(json.getJSONObject("meta"));
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
                    meta = STObject.fromJSONObject(json.getJSONObject("meta"));
                    engineResult = TransactionEngineResult.fromNumber(meta.get(UInt8.TransactionResult));
                    transaction = STObject.fromJSONObject(tx);
                    hash = transaction.get(Hash256.hash);
                    ledgerIndex = new UInt32(tx.getLong("ledger_index"));
                    ledgerHash = null;
                }
            }


        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public AccountID createdAccount() {
        if (transactionType() == TransactionType.Payment &&  meta.has(Field.AffectedNodes)) {
            STArray affected = meta.get(STArray.AffectedNodes);
            for (STObject node : affected) {
                if (node.has(Field.CreatedNode)) {
                    STObject created = node.get(STObject.CreatedNode);

                    if (created.ledgerEntryType() == LedgerEntryType.AccountRoot) {
                        AccountID accountID = transaction.get(AccountID.Destination);
                        if (Hash256.accountIDLedgerIndex(accountID).equals(created.get(Hash256.LedgerIndex))) {
                            return accountID;
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
                    if (node.ledgerEntryType() == LedgerEntryType.AccountRoot) {
                        STObject finalFields = node.get(STObject.FinalFields);
                        AccountID key;

                        if (finalFields != null) {
                            key = finalFields.get(AccountID.Account);
                            accounts.put(key, node);
                        } else {

//                            key = initiatingAccount();
//                            Hash256 ledgerIndex = Hash256.accountIDLedgerIndex(key);
//                            if (ledgerIndex.equals(node.get(Hash256.LedgerIndex))) {
//                                accounts.put(key, node);
//                            }
                        }
                    }
                }
            }
        }

        return accounts;
    }

    public AccountID initiatingAccount() {
        return transaction.get(AccountID.Account);
    }
}
