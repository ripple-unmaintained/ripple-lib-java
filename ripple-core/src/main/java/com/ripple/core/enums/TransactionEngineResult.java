package com.ripple.core.enums;

import java.util.TreeMap;

public enum TransactionEngineResult
{
    telLOCAL_ERROR(-399, "Local failure."),
    telBAD_DOMAIN (-398, "Domain too long."),
    telBAD_PATH_COUNT (-397, "Malformed: Too many paths."),
    telBAD_PUBLIC_KEY (-396, "Public key too long."),
    telFAILED_PROCESSING (-395, "Failed to correctly process transaction."),
    telINSUF_FEE_P (-394, "Fee insufficient."),
    telNO_DST_PARTIAL (-393, "Partial payment to create account not allowed."),
    temMALFORMED (-299, "Malformed transaction."),
    temBAD_AMOUNT (-298, "Can only send positive amounts."),
    temBAD_AUTH_MASTER (-297, "Auth for unclaimed account needs correct master key."),
    temBAD_CURRENCY (-296, "Malformed: Bad currencyString."),
    temBAD_FEE (-295, "Invalid fee, negative or not XRP."),
    temBAD_EXPIRATION (-294, "Malformed: Bad expiration."),
    temBAD_ISSUER (-293, "Malformed: Bad issuer."),
    temBAD_LIMIT (-292, "Limits must be non-negative."),
    temBAD_OFFER (-291, "Malformed: Bad offer."),
    temBAD_PATH (-290, "Malformed: Bad path."),
    temBAD_PATH_LOOP (-289, "Malformed: Loop in path."),
    temBAD_PUBLISH (-288, "Malformed: Bad publish."),
    temBAD_TRANSFER_RATE (-287, "Malformed: Transfer rate must be >= 1.0"),
    temBAD_SEND_XRP_LIMIT (-286, "Malformed: Limit quality is not allowed for XRP to XRP."),
    temBAD_SEND_XRP_MAX (-285, "Malformed: Send max is not allowed for XRP to XRP."),
    temBAD_SEND_XRP_NO_DIRECT (-284, "Malformed: No Ripple direct is not allowed for XRP to XRP."),
    temBAD_SEND_XRP_PARTIAL (-283, "Malformed: Partial payment is not allowed for XRP to XRP."),
    temBAD_SEND_XRP_PATHS (-282, "Malformed: Paths are not allowed for XRP to XRP."),
    temBAD_SIGNATURE (-281, "Malformed: Bad signature."),
    temBAD_SRC_ACCOUNT (-280, "Malformed: Bad source account."),
    temBAD_SEQUENCE (-279, "Malformed: Sequence is not in the past."),
    temDST_IS_SRC (-278, "Destination may not be source."),
    temDST_NEEDED (-277, "Destination not specified."),
    temINVALID (-276, "The transaction is ill-formed."),
    temINVALID_FLAG (-275, "The transaction has an invalid flag."),
    temREDUNDANT (-274, "Sends same currencyString to self."),
    temREDUNDANT_SEND_MAX (-273, "Send max is redundant."),
    temRIPPLE_EMPTY (-272, "PathSet with no paths."),
    temUNCERTAIN (-271, "In process of determining result. Never returned."),
    temUNKNOWN (-270, "The transactions requires logic not implemented yet."),
    tefFAILURE (-199, "Failed to apply."),
    tefALREADY (-198, "The exact transaction was already in this ledger."),
    tefBAD_ADD_AUTH (-197, "Not authorized to add account."),
    tefBAD_AUTH (-196, "Transaction's public key is not authorized."),
    tefBAD_CLAIM_ID (-195, "Malformed: Bad claim id."),
    tefBAD_GEN_AUTH (-194, "Not authorized to claim generator."),
    tefBAD_LEDGER (-193, "Ledger in unexpected state."),
    tefCLAIMED (-192, "Can not claim a previously claimed account."),
    tefCREATED (-191, "Can't add an already created account."),
    tefDST_TAG_NEEDED (-190, "Destination tag required."),
    tefEXCEPTION (-189, "Unexpected program state."),
    tefGEN_IN_USE (-188, "Generator already in use."),
    tefINTERNAL (-187, "Internal error."),
    tefNO_AUTH_REQUIRED (-186, "Auth is not required."),
    tefPAST_SEQ (-185, "This sequence number has already past."),
    tefWRONG_PRIOR (-184, "tefWRONG_PRIOR"),
    tefMASTER_DISABLED (-183, "tefMASTER_DISABLED"),
    terRETRY (-99, "Retry transaction."),
    terFUNDS_SPENT (-98, "Can't set password, password set funds already spent."),
    terINSUF_FEE_B (-97, "AccountID balance can't pay fee."),
    terNO_ACCOUNT (-96, "The source account does not exist."),
    terNO_AUTH (-95, "Not authorized to hold IOUs."),
    terNO_LINE (-94, "No such line."),
    terOWNERS (-93, "Non-zero owner count."),
    terPRE_SEQ (-92, "Missing/inapplicable prior transaction."),
    terLAST (-91, "Process last."),
    tesSUCCESS (0, "The transaction was applied."),
    tecCLAIM (100, "Fee claimed. Sequence used. No action."),
    tecPATH_PARTIAL (101, "Path could not send full amount."),
    tecUNFUNDED_ADD (102, "Insufficient XRP balance for WalletAdd."),
    tecUNFUNDED_OFFER (103, "Insufficient balance to fund created offer."),
    tecUNFUNDED_PAYMENT (104, "Insufficient XRP balance to send."),
    tecFAILED_PROCESSING (105, "Failed to correctly process transaction."),
    tecDIR_FULL (121, "Can not add entry to full directory."),
    tecINSUF_RESERVE_LINE (122, "Insufficient reserve to add trust line."),
    tecINSUF_RESERVE_OFFER (123, "Insufficient reserve to create offer."),
    tecNO_DST (124, "Destination does not exist. Send XRP to create it."),
    tecNO_DST_INSUF_XRP (125, "Destination does not exist. Too little XRP sent to create it."),
    tecNO_LINE_INSUF_RESERVE (126, "No such line. Too little reserve to create it."),
    tecNO_LINE_REDUNDANT (127, "Can't set non-existant line to default."),
    tecPATH_DRY (128, "Path could not send partial amount."),
    tecUNFUNDED (129, "One of _ADD, _OFFER, or _SEND. Deprecated."),
    tecMASTER_DISABLED (130, "tecMASTER_DISABLED"),
    tecNO_REGULAR_KEY (131, "tecNO_REGULAR_KEY"),
    tecOWNERS (132, "tecOWNERS");

    public int asInteger() {
        return ord;
    }

    int ord;
    String human;

    TransactionEngineResult(int i, String s) {
        human = s;
        ord = i;
    }

    private static TreeMap<Integer, TransactionEngineResult> byCode;

    static {
        byCode = new TreeMap<Integer, TransactionEngineResult>();
        for (TransactionEngineResult ter : TransactionEngineResult.values()) {
            byCode.put(ter.ord, ter);
        }
    }

    public static TransactionEngineResult fromNumber(Number i) {
        return byCode.get(i.intValue());
    }

    public Class resultClass() {
        return Class.forResult(this);
    }

    public static enum Class {
        telLOCAL_ERROR(-399),
        temMALFORMED(-299),
        tefFAILURE(-199),
        terRETRY(-99),
        tesSUCCESS(0),
        tecCLAIMED(100);

        int starts;

        Class(int i) {
            starts = i;
        }

        public static Class forResult(TransactionEngineResult result) {
            if (result.ord >= telLOCAL_ERROR.starts && result.ord < temMALFORMED.starts) {
                return telLOCAL_ERROR;
            }
            if (result.ord >= temMALFORMED.starts && result.ord < tefFAILURE.starts) {
                return temMALFORMED;
            }
            if (result.ord >= tefFAILURE.starts && result.ord < terRETRY.starts) {
                return tefFAILURE;
            }
            if (result.ord >= terRETRY.starts && result.ord < tesSUCCESS.starts) {
                return terRETRY;
            }
            if (result.ord >= tesSUCCESS.starts && result.ord < tecCLAIMED.starts) {
                return tesSUCCESS;
            }
            return tecCLAIMED;
        }

    }
}


