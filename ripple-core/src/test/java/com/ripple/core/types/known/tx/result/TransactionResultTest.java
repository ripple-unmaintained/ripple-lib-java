package com.ripple.core.types.known.tx.result;

import com.ripple.core.serialized.enums.EngineResult;
import com.ripple.core.types.known.tx.txns.Payment;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static com.ripple.core.types.known.tx.result.TransactionResult.Source;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransactionResultTest {
    /*
        DONE:
            transaction_subscription_notification
            request_account_tx,
            request_account_tx_binary,
            request_tx_result,
            request_tx_binary,

        ledger_transactions_expanded_with_ledger_index_injected,

    * */

    public static final JSONObject transaction_notification_message;
    public static final JSONObject account_tx_result;
    public static final JSONObject account_tx_binary_result;

    public static final JSONObject request_tx_result;
    public static final JSONObject request_tx_binary_result;
    public static final JSONObject ledger_expanded;

    static {
        ledger_expanded = new JSONObject("{"+
                "   \"result\" : {"+
                "      \"ledger\" : {"+
                "         \"accepted\" : true,"+
                "         \"accountState\" : ["+
                "            {"+
                "               \"Account\" : \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\","+
                "               \"Balance\" : \"99999993999999940\","+
                "               \"Flags\" : 0,"+
                "               \"LedgerEntryType\" : \"AccountRoot\","+
                "               \"OwnerCount\" : 0,"+
                "               \"PreviousTxnID\" : \"235DA149DDA3BD32B886C132ABDE60CC5AD2C5693652F1E1725565E4B3D425B4\","+
                "               \"PreviousTxnLgrSeq\" : 7,"+
                "               \"Sequence\" : 7,"+
                "               \"index\" : \"2B6AC232AA4C4BE41BF49D2459FA4A0347E1B543A4C92FCEE0821C0201E2E9A8\""+
                "            },"+
                "            {"+
                "               \"Flags\" : 0,"+
                "               \"Hashes\" : ["+
                "                  \"84F68FB0D5AE051A4F6090440369620686050390A404C581CD1EB8FFA12C58FE\","+
                "                  \"6408B6FED471D850E883810BC69D2375E1D819DB78B49F012A30296EF911D04F\","+
                "                  \"CA431AF1781EC990E04817312390CF9469FC1C1AD8BF8BC85677377B44452E47\","+
                "                  \"AAC6148B6745A36D0911D5ED85844B13D98FD68A8B75E65EA663F3BD1678CBBD\","+
                "                  \"DE29173A6AF2FBDCF5A634D5EACEA6EAD1CFAFC0751820CE9FFE6CD1E85A9311\""+
                "               ],"+
                "               \"LastLedgerSequence\" : 6,"+
                "               \"LedgerEntryType\" : \"LedgerHashes\","+
                "               \"index\" : \"B4979A36CDC7F3D3D5C31A4EAE2AC7D7209DDA877588B9AFC66799692AB0D66B\""+
                "            },"+
                "            {"+
                "               \"Account\" : \"rPMh7Pi9ct699iZUTWaytJUoHcJ7cgyziK\","+
                "               \"Balance\" : \"6000000000\","+
                "               \"Flags\" : 0,"+
                "               \"LedgerEntryType\" : \"AccountRoot\","+
                "               \"OwnerCount\" : 0,"+
                "               \"PreviousTxnID\" : \"235DA149DDA3BD32B886C132ABDE60CC5AD2C5693652F1E1725565E4B3D425B4\","+
                "               \"PreviousTxnLgrSeq\" : 7,"+
                "               \"Sequence\" : 1,"+
                "               \"index\" : \"DE3BE7FDF6864FB024807B36BFCB4607E7CDA7D4C155C7AFB4B0973D638938BF\""+
                "            }"+
                "         ],"+
                "         \"account_hash\" : \"5691303DE8A8B6A01661329A2EF10BC1EB906130583CE2E383FA22ECE2D91F86\","+
                "         \"close_time\" : 464678250,"+
                "         \"close_time_human\" : \"2014-Sep-22 05:17:30\","+
                "         \"close_time_resolution\" : 30,"+
                "         \"closed\" : true,"+
                "         \"hash\" : \"B885C2935021E6BD64C4F7BA79B838797BC2D1AD2883DFC811B1D80D9E5E890D\","+
                "         \"ledger_hash\" : \"B885C2935021E6BD64C4F7BA79B838797BC2D1AD2883DFC811B1D80D9E5E890D\","+
                "         \"ledger_index\" : \"7\","+
                "         \"parent_hash\" : \"DE29173A6AF2FBDCF5A634D5EACEA6EAD1CFAFC0751820CE9FFE6CD1E85A9311\","+
                "         \"seqNum\" : \"7\","+
                "         \"totalCoins\" : \"99999999999999940\","+
                "         \"total_coins\" : \"99999999999999940\","+
                "         \"transaction_hash\" : \"B2F089C16A06CF4087EA6D61F8DED914DD3378DA345F9F48293B370DCB493644\","+
                "         \"transactions\" : ["+
                "            {"+
                "               \"Account\" : \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\","+
                "               \"Amount\" : \"1000000000\","+
                "               \"Destination\" : \"rPMh7Pi9ct699iZUTWaytJUoHcJ7cgyziK\","+
                "               \"Fee\" : \"10\","+
                "               \"Flags\" : 2147483648,"+
                "               \"Sequence\" : 6,"+
                "               \"SigningPubKey\" : \"0330E7FC9D56BB25D6893BA3F317AE5BCF33B3291BD63DB32654A313222F7FD020\","+
                "               \"TransactionType\" : \"Payment\","+
                "               \"TxnSignature\" : \"3044022063FC9F65D0B8F3A13FAE8BBC91E34930C25725388667B430EF1EEB2F6024FB6502201AFE9E4AA947384880D5768C7C574E03C42ABDF2000CF736DED9FF7BF999B09B\","+
                "               \"hash\" : \"235DA149DDA3BD32B886C132ABDE60CC5AD2C5693652F1E1725565E4B3D425B4\","+
                "               \"metaData\" : {"+
                "                  \"AffectedNodes\" : ["+
                "                     {"+
                "                        \"ModifiedNode\" : {"+
                "                           \"FinalFields\" : {"+
                "                              \"Account\" : \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\","+
                "                              \"Balance\" : \"99999993999999940\","+
                "                              \"Flags\" : 0,"+
                "                              \"OwnerCount\" : 0,"+
                "                              \"Sequence\" : 7"+
                "                           },"+
                "                           \"LedgerEntryType\" : \"AccountRoot\","+
                "                           \"LedgerIndex\" : \"2B6AC232AA4C4BE41BF49D2459FA4A0347E1B543A4C92FCEE0821C0201E2E9A8\","+
                "                           \"PreviousFields\" : {"+
                "                              \"Balance\" : \"99999994999999950\","+
                "                              \"Sequence\" : 6"+
                "                           },"+
                "                           \"PreviousTxnID\" : \"D66E6A2F99D4B23AC650B547294EE3F61CF756D82A983AA40B70371A05CDEA85\","+
                "                           \"PreviousTxnLgrSeq\" : 6"+
                "                        }"+
                "                     },"+
                "                     {"+
                "                        \"ModifiedNode\" : {"+
                "                           \"FinalFields\" : {"+
                "                              \"Account\" : \"rPMh7Pi9ct699iZUTWaytJUoHcJ7cgyziK\","+
                "                              \"Balance\" : \"6000000000\","+
                "                              \"Flags\" : 0,"+
                "                              \"OwnerCount\" : 0,"+
                "                              \"Sequence\" : 1"+
                "                           },"+
                "                           \"LedgerEntryType\" : \"AccountRoot\","+
                "                           \"LedgerIndex\" : \"DE3BE7FDF6864FB024807B36BFCB4607E7CDA7D4C155C7AFB4B0973D638938BF\","+
                "                           \"PreviousFields\" : {"+
                "                              \"Balance\" : \"5000000000\""+
                "                           },"+
                "                           \"PreviousTxnID\" : \"D66E6A2F99D4B23AC650B547294EE3F61CF756D82A983AA40B70371A05CDEA85\","+
                "                           \"PreviousTxnLgrSeq\" : 6"+
                "                        }"+
                "                     }"+
                "                  ],"+
                "                  \"TransactionIndex\" : 0,"+
                "                  \"TransactionResult\" : \"tesSUCCESS\""+
                "               }"+
                "            }"+
                "         ]"+
                "      },"+
                "      \"status\" : \"success\""+
                "   }"+
                "}");

        // Note that we parse this and then pluck the result object
        // see at the end of this code, the full response is just placed for documentation
        // value ;)
        request_tx_result = new JSONObject("{" +
                "   \"result\" : {" +
                "      \"Account\" : \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\"," +
                "      \"Amount\" : \"1000000000\"," +
                "      \"Destination\" : \"rPMh7Pi9ct699iZUTWaytJUoHcJ7cgyziK\"," +
                "      \"Fee\" : \"10\"," +
                "      \"Flags\" : 2147483648," +
                "      \"Sequence\" : 6," +
                "      \"SigningPubKey\" : \"0330E7FC9D56BB25D6893BA3F317AE5BCF33B3291BD63DB32654A313222F7FD020\"," +
                "      \"TransactionType\" : \"Payment\"," +
                "      \"TxnSignature\" : \"3044022063FC9F65D0B8F3A13FAE8BBC91E34930C25725388667B430EF1EEB2F6024FB6502201AFE9E4AA947384880D5768C7C574E03C42ABDF2000CF736DED9FF7BF999B09B\"," +
                "      \"date\" : 464678250," +
                "      \"hash\" : \"235DA149DDA3BD32B886C132ABDE60CC5AD2C5693652F1E1725565E4B3D425B4\"," +
                "      \"inLedger\" : 7," +
                "      \"ledger_index\" : 7," +
                "      \"meta\" : {" +
                "         \"AffectedNodes\" : [" +
                "            {" +
                "               \"ModifiedNode\" : {" +
                "                  \"FinalFields\" : {" +
                "                     \"Account\" : \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\"," +
                "                     \"Balance\" : \"99999993999999940\"," +
                "                     \"Flags\" : 0," +
                "                     \"OwnerCount\" : 0," +
                "                     \"Sequence\" : 7" +
                "                  }," +
                "                  \"LedgerEntryType\" : \"AccountRoot\"," +
                "                  \"LedgerIndex\" : \"2B6AC232AA4C4BE41BF49D2459FA4A0347E1B543A4C92FCEE0821C0201E2E9A8\"," +
                "                  \"PreviousFields\" : {" +
                "                     \"Balance\" : \"99999994999999950\"," +
                "                     \"Sequence\" : 6" +
                "                  }," +
                "                  \"PreviousTxnID\" : \"D66E6A2F99D4B23AC650B547294EE3F61CF756D82A983AA40B70371A05CDEA85\"," +
                "                  \"PreviousTxnLgrSeq\" : 6" +
                "               }" +
                "            }," +
                "            {" +
                "               \"ModifiedNode\" : {" +
                "                  \"FinalFields\" : {" +
                "                     \"Account\" : \"rPMh7Pi9ct699iZUTWaytJUoHcJ7cgyziK\"," +
                "                     \"Balance\" : \"6000000000\"," +
                "                     \"Flags\" : 0," +
                "                     \"OwnerCount\" : 0," +
                "                     \"Sequence\" : 1" +
                "                  }," +
                "                  \"LedgerEntryType\" : \"AccountRoot\"," +
                "                  \"LedgerIndex\" : \"DE3BE7FDF6864FB024807B36BFCB4607E7CDA7D4C155C7AFB4B0973D638938BF\"," +
                "                  \"PreviousFields\" : {" +
                "                     \"Balance\" : \"5000000000\"" +
                "                  }," +
                "                  \"PreviousTxnID\" : \"D66E6A2F99D4B23AC650B547294EE3F61CF756D82A983AA40B70371A05CDEA85\"," +
                "                  \"PreviousTxnLgrSeq\" : 6" +
                "               }" +
                "            }" +
                "         ]," +
                "         \"TransactionIndex\" : 0," +
                "         \"TransactionResult\" : \"tesSUCCESS\"" +
                "      }," +
                "      \"status\" : \"success\"," +
                "      \"validated\" : true" +
                "   }" +
                "}"
        ).getJSONObject("result");

        request_tx_binary_result = new JSONObject("{" +
                "   \"result\" : {" +
                "      \"date\" : 464678250," +
                "      \"hash\" : \"235DA149DDA3BD32B886C132ABDE60CC5AD2C5693652F1E1725565E4B3D425B4\"," +
                "      \"inLedger\" : 7," +
                "      \"ledger_index\" : 7," +
                "      \"meta\" : \"201C00000000F8E5110061250000000655D66E6A2F99D4B23AC650B547294EE3F61CF756D82A983AA40B70371A05CDEA85562B6AC232AA4C4BE41BF49D2459FA4A0347E1B543A4C92FCEE0821C0201E2E9A8E62400000006624163457733840DCEE1E7220000000024000000072D000000006241634576F7E943C48114B5F762798A53D543A014CAF8B297CFF8F2F937E8E1E1E5110061250000000655D66E6A2F99D4B23AC650B547294EE3F61CF756D82A983AA40B70371A05CDEA8556DE3BE7FDF6864FB024807B36BFCB4607E7CDA7D4C155C7AFB4B0973D638938BFE662400000012A05F200E1E7220000000024000000012D00000000624000000165A0BC008114F51DFC2A09D62CBBA1DFBDD4691DAC96AD98B90FE1E1F1031000\"," +
                "      \"status\" : \"success\"," +
                "      \"tx\" : \"1200002280000000240000000661400000003B9ACA0068400000000000000A73210330E7FC9D56BB25D6893BA3F317AE5BCF33B3291BD63DB32654A313222F7FD02074463044022063FC9F65D0B8F3A13FAE8BBC91E34930C25725388667B430EF1EEB2F6024FB6502201AFE9E4AA947384880D5768C7C574E03C42ABDF2000CF736DED9FF7BF999B09B8114B5F762798A53D543A014CAF8B297CFF8F2F937E88314F51DFC2A09D62CBBA1DFBDD4691DAC96AD98B90F\"," +
                "      \"validated\" : true" +
                "   }" +
                "}"
        ).getJSONObject("result");

        // Note that we parse this and then pluck the first txn in the transactions array
        // see at the end of this code, the full response is just placed for documentation
        // value ;)
        account_tx_binary_result = new JSONObject("{" +
                "   \"result\" : {" +
                "      \"account\" : \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\"," +
                "      \"ledger_index_max\" : 7," +
                "      \"ledger_index_min\" : 7," +
                "      \"status\" : \"success\"," +
                "      \"transactions\" : [" +
                "         {" +
                "            \"ledger_index\" : 7," +
                "            \"meta\" : \"201C00000000F8E5110061250000000655D66E6A2F99D4B23AC650B547294EE3F61CF756D82A983AA40B70371A05CDEA85562B6AC232AA4C4BE41BF49D2459FA4A0347E1B543A4C92FCEE0821C0201E2E9A8E62400000006624163457733840DCEE1E7220000000024000000072D000000006241634576F7E943C48114B5F762798A53D543A014CAF8B297CFF8F2F937E8E1E1E5110061250000000655D66E6A2F99D4B23AC650B547294EE3F61CF756D82A983AA40B70371A05CDEA8556DE3BE7FDF6864FB024807B36BFCB4607E7CDA7D4C155C7AFB4B0973D638938BFE662400000012A05F200E1E7220000000024000000012D00000000624000000165A0BC008114F51DFC2A09D62CBBA1DFBDD4691DAC96AD98B90FE1E1F1031000\"," +
                "            \"tx_blob\" : \"1200002280000000240000000661400000003B9ACA0068400000000000000A73210330E7FC9D56BB25D6893BA3F317AE5BCF33B3291BD63DB32654A313222F7FD02074463044022063FC9F65D0B8F3A13FAE8BBC91E34930C25725388667B430EF1EEB2F6024FB6502201AFE9E4AA947384880D5768C7C574E03C42ABDF2000CF736DED9FF7BF999B09B8114B5F762798A53D543A014CAF8B297CFF8F2F937E88314F51DFC2A09D62CBBA1DFBDD4691DAC96AD98B90F\"," +
                "            \"validated\" : true" +
                "         }" +
                "      ]" +
                "   }" +
                "}"
        ).getJSONObject("result").getJSONArray("transactions").getJSONObject(0);

        // Note that we parse this and then pluck the first txn in the transactions array
        // see at the end of this code, the full response is just placed for documentation
        // value ;)
        account_tx_result = new JSONObject("{" +
                "   \"result\" : {" +
                "      \"account\" : \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\"," +
                "      \"ledger_index_max\" : 7," +
                "      \"ledger_index_min\" : 7," +
                "      \"status\" : \"success\"," +
                "      \"transactions\" : [" +
                "         {" +
                "            \"meta\" : {" +
                "               \"AffectedNodes\" : [" +
                "                  {" +
                "                     \"ModifiedNode\" : {" +
                "                        \"FinalFields\" : {" +
                "                           \"Account\" : \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\"," +
                "                           \"Balance\" : \"99999993999999940\"," +
                "                           \"Flags\" : 0," +
                "                           \"OwnerCount\" : 0," +
                "                           \"Sequence\" : 7" +
                "                        }," +
                "                        \"LedgerEntryType\" : \"AccountRoot\"," +
                "                        \"LedgerIndex\" : \"2B6AC232AA4C4BE41BF49D2459FA4A0347E1B543A4C92FCEE0821C0201E2E9A8\"," +
                "                        \"PreviousFields\" : {" +
                "                           \"Balance\" : \"99999994999999950\"," +
                "                           \"Sequence\" : 6" +
                "                        }," +
                "                        \"PreviousTxnID\" : \"D66E6A2F99D4B23AC650B547294EE3F61CF756D82A983AA40B70371A05CDEA85\"," +
                "                        \"PreviousTxnLgrSeq\" : 6" +
                "                     }" +
                "                  }," +
                "                  {" +
                "                     \"ModifiedNode\" : {" +
                "                        \"FinalFields\" : {" +
                "                           \"Account\" : \"rPMh7Pi9ct699iZUTWaytJUoHcJ7cgyziK\"," +
                "                           \"Balance\" : \"6000000000\"," +
                "                           \"Flags\" : 0," +
                "                           \"OwnerCount\" : 0," +
                "                           \"Sequence\" : 1" +
                "                        }," +
                "                        \"LedgerEntryType\" : \"AccountRoot\"," +
                "                        \"LedgerIndex\" : \"DE3BE7FDF6864FB024807B36BFCB4607E7CDA7D4C155C7AFB4B0973D638938BF\"," +
                "                        \"PreviousFields\" : {" +
                "                           \"Balance\" : \"5000000000\"" +
                "                        }," +
                "                        \"PreviousTxnID\" : \"D66E6A2F99D4B23AC650B547294EE3F61CF756D82A983AA40B70371A05CDEA85\"," +
                "                        \"PreviousTxnLgrSeq\" : 6" +
                "                     }" +
                "                  }" +
                "               ]," +
                "               \"TransactionIndex\" : 0," +
                "               \"TransactionResult\" : \"tesSUCCESS\"" +
                "            }," +
                "            \"tx\" : {" +
                "               \"Account\" : \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\"," +
                "               \"Amount\" : \"1000000000\"," +
                "               \"Destination\" : \"rPMh7Pi9ct699iZUTWaytJUoHcJ7cgyziK\"," +
                "               \"Fee\" : \"10\"," +
                "               \"Flags\" : 2147483648," +
                "               \"Sequence\" : 6," +
                "               \"SigningPubKey\" : \"0330E7FC9D56BB25D6893BA3F317AE5BCF33B3291BD63DB32654A313222F7FD020\"," +
                "               \"TransactionType\" : \"Payment\"," +
                "               \"TxnSignature\" : \"3044022063FC9F65D0B8F3A13FAE8BBC91E34930C25725388667B430EF1EEB2F6024FB6502201AFE9E4AA947384880D5768C7C574E03C42ABDF2000CF736DED9FF7BF999B09B\"," +
                "               \"date\" : 464678250," +
                "               \"hash\" : \"235DA149DDA3BD32B886C132ABDE60CC5AD2C5693652F1E1725565E4B3D425B4\"," +
                "               \"inLedger\" : 7," +
                "               \"ledger_index\" : 7" +
                "            }," +
                "            \"validated\" : true" +
                "         }" +
                "      ]" +
                "   }" +
                "}"
        ).getJSONObject("result").getJSONArray("transactions").getJSONObject(0);

        transaction_notification_message = new JSONObject("{" +
                "  \"engine_result\": \"tesSUCCESS\"," +
                "  \"engine_result_code\": 0," +
                "  \"engine_result_message\": \"The transaction was applied.\"," +
                "  \"ledger_hash\": \"B885C2935021E6BD64C4F7BA79B838797BC2D1AD2883DFC811B1D80D9E5E890D\"," +
                "  \"ledger_index\": 7," +
                "  \"meta\": {" +
                "    \"AffectedNodes\": [" +
                "      {" +
                "        \"ModifiedNode\": {" +
                "          \"FinalFields\": {" +
                "            \"Account\": \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\"," +
                "            \"Balance\": \"99999993999999940\"," +
                "            \"Flags\": 0," +
                "            \"OwnerCount\": 0," +
                "            \"Sequence\": 7" +
                "          }," +
                "          \"LedgerEntryType\": \"AccountRoot\"," +
                "          \"LedgerIndex\": \"2B6AC232AA4C4BE41BF49D2459FA4A0347E1B543A4C92FCEE0821C0201E2E9A8\"," +
                "          \"PreviousFields\": {" +
                "            \"Balance\": \"99999994999999950\"," +
                "            \"Sequence\": 6" +
                "          }," +
                "          \"PreviousTxnID\": \"D66E6A2F99D4B23AC650B547294EE3F61CF756D82A983AA40B70371A05CDEA85\"," +
                "          \"PreviousTxnLgrSeq\": 6" +
                "        }" +
                "      }," +
                "      {" +
                "        \"ModifiedNode\": {" +
                "          \"FinalFields\": {" +
                "            \"Account\": \"rPMh7Pi9ct699iZUTWaytJUoHcJ7cgyziK\"," +
                "            \"Balance\": \"6000000000\"," +
                "            \"Flags\": 0," +
                "            \"OwnerCount\": 0," +
                "            \"Sequence\": 1" +
                "          }," +
                "          \"LedgerEntryType\": \"AccountRoot\"," +
                "          \"LedgerIndex\": \"DE3BE7FDF6864FB024807B36BFCB4607E7CDA7D4C155C7AFB4B0973D638938BF\"," +
                "          \"PreviousFields\": {" +
                "            \"Balance\": \"5000000000\"" +
                "          }," +
                "          \"PreviousTxnID\": \"D66E6A2F99D4B23AC650B547294EE3F61CF756D82A983AA40B70371A05CDEA85\"," +
                "          \"PreviousTxnLgrSeq\": 6" +
                "        }" +
                "      }" +
                "    ]," +
                "    \"TransactionIndex\": 0," +
                "    \"TransactionResult\": \"tesSUCCESS\"" +
                "  }," +
                "  \"status\": \"closed\"," +
                "  \"transaction\": {" +
                "    \"Account\": \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\"," +
                "    \"Amount\": \"1000000000\"," +
                "    \"Destination\": \"rPMh7Pi9ct699iZUTWaytJUoHcJ7cgyziK\"," +
                "    \"Fee\": \"10\"," +
                "    \"Flags\": 2147483648," +
                "    \"Sequence\": 6," +
                "    \"SigningPubKey\": \"0330E7FC9D56BB25D6893BA3F317AE5BCF33B3291BD63DB32654A313222F7FD020\"," +
                "    \"TransactionType\": \"Payment\"," +
                "    \"TxnSignature\": \"3044022063FC9F65D0B8F3A13FAE8BBC91E34930C25725388667B430EF1EEB2F6024FB6502201AFE9E4AA947384880D5768C7C574E03C42ABDF2000CF736DED9FF7BF999B09B\"," +
                "    \"date\": 464678250," +
                "    \"hash\": \"235DA149DDA3BD32B886C132ABDE60CC5AD2C5693652F1E1725565E4B3D425B4\"" +
                "  }," +
                "  \"type\": \"transaction\"," +
                "  \"validated\": true" +
                "}");
    }

    @Test
    public void testTransactionNotificationMessage() {
        TransactionResult tr = TransactionResult.fromJSON(transaction_notification_message);
        assertHelper(tr, true);

        tr = new TransactionResult(transaction_notification_message,
                                   Source.transaction_subscription_notification);
        assertHelper(tr);
    }

    @Test
    public void testAccountTxResult() {
        TransactionResult tr = TransactionResult.fromJSON(account_tx_result);
        assertHelper(tr);

        tr = new TransactionResult(account_tx_result, Source.request_account_tx);
        assertHelper(tr);
    }

    @Test
    public void testRequestTxResult() {
        TransactionResult tr = TransactionResult.fromJSON(request_tx_result);
        assertHelper(tr);

        tr = new TransactionResult(request_tx_result, Source.request_tx_result);
        assertHelper(tr);
    }

    @Test
    public void testAccountTxBinaryResult() {
        TransactionResult tr = TransactionResult.fromJSON(account_tx_binary_result);
        assertHelper(tr);

        tr = new TransactionResult(account_tx_binary_result, Source.request_account_tx_binary);
        assertHelper(tr);
    }

    @Test
    public void testRequestTxBinaryResult() {
        TransactionResult tr = TransactionResult.fromJSON(request_tx_binary_result);
        assertHelper(tr);

        tr = new TransactionResult(request_tx_binary_result, Source.request_tx_binary);
        assertHelper(tr);
    }

    @Test
    public void testLedgerExpanded() {
        JSONObject result = null, tx = null;
        try {
            result = ledger_expanded.getJSONObject("result");
            JSONObject ledger = result.getJSONObject("ledger");
            tx = ledger.getJSONArray("transactions").getJSONObject(0);
            tx.put("ledger_index", ledger.getLong("ledger_index"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        TransactionResult tr = TransactionResult.fromJSON(tx);
        assertHelper(tr);

        tr = new TransactionResult(tx, Source.ledger_transactions_expanded_with_ledger_index_injected);
        assertHelper(tr);
    }

    public void assertHelper(TransactionResult tr) {
        assertHelper(tr, false);
    }

    public void assertHelper(TransactionResult tr, boolean checkLedgerHash) {
        assertEquals(true, tr.validated);
        assertEquals("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", tr.initiatingAccount().address);
        assertEquals(EngineResult.tesSUCCESS, tr.engineResult);
        assertEquals("235DA149DDA3BD32B886C132ABDE60CC5AD2C5693652F1E1725565E4B3D425B4", tr.hash.toHex());
        assertEquals(7, tr.ledgerIndex.longValue());
        assertTrue(tr.txn != null);
        assertTrue(tr.meta != null);

        Payment payment = (Payment) tr.txn;
        assertEquals("1000000000", payment.amount().toDropsString());
        TransactionMeta meta = tr.meta;
        assertEquals(0, meta.transactionIndex().longValue());

        if (checkLedgerHash) {
            assertEquals("B885C2935021E6BD64C4F7BA79B838797BC2D1AD2883DFC811B1D80D9E5E890D", tr.ledgerHash.toHex());
        }
    }

}