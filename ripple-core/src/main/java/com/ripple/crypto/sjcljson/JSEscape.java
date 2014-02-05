package com.ripple.crypto.sjcljson;

public class JSEscape {
    public static String unescape(String escaped) {
        int length = escaped.length();
        int i = 0;
        StringBuilder sb = new StringBuilder(escaped.length() / 2);

        while (i < length) {
            char n = escaped.charAt(i++);
            if (n != '%') {
                sb.append(n);
            } else {
                n = escaped.charAt(i++);
                int code;

                if (n == 'u') {
                    String slice = escaped.substring(i, i + 4);
                    code = Integer.valueOf(slice, 16);
                    i+=4;
                }  else {
                    String slice = escaped.substring(i-1, ++i);
                    code = Integer.valueOf(slice, 16);
                }
                sb.append((char) code);
            }
        }

        return sb.toString();
    }

    public static String escape(String raw) {
        int length = raw.length();
        int i = 0;
        StringBuilder sb = new StringBuilder(raw.length() / 2);

        while (i < length) {
            char c = raw.charAt(i++);

            if (isLetterOrDigit(c) || isEscapeExempt(c)) {
                sb.append(c);
            } else {
                int i1 = raw.codePointAt(i-1);
                String escape = Integer.toHexString(i1);

                sb.append('%');

                if (escape.length() > 2) {
                    sb.append('u');
                }
                sb.append(escape.toUpperCase());

            }
        }

        return sb.toString();
    }

    private static boolean isLetterOrDigit(char ch) {
        return (ch >= 'a' && ch <= 'z') ||
               (ch >= 'A' && ch <= 'Z') ||
               (ch >= '0' && ch <= '9');
    }

    private static boolean isEscapeExempt(char c) {
        switch (c) {
            case '*':
            case  '@':
            case '-':
            case '_':
            case '+':
            case '.':
            case '/':
                return true;
            default:
                return false;
        }
    }
}
