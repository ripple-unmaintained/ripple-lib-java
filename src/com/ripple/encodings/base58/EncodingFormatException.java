package com.ripple.encodings.base58;

/**
 * Created with IntelliJ IDEA.
 * User: nick
 * Date: 8/3/13
 * Time: 3:29 PM
 */
public class EncodingFormatException extends RuntimeException{
    public EncodingFormatException(String message) {
        super(message);
    }
}
