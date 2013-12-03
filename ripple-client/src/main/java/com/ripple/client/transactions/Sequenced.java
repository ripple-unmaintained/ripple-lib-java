package com.ripple.client.transactions;

import com.ripple.core.types.uint.UInt32;

public interface Sequenced {
    UInt32 sequence();
}
