package com.ripple.client.transactions;

import com.ripple.client.requests.Request;
import com.ripple.core.types.Amount;
import com.ripple.core.types.hash.Hash256;
import com.ripple.core.types.uint.UInt32;

public class Submission {
    public Request request;
    public UInt32 sequence;
    public Hash256 hash;
    public Amount fee;
    public long ledgerSequence;

    public Submission(Request request, UInt32 sequence, Hash256 hash, long ledgerSequence, Amount fee) {
        this.request = request;
        this.sequence = sequence;
        this.hash = hash;
        this.ledgerSequence = ledgerSequence;
        this.fee = fee;
    }
}
