
package com.ripple.client;

import com.ripple.client.transactions.ManagedTxn;
import com.ripple.core.coretypes.hash.Index;
import com.ripple.core.fields.Field;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.STObject;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.tx.txns.Payment;
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.crypto.ecdsa.Seed;
import org.json.JSONObject;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class TransactionTest {

    @Test
    public void testCreatePaymentTransaction() throws Exception {
        final String niqwit1Seed = "snSq7dKr5v39hJ8Enb45RpXFJL25h";
        IKeyPair kp = Seed.getKeyPair(niqwit1Seed);
        ManagedTxn transaction = new ManagedTxn(new Payment());
        transaction.prepare(kp, Amount.fromString("15"), new UInt32(1), null );
    }
}
