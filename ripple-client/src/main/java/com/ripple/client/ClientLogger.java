package com.ripple.client;

public class ClientLogger {
    public static boolean quiet;
    public static Logger logger = new Logger() {
        @Override
        public void log(String fmt, Object... args) {
            if (quiet) {
                return;
            }
            System.out.printf(fmt + "\n", args);
        }
    };

    public static void log(String fmt, Object... args) {
        logger.log(fmt, args);
    }
}
