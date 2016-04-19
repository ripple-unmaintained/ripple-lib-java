package com.ripple.core.serialized.enums;

import com.ripple.core.fields.Type;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesSink;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import com.ripple.encodings.common.B16;

import java.util.TreeMap;

public enum EngineResult implements SerializedType
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
    temBAD_CURRENCY (-296, "Malformed: Bad currency."),
    temBAD_EXPIRATION (-295, "Malformed: Bad expiration."),
    temBAD_FEE (-294, "Invalid fee, negative or not XRP."),
    temBAD_ISSUER (-293, "Malformed: Bad issuer."),
    temBAD_LIMIT (-292, "Limits must be non-negative."),
    temBAD_OFFER (-291, "Malformed: Bad offer."),
    temBAD_PATH (-290, "Malformed: Bad path."),
    temBAD_PATH_LOOP (-289, "Malformed: Loop in path."),
    temBAD_SEND_XRP_LIMIT (-288, "Malformed: Limit quality is not allowed for XRP to XRP."),
    temBAD_SEND_XRP_MAX (-287, "Malformed: Send max is not allowed for XRP to XRP."),
    temBAD_SEND_XRP_NO_DIRECT (-286, "Malformed: No Ripple direct is not allowed for XRP to XRP."),
    temBAD_SEND_XRP_PARTIAL (-285, "Malformed: Partial payment is not allowed for XRP to XRP."),
    temBAD_SEND_XRP_PATHS (-284, "Malformed: Paths are not allowed for XRP to XRP."),
    temBAD_SEQUENCE (-283, "Malformed: Sequence is not in the past."),
    temBAD_SIGNATURE (-282, "Malformed: Bad signature."),
    temBAD_SRC_ACCOUNT (-281, "Malformed: Bad source account."),
    temBAD_TRANSFER_RATE(-280, "Malformed: Bad transfer rate"),
    temDST_IS_SRC (-279, "Destination may not be source."),
    temDST_NEEDED (-278, "Destination not specified."),
    temINVALID (-277, "The transaction is ill-formed."),
    temINVALID_FLAG (-276, "The transaction has an invalid flag."),
    temREDUNDANT (-275, "Sends same currency to self."),
    temREDUNDANT_SEND_MAX (-274, "Send max is redundant."),
    temRIPPLE_EMPTY (-273, "PathSet with no paths."),

    temUNCERTAIN (-272, "In process of determining result. Never returned."),
    temUNKNOWN (-271, "The transactions requires logic not implemented yet."),

    tefFAILURE (-199, "Failed to apply."),
    tefALREADY (-198, "The exact transaction was already in this ledger."),
    tefBAD_ADD_AUTH (-197, "Not authorized to add account."),
    tefBAD_AUTH (-196, "Transaction's public key is not authorized."),
    tefBAD_LEDGER (-195, "Ledger in unexpected state."),
    tefCREATED (-194, "Can't add an already created account."),
    tefDST_TAG_NEEDED (-193, "Destination tag required."),
    tefEXCEPTION (-192, "Unexpected program state."),
    tefINTERNAL (-191, "Internal error."),
    tefNO_AUTH_REQUIRED (-190, "Auth is not required."),
    tefPAST_SEQ (-189, "This sequence number has already past."),
    tefWRONG_PRIOR (-188, "tefWRONG_PRIOR"),
    tefMASTER_DISABLED (-187, "tefMASTER_DISABLED"),
    tefMAX_LEDGER (-186, "Ledger sequence too high."),

    terRETRY (-99, "Retry transaction."),
    terFUNDS_SPENT (-98, "Can't set password, password set funds already spent."),
    terINSUF_FEE_B (-97, "AccountID balance can't pay fee."),
    terNO_ACCOUNT (-96, "The source account does not exist."),
    terNO_AUTH (-95, "Not authorized to hold IOUs."),
    terNO_LINE (-94, "No such line."),
    terOWNERS (-93, "Non-zero owner count."),
    terPRE_SEQ (-92, "Missing/inapplicable prior transaction."),
    terLAST (-91, "Process last."),
    terNO_RIPPLE(-90, "Process last."),

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
    tecNO_ALTERNATIVE_KEY(130, "tecNO_ALTERNATIVE_KEY"),
    tecNO_REGULAR_KEY (131, "tecNO_REGULAR_KEY"),
    tecOWNERS (132, "tecOWNERS"),
    tecNO_ISSUER(133, "Issuer account does not exist."),
    tecNO_AUTH(134, "Not authorized to hold asset."),
    tecNO_LINE(135, "No such line."),
    tecINSUFF_FEE(136,  "Insufficient balance to pay fee."),
    tecFROZEN(137, "Asset is frozen."),
    tecNO_TARGET(138, "Target account does not exist."),
    tecNO_PERMISSION(139, "No permission to perform requested operation."),
    tecNO_ENTRY(140, "No matching entry found."),
    tecINSUFFICIENT_RESERVE(141, "Insufficient reserve to complete requested operation."),
    tecNEED_MASTER_KEY(142, "The operation requires the use of the Master Key."),
    tecDST_TAG_NEEDED(143, "A destination tag is required."),
    tecINTERNAL(144, "An internal error has occurred during processing."),
    tecOVERSIZE(145, "Object exceeded serialization limits.");

    public int asInteger() {
        return ord;
    }

    @Override
    public Type type() {
        return Type.UInt8;
    }

    int ord;
    public String human;
    EngineResult class_ = null;

    EngineResult(int i, String s) {
        human = s;
        ord = i;
    }

    private static TreeMap<Integer, EngineResult> byCode;

    static {
        byCode = new TreeMap<Integer, EngineResult>();
        for (EngineResult ter : EngineResult.values()) {
            byCode.put(ter.ord, ter);
        }
    }

    public static EngineResult fromNumber(Number i) {
        return byCode.get(i.intValue());
    }


    /*Serialized Type implementation*/
    @Override
    public byte[] toBytes() {
        return new byte[]{(byte) ord};
    }

    @Override
    public void toBytesSink(BytesSink to) {
        to.add((byte) ord);
    }

    @Override
    public Object toJSON() {
        return toString();
    }

    @Override
    public String toHex() {
        return B16.toString(toBytes());
    }

    public static class Translator extends TypeTranslator<EngineResult> {
        @Override
        public EngineResult fromParser(BinaryParser parser, Integer hint) {
            return fromInteger(parser.readOneInt());
        }

        @Override
        public EngineResult fromString(String value) {
            return EngineResult.valueOf(value);
        }

        @Override
        public EngineResult fromInteger(int integer) {
            return fromNumber(integer);
        }
    }

    public static Translator translate = new Translator();

    // Result Classes
    public static EngineResult resultClass(EngineResult result) {
        if (result.ord >= telLOCAL_ERROR.ord && result.ord < temMALFORMED.ord) {
            return telLOCAL_ERROR;
        }
        if (result.ord >= temMALFORMED.ord && result.ord < tefFAILURE.ord) {
            return temMALFORMED;
        }
        if (result.ord >= tefFAILURE.ord && result.ord < terRETRY.ord) {
            return tefFAILURE;
        }
        if (result.ord >= terRETRY.ord && result.ord < tesSUCCESS.ord) {
            return terRETRY;
        }
        if (result.ord >= tesSUCCESS.ord && result.ord < tecCLAIM.ord) {
            return tesSUCCESS;
        }
        return tecCLAIM;
    }
    public EngineResult resultClass() {
        return class_;
    }
    static {
        for (EngineResult engineResult : EngineResult.values()) {
            engineResult.class_ = resultClass(engineResult);
        }
    }

}


