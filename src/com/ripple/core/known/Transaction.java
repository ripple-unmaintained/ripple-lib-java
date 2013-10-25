package com.ripple.core.known;

import com.ripple.core.fields.Field;
import com.ripple.core.formats.TxFormat;

/**
 * Created with IntelliJ IDEA.
 * User: nick
 * Date: 9/25/13
 * Time: 3:24 PM
 */
public class Transaction extends AbstractKnown {
    Field f = Field.TransactionType;
    TxFormat txFormat = TxFormat.Payment;

    protected Transaction() {
        super();
    }
}
