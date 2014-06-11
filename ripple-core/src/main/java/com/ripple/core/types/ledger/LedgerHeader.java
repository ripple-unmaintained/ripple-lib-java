package com.ripple.core.types.ledger;

import com.ripple.core.binary.BinaryReader;
import com.ripple.core.coretypes.RippleDate;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.coretypes.uint.UInt64;
import com.ripple.core.coretypes.uint.UInt8;

import java.util.Date;

public class LedgerHeader {
    UInt32  version;         // Always 0x4C475200 (LWR) (Secures signed objects)
    UInt32  sequence;        // Ledger Sequence (0 for genesis ledger)
    UInt64  totalXRP;        //
    Hash256 previousLedger;  // The hash of the previous ledger (0 for genesis ledger)
    Hash256 transactionHash; // The hash of the transaction tree's root node.
    Hash256 stateHash;       // The hash of the state tree's root node.
    UInt32  parentCloseTime; // The time the previous ledger closed
    UInt32  closeTime;       // UTC minute ledger closed encoded as seconds since 1/1/2000 (or 0 for genesis ledger)
    UInt8   closeResolution; // The resolution (in seconds) of the close time
    UInt8   closeFlags;      // Flags

    Date closeDate;

    public static LedgerHeader fromParser(BinaryParser parser) {
        return fromReader(new BinaryReader(parser));
    }
    public static LedgerHeader fromReader(BinaryReader reader) {
        LedgerHeader ledger = new LedgerHeader();

        ledger.version = reader.uInt32();
        ledger.sequence = reader.uInt32();
        ledger.totalXRP = reader.uInt64();
        ledger.previousLedger = reader.hash256();
        ledger.transactionHash= reader.hash256();
        ledger.stateHash = reader.hash256();
        ledger.parentCloseTime = reader.uInt32();
        ledger.closeTime = reader.uInt32();
        ledger.closeResolution = reader.uInt8();
        ledger.closeFlags = reader.uInt8();

        ledger.closeDate = RippleDate.fromSecondsSinceRippleEpoch(ledger.closeTime);

        return ledger;
    }
}
