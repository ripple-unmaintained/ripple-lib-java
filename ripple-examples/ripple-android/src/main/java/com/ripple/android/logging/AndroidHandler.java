package com.ripple.android.logging;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.logging.*;

/**
 * Implements a {@link java.util.logging.Logger} handler that writes to the Android log. The
 * implementation is rather straightforward. The name of the logger serves as
 * the log tag. Only the log levels need to be converted appropriately. For
 * this purpose, the following mapping is being used:
 * <p/>
 * <table>
 * <tr>
 * <th>logger level</th>
 * <th>Android level</th>
 * </tr>
 * <tr>
 * <td>
 * SEVERE
 * </td>
 * <td>
 * ERROR
 * </td>
 * </tr>
 * <tr>
 * <td>
 * WARNING
 * </td>
 * <td>
 * WARN
 * </td>
 * </tr>
 * <tr>
 * <td>
 * INFO
 * </td>
 * <td>
 * INFO
 * </td>
 * </tr>
 * <tr>
 * <td>
 * CONFIG
 * </td>
 * <td>
 * DEBUG
 * </td>
 * </tr>
 * <tr>
 * <td>
 * FINE, FINER, FINEST
 * </td>
 * <td>
 * VERBOSE
 * </td>
 * </tr>
 * </table>
 */
public class AndroidHandler extends Handler {
    /**
     * Holds the formatter for all Android log handlers.
     */
    private static final Formatter THE_FORMATTER = new Formatter() {
        @Override
        public String format(LogRecord r) {
            Throwable thrown = r.getThrown();
            if (thrown != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                sw.write(getFormattedMessage(r));
                sw.write("\n");
                thrown.printStackTrace(pw);
                pw.flush();
                return sw.toString();
            } else {
                return getFormattedMessage(r);

            }
        }

        private String getFormattedMessage(LogRecord r) {
            return (MessageFormat.format(r.getMessage(), r.getParameters()));
        }
    };

    /**
     * Constructs a new instance of the Android log handler.
     */
    public AndroidHandler() {
        setFormatter(THE_FORMATTER);
    }


    public static String loggerNameToTag(String loggerName) {
        // Anonymous logger.
        if (loggerName == null) {
            return "null";
        }
        int length = loggerName.length();
        if (length <= 23) {
            return loggerName;
        }
        int lastPeriod = loggerName.lastIndexOf(".");
        return length - (lastPeriod + 1) <= 23
                ? loggerName.substring(lastPeriod + 1)
                : loggerName.substring(loggerName.length() - 23);
    }

    @Override
    public void close() {
        // No need to close, but must implement abstract method.
    }

    @Override
    public void flush() {
        // No need to flush, but must implement abstract method.
    }

    @Override
    public void publish(LogRecord record) {
        int level = getAndroidLevel(record.getLevel());
        String tag = loggerNameToTag(record.getLoggerName());
//        if (!Log.isLoggable(tag, level)) {
//            return;
//        }

        try {
            String message = getFormatter().format(record);
            Log.println(level, tag, message);
        } catch (RuntimeException e) {
            Log.e("AndroidHandler", "Error logging message.", e);
        }
    }

    public void publish(Logger source, String tag, Level level, String message) {
        // TODO: avoid ducking into native 2x; we aren't saving any formatter calls                u
        int priority = getAndroidLevel(level);
//        if (!Log.isLoggable(tag, priority)) {
//            return;
//        }

        try {
            Log.println(priority, tag, message);
        } catch (RuntimeException e) {
            Log.e("AndroidHandler", "Error logging message.", e);
        }
    }

    /**
     * Converts a {@link java.util.logging.Logger} logging level into an Android one.
     *
     * @param level The {@link java.util.logging.Logger} logging level.
     * @return The resulting Android logging level.
     */
    static int getAndroidLevel(Level level) {
        int value = level.intValue();
        if (value >= 1000) { // SEVERE
            return Log.ERROR;
        } else if (value >= 900) { // WARNING
            return Log.WARN;
        } else if (value >= 800) { // INFO
            return Log.INFO;
        } else {
            return Log.DEBUG;
        }
    }
}