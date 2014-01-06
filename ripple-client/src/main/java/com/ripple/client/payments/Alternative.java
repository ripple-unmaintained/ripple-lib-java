package com.ripple.client.payments;

import com.ripple.core.types.Amount;
import com.ripple.core.types.PathSet;

public class Alternative {
    public Amount sourceAmount;
    public PathSet paths;

    public Alternative(PathSet paths, Amount sourceAmount) {
        this.paths = paths;
        this.sourceAmount = sourceAmount;
    }
}
