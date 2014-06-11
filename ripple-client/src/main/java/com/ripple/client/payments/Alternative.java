package com.ripple.client.payments;

import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.PathSet;
import com.ripple.core.coretypes.hash.HalfSha512;
import com.ripple.core.coretypes.hash.Hash256;

public class Alternative implements Comparable<Alternative> {

    public Amount sourceAmount;
    public PathSet paths;
    public Hash256 hash;

    public Alternative(PathSet paths, Amount sourceAmount) {
        this.paths = paths;
        this.sourceAmount = sourceAmount;
        this.hash = calculateHash(paths, sourceAmount);
    }

    private Hash256 calculateHash(PathSet paths, Amount sourceAmount) {
        HalfSha512 half = new HalfSha512();
        half.update(paths.toBytes());
        half.update(sourceAmount.toBytes());
        return half.finish();
    }

    @Override
    public int compareTo(Alternative another) {
        return hash.compareTo(another.hash);
    }

    public boolean directXRP() {
        return !hasPaths() && sourceAmount.isNative();
    }

    boolean hasPaths() {
        return paths.size() > 0;
    }
}
