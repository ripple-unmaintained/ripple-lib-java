package com.ripple.core.coretypes;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class RippleDateTest extends TestCase {
    @Test
    public void testDateParsing() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        int seconds = 442939950;
        RippleDate d = RippleDate.fromSecondsSinceRippleEpoch(seconds);
        cal.setTime(d);

        // 13/2/2014
        assertEquals(13,   cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0,    cal.get(Calendar.MONTH));
        assertEquals(2014, cal.get(Calendar.YEAR));

        // 14:52:30
        assertEquals(14,   cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(52,   cal.get(Calendar.MINUTE));
        assertEquals(30,   cal.get(Calendar.SECOND));

        assertEquals(seconds, d.secondsSinceRippleEpoch());
    }
}
