package com.ripple.core.coretypes;

import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.serialized.BinaryParser;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class RippleDate extends Date {
    public static long RIPPLE_EPOCH;
    static {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.set(2000, 0, 0, 0, 0, 0);
        RIPPLE_EPOCH = cal.getTimeInMillis();
    }

    public static Date fromSecondsSinceRippleEpoch(Number ms) {
        return new Date(RIPPLE_EPOCH + (ms.longValue() * 1000));
    }

    public static Date fromParser(BinaryParser parser) {
        UInt32 uInt32 = UInt32.translate.fromParser(parser);
        return fromSecondsSinceRippleEpoch(uInt32);
    }
}
