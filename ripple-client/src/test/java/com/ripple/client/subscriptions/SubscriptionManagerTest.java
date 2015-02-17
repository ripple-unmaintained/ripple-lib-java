package com.ripple.client.subscriptions;

import com.ripple.core.coretypes.AccountID;
import org.json.JSONObject;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class SubscriptionManagerTest {
    public static AccountID bob_account = AccountID.fromSeedString("shn6zJ8zzkaoFSfsEWvJLZf3V344C");

    @Test
    public void testConnectionSubscription() throws Exception {
        SubscriptionManager sm = new SubscriptionManager();
        sm.addStream(SubscriptionManager.Stream.ledger);
        sm.addStream(SubscriptionManager.Stream.server);
        sm.addAccount(bob_account);
        JSONObject allSubscribed = sm.allSubscribed();

        String expected = "{\"accounts\":" +
                "[\"rQfFsw6w4wdymTCSfF2fZQv7SZzfGyzsyB\"]," +
                "\"streams\":[\"server\",\"ledger\"]}";

        // We need to do this so that the ordering of elements is the same ;)
        String normedExpected = new JSONObject(expected).toString();
        assertEquals(normedExpected, allSubscribed.toString());
    }
}
