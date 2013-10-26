package com.ripple.client.subscriptions;

import com.ripple.core.types.AccountID;
import org.json.JSONObject;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: nick
 * Date: 10/16/13
 * Time: 1:43 PM
 */
public class SubscriptionManagerTest {
    public static AccountID bob_account = AccountID.fromSeedString("shn6zJ8zzkaoFSfsEWvJLZf3V344C");

    @Test
    public void testConnectionSubscription() throws Exception {
        SubscriptionManager sm = new SubscriptionManager();
        sm.addStream(SubscriptionManager.Stream.ledger);
        sm.addStream(SubscriptionManager.Stream.server);
        sm.addAccount(bob_account);
        JSONObject allSubscribed = sm.allSubscribed();

        assertEquals("{\"accounts\":[\"rQfFsw6w4wdymTCSfF2fZQv7SZzfGyzsyB\"],\"streams\":[\"server\",\"ledger\"]}",
                allSubscribed.toString());
    }
}
