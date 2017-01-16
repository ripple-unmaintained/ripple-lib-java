package com.ripple.core.serialized.enums;

import com.ripple.core.fields.Type;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesSink;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import com.ripple.encodings.common.B16;

import java.util.TreeMap;

class Holder {
    public static int ords = 0;
}

public enum EngineResult implements SerializedType {
    telLOCAL_ERROR(-399, "Local failure."),
    telBAD_DOMAIN("Domain too long."),
    telBAD_PATH_COUNT("Malformed: Too many paths."),
    telBAD_PUBLIC_KEY("Public key too long."),
    telFAILED_PROCESSING("Failed to correctly process transaction."),
    telINSUF_FEE_P("Fee insufficient."),
    telNO_DST_PARTIAL("Partial payment to create account not allowed."),
    telCAN_NOT_QUEUE("Can not queue at this time."),

    temMALFORMED(-299, "Malformed transaction."),
    temBAD_AMOUNT("Can only send positive amounts."),
    temBAD_CURRENCY("Malformed: Bad currency."),
    temBAD_EXPIRATION("Malformed: Bad expiration."),
    temBAD_FEE("Invalid fee, negative or not XRP."),
    temBAD_ISSUER("Malformed: Bad issuer."),
    temBAD_LIMIT("Limits must be non-negative."),
    temBAD_OFFER("Malformed: Bad offer."),
    temBAD_PATH("Malformed: Bad path."),
    temBAD_PATH_LOOP("Malformed: Loop in path."),
    temBAD_SEND_XRP_LIMIT("Malformed: Limit quality is not allowed for XRP to XRP."),
    temBAD_SEND_XRP_MAX("Malformed: Send max is not allowed for XRP to XRP."),
    temBAD_SEND_XRP_NO_DIRECT("Malformed: No Ripple direct is not allowed for XRP to XRP."),
    temBAD_SEND_XRP_PARTIAL("Malformed: Partial payment is not allowed for XRP to XRP."),
    temBAD_SEND_XRP_PATHS("Malformed: Paths are not allowed for XRP to XRP."),
    temBAD_SEQUENCE("Malformed: Sequence is not in the past."),
    temBAD_SIGNATURE("Malformed: Bad signature."),
    temBAD_SRC_ACCOUNT("Malformed: Bad source account."),
    temBAD_TRANSFER_RATE("Malformed: Transfer rate must be >= 1.0"),
    temDST_IS_SRC("Destination may not be source."),
    temDST_NEEDED("Destination not specified."),
    temINVALID("The transaction is ill-formed."),
    temINVALID_FLAG("The transaction has an invalid flag."),
    temREDUNDANT("Sends same currency to self."),
    temRIPPLE_EMPTY("PathSet with no paths."),
    temDISABLED("The transaction requires logic that is currently disabled."),
    temBAD_SIGNER("Malformed: No signer may duplicate account or other signers."),
    temBAD_QUORUM("Malformed: Quorum is unreachable."),
    temBAD_WEIGHT("Malformed: Weight must be a positive value."),
    temBAD_TICK_SIZE("Malformed: Tick size out of range."),
    temUNCERTAIN("In process of determining result. Never returned."),
    temUNKNOWN("The transaction requires logic that is not implemented yet."),

    tefFAILURE(-199, "Failed to apply."),
    tefALREADY("The exact transaction was already in this ledger."),
    tefBAD_ADD_AUTH("Not authorized to add account."),
    tefBAD_AUTH("Transaction's public key is not authorized."),
    tefBAD_LEDGER("Ledger in unexpected state."),
    tefCREATED("Can't add an already created account."),
    tefEXCEPTION("Unexpected program state."),
    tefINTERNAL("Internal error."),
    tefNO_AUTH_REQUIRED("Auth is not required."),
    tefPAST_SEQ("This sequence number has already past."),
    tefWRONG_PRIOR("This previous transaction does not match."),
    tefMASTER_DISABLED("Master key is disabled."),
    tefMAX_LEDGER("Ledger sequence too high."),
    tefBAD_SIGNATURE("A signature is provided for a non-signer."),
    tefBAD_QUORUM("Signatures provided do not meet the quorum."),
    tefNOT_MULTI_SIGNING("Account has no appropriate list of multi-signers."),
    tefBAD_AUTH_MASTER("Auth for unclaimed account needs correct master key."),
    terRETRY(-99, "Retry transaction."),
    terFUNDS_SPENT("Can't set password, password set funds already spent."),
    terINSUF_FEE_B("Account balance can't pay fee."),
    terNO_ACCOUNT("The source account does not exist."),
    terNO_AUTH("Not authorized to hold IOUs."),
    terNO_LINE("No such line."),
    terOWNERS("Non-zero owner count."),
    terPRE_SEQ("Missing/inapplicable prior transaction."),
    terLAST("Process last."),
    terNO_RIPPLE("Path does not permit rippling."),
    terQUEUED("Held until escalated fee drops."),

    tesSUCCESS(0, "The transaction was applied. Only final in a validated ledger."),

    tecCLAIM(100, "Fee claimed. Sequence used. No action."),
    tecPATH_PARTIAL(101, "Path could not send full amount."),
    tecUNFUNDED_ADD(102, "Insufficient XRP balance for WalletAdd."),
    tecUNFUNDED_OFFER(103, "Insufficient balance to fund created offer."),
    tecUNFUNDED_PAYMENT(104, "Insufficient XRP balance to send."),
    tecFAILED_PROCESSING(105, "Failed to correctly process transaction."),
    tecDIR_FULL(121, "Can not add entry to full directory."),
    tecINSUF_RESERVE_LINE(122, "Insufficient reserve to add trust line."),
    tecINSUF_RESERVE_OFFER(123, "Insufficient reserve to create offer."),
    tecNO_DST(124, "Destination does not exist. Send XRP to create it."),
    tecNO_DST_INSUF_XRP(125, "Destination does not exist. Too little XRP sent to create it."),
    tecNO_LINE_INSUF_RESERVE(126, "No such line. Too little reserve to create it."),
    tecNO_LINE_REDUNDANT(127, "Can't set non-existent line to default."),
    tecPATH_DRY(128, "Path could not send partial amount."),
    tecUNFUNDED(129, "One of _ADD, _OFFER, or _SEND. Deprecated."),
    tecNO_ALTERNATIVE_KEY(130, "The operation would remove the ability to sign transactions with the account."),
    tecNO_REGULAR_KEY(131, "Regular key is not set."),
    tecOWNERS(132, "Non-zero owner count."),
    tecNO_ISSUER(133, "Issuer account does not exist."),
    tecNO_AUTH(134, "Not authorized to hold asset."),
    tecNO_LINE(135, "No such line."),
    tecINSUFF_FEE(136, "Insufficient balance to pay fee."),
    tecFROZEN(137, "Asset is frozen."),
    tecNO_TARGET(138, "Target account does not exist."),
    tecNO_PERMISSION(139, "No permission to perform requested operation."),
    tecNO_ENTRY(140, "No matching entry found."),
    tecINSUFFICIENT_RESERVE(141, "Insufficient reserve to complete requested operation."),
    tecNEED_MASTER_KEY(142, "The operation requires the use of the Master Key."),
    tecDST_TAG_NEEDED(143, "A destination tag is required."),
    tecINTERNAL(144, "An internal error has occurred during processing."),
    tecOVERSIZE(145, "Object exceeded serialization limits."),
    tecCRYPTOCONDITION_ERROR(146, "Malformed, invalid, or mismatched " +
            "conditional or fulfillment.");

    public int asInteger() {
        return ord;
    }

    @Override
    public Type type() {
        return Type.UInt8;
    }

    private static int ords = 0;
    int ord;
    public String human;
    EngineResult class_ = null;

    EngineResult(Integer i, String s) {
        human = s;
        if (i == null) {
            i = ++Holder.ords;
        } else {
            Holder.ords = i;
        }
        ord = i;
    }

    EngineResult(String s) {
        this(null, s);
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


