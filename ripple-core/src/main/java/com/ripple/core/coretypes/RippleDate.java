package com.ripple.core.coretypes;

import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.serialized.BinaryParser;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class RippleDate extends Date {
    public static TimeZone EPOCH_TIMEZONE = TimeZone.getTimeZone("GMT");
    public static long SECONDS_SINCE_RIPPLE_EPOCH_UTC;

    static {
        GregorianCalendar cal = new GregorianCalendar(EPOCH_TIMEZONE);
        cal.set(2000, 0, 0, 0, 0, 0);
        SECONDS_SINCE_RIPPLE_EPOCH_UTC = cal.getTimeInMillis();
    }
    private RippleDate() {
        super();
    }
    private RippleDate(long milliseconds) {
        super(SECONDS_SINCE_RIPPLE_EPOCH_UTC + milliseconds);
    }

    public long secondsSinceRippleEpoch() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(this);
        return ((cal.getTimeInMillis() - SECONDS_SINCE_RIPPLE_EPOCH_UTC) / 1000);
    }
    public static RippleDate fromSecondsSinceRippleEpoch(Number seconds) {
        return new RippleDate((seconds.longValue() * 1000));
    }
    public static RippleDate fromParser(BinaryParser parser) {
        UInt32 uInt32 = UInt32.translate.fromParser(parser);
        return fromSecondsSinceRippleEpoch(uInt32);
    }
    public static RippleDate now() {
        return new RippleDate();
    }
}
