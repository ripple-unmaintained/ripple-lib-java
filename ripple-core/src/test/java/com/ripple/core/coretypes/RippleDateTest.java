package com.ripple.core.coretypes;

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class RippleDateTest extends TestCase {
    public void testDateParsing() throws Exception {
        GregorianCalendar cal = new GregorianCalendar();
        Date d = RippleDate.fromSecondsSinceRippleEpoch(442939950);
        cal.setTime(d);
        assertEquals(2014, cal.get(Calendar.YEAR));
        assertEquals(0, cal.get(Calendar.MONTH));
        // We can't get any more precise unless we set a time zone ;)
    }
}
