package com.ripple.crypto.ecdsa;

import com.ripple.config.Config;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.STObject;
import com.ripple.core.types.known.tx.Transaction;
import com.ripple.encodings.common.B16;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EDKeyPairTest {
    static {
        Config.initBouncy();
    }

    String fixturesJson = "{ " +
            "  \"tx_json\": {" +
            "    \"Account\": \"rJZdUusLDtY9NEsGea7ijqhVrXv98rYBYN\"," +
            "    \"Amount\": \"1000\"," +
            "    \"Destination\": \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\"," +
            "    \"Fee\": \"10\"," +
            "    \"Flags\": 2147483648," +
            "    \"Sequence\": 1," +
            "    \"SigningPubKey\": \"EDD3993CDC6647896C455F136648B7750723B011475547AF60691AA3D7438E021D\"," +
            "    \"TransactionType\": \"Payment\"" +
            "  }," +
            "  \"expected_sig\": \"C3646313B08EED6AF4392261A31B961F10C66CB733DB7F6CD9EAB079857834C8B0334270A2C037E63CDCCC1932E0832882B7B7066ECD2FAEDEB4A83DF8AE6303\"" +
            "}";

    EDKeyPair edKeyPair;
    JSONObject fixtures = new JSONObject(fixturesJson);
    private final String expectedSig = fixtures.getString("expected_sig");
    Transaction tx = (Transaction) STObject.fromJSONObject(fixtures.getJSONObject("tx_json"));
    byte[] message = tx.signingData();

    {
        byte[] seedBytes = Seed.passPhraseToSeedBytes("niq");
        edKeyPair = EDKeyPair.from128Seed(seedBytes);
    }

    @Test
    public void testAccountIDGeneration() {
        assertEquals("rJZdUusLDtY9NEsGea7ijqhVrXv98rYBYN",
                     AccountID.fromKeyPair(edKeyPair).toString());
    }

    @Test
    public void testSigning() {
        byte[] bytes = edKeyPair.signMessage(message);
        assertEquals(fixtures.getString("expected_sig"),
                B16.toString(bytes));
    }

    @Test
    public void testVerifying() {
        assertTrue(edKeyPair.verifySignature(message, B16.decode(expectedSig)));
    }
}