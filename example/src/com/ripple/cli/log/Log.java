package com.ripple.cli.log;

public class Log {
    public static void LOG(String fmt, Object... args) {
        System.out.printf(fmt + "\n", args);
    }
}
