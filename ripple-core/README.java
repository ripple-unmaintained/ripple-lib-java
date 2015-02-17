/**
 *
 * # Transaction Meta JSON
 *
 * {
 *   "TransactionResult": "tesSUCCESS",
 *
 *      {@link com.ripple.core.types.known.tx.result.TransactionResult}
 *      {@link com.ripple.core.fields.Field.TransactionResult}
 *      {@link com.ripple.core.coretypes.uint.UInt8}
 *
 *   "TransactionIndex": 0,
 *
 *      {@link com.ripple.core.types.known.tx.result.TransactionMeta#transactionIndex}
 *      {@link com.ripple.core.coretypes.uint.UInt32}
 *
 *   "AffectedNodes": [
 *
 *      {@link com.ripple.core.types.known.tx.result.TransactionMeta#affectedNodes}
 *
 *     {
 *       "CreatedNode": {
 *
 *          {@link com.ripple.core.types.known.tx.result.AffectedNode}
 *          {@link com.ripple.core.types.known.tx.result.AffectedNode#isCreatedNode()}
 *
 *         "NewFields": {
 *
 *              {@link com.ripple.core.types.known.tx.result.AffectedNode#nodeAsFinal()}
 *
 *           "Sequence": 103929,
 *
 *              {@link com.ripple.core.fields.Field#Sequence}
 *
 *
 *           "TakerGets": {
 *
 *              {@link com.ripple.core.coretypes.Amount}
 *
 *             "currency": "ILS",
 *
 *                  {@link com.ripple.core.coretypes.Currency}
 *
 *             "value": "1694.768",
 *
 *                  {@link com.ripple.core.coretypes.AccountID}
 *
 *             "issuer": "rNPRNzBB92BVpAhhZr4iXDTveCgV5Pofm9"
 *
 *                  {@link java.math.BigDecimal}
 *
 *           },
 *
 *
 *           "Account": "raD5qJMAShLeHZXf9wjUmo6vRK4arj9cF3",
 *
 *              {@link com.ripple.core.coretypes.AccountID}
 *
 *           "BookDirectory": "62A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F000",
 *
 *              {@link com.ripple.core.coretypes.Quality#fromBookDirectory}
 *
 *           "TakerPays": "98957503520",
 *
 *              {@link com.ripple.core.coretypes.Amount}
 *              {@link com.ripple.core.coretypes.Amount#isNative()}
 *
 *           "OwnerNode": "000000000000000E"
 *
 *              {@link com.ripple.core.types.known.sle.entries.Offer#ownerNodeDirectoryIndex}
 *         },
 *
 *         "LedgerIndex": "3596CE72C902BAFAAB56CC486ACAF9B4AFC67CF7CADBB81A4AA9CBDC8C5CB1AA",
 *
 *              {@link com.ripple.core.coretypes.hash.Hash256#index}
 *
 *         "LedgerEntryType": "Offer"
 *
 *              {@link com.ripple.core.serialized.enums.LedgerEntryType}
 *              {@link com.ripple.core.types.known.sle.entries.Offer}
 *       }
 *     },
 *
 *
 *     {
 *       "CreatedNode": {
 *
 *          {@link com.ripple.core.types.known.tx.result.AffectedNode}
 *          {@link com.ripple.core.types.known.tx.result.AffectedNode#isCreatedNode()}
 *
 *         "NewFields": {
 *
 *              {@link com.ripple.core.types.known.tx.result.AffectedNode#nodeAsFinal()}
 *
 *           "RootIndex": "62A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F000",
 *
 *              com.ripple.core.coretypes.hash.Hash256#index
 *
 *           "TakerGetsIssuer": "92D705968936C419CE614BF264B5EEB1CEA47FF4",
 *
 *              {@link com.ripple.core.coretypes.Currency}
 *              {@link com.ripple.core.coretypes.hash.Hash160}
 *              {@link com.ripple.core.fields.Field#TakerGetsIssuer}
 *
 *           "ExchangeRate": "5C14BE8A20D7F000",
 *
 *              {@link com.ripple.core.coretypes.Quality#fromBookDirectory}
 *
 *           "TakerGetsCurrency": "000000000000000000000000494C530000000000"
 *
 *              {@link com.ripple.core.coretypes.Currency}
 *              {@link com.ripple.core.coretypes.hash.Hash160}
 *              {@link com.ripple.core.fields.Field#TakerGetsCurrency}
 *
 *         },
 *
 *         "LedgerIndex": "62A3338CAF2E1BEE510FC33DE1863C56948E962CCE173CA55C14BE8A20D7F000",
 *
 *              {@link com.ripple.core.coretypes.hash.Hash256#index}
 *
 *         "LedgerEntryType": "DirectoryNode"
 *
 *              {@link com.ripple.core.types.known.sle.entries.DirectoryNode}
 *              {@link com.ripple.core.serialized.enums.LedgerEntryType#DirectoryNode}
 *       }
 *     },
 *
 *     {
 *       "ModifiedNode": {
 *
 *          {@link com.ripple.core.types.known.tx.result.AffectedNode}
 *          {@link com.ripple.core.types.known.tx.result.AffectedNode#isModifiedNode()}
 *
 *         "FinalFields": {
 *
 *              {@link com.ripple.core.types.known.tx.result.AffectedNode#nodeAsFinal()}
 *
 *           "RootIndex": "801C5AFB5862D4666D0DF8E5BE1385DC9B421ED09A4269542A07BC0267584B64",
 *
 *              {@link com.ripple.core.types.known.sle.entries.DirectoryNode#rootIndex}
 *
 *           "Flags": 0,
 *           "Owner": "raD5qJMAShLeHZXf9wjUmo6vRK4arj9cF3",
 *
 *              {@link com.ripple.core.coretypes.hash.Index#ownerDirectory}
 *
 *           "IndexPrevious": "0000000000000000"
 *
 *              {@link com.ripple.core.types.known.sle.entries.DirectoryNode#hasPreviousIndex}
 *              {@link com.ripple.core.types.known.sle.entries.DirectoryNode#prevIndex}
 *         },
 *
 *         "LedgerIndex": "AB03F8AA02FFA4635E7CE2850416AEC5542910A2B4DBE93C318FEB08375E0DB5",
 *
 *         "LedgerEntryType": "DirectoryNode"
 *       }
 *     },
 *     {
 *       "ModifiedNode": {
 *
 *          {@link com.ripple.core.types.known.tx.result.AffectedNode}
 *          {@link com.ripple.core.types.known.tx.result.AffectedNode#isModifiedNode()}
 *
 *         "FinalFields": {
 *
 *              {@link com.ripple.core.types.known.tx.result.AffectedNode#nodeAsFinal()}
 *
 *           "Sequence": 103930,
 *           "Flags": 0,
 *           "Account": "raD5qJMAShLeHZXf9wjUmo6vRK4arj9cF3",
 *           "OwnerCount": 9,
 *           "Balance": "106861218302"
 *
 *              {@link com.ripple.core.coretypes.Amount}
 *         },
 *
 *         "LedgerIndex": "CF23A37E39A571A0F22EC3E97EB0169936B520C3088963F16C5EE4AC59130B1B",
 *
 *              {@link com.ripple.core.coretypes.hash.Index#accountRoot}
 *
 *         "LedgerEntryType": "AccountRoot",
 *
 *              {@link com.ripple.core.types.known.sle.entries.AccountRoot}
 *              {@link com.ripple.core.serialized.enums.LedgerEntryType#AccountRoot}
 *
 *         "PreviousFields": {
 *
 *              {@link com.ripple.core.types.known.tx.result.AffectedNode#nodeAsPrevious()}
 *
 *           "Sequence": 103929,
 *           "OwnerCount": 8,
 *           "Balance": "106861218312"
 *         },
 *
 *         "PreviousTxnID": "DE15F43F4A73C4F6CB1C334D9E47BDE84467C0902796BB81D4924885D1C11E6D",
 *
 *              {@link com.ripple.core.types.known.tx.Transaction#hash}
 *
 *         "PreviousTxnLgrSeq": 3225338
 *
 *              {@link com.ripple.core.coretypes.uint.UInt32}
 *       }
 *     }
 *   ]
 * }
*/