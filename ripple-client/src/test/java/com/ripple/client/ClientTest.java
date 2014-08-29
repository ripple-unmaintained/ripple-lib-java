package com.ripple.client;

import com.ripple.client.enums.Command;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.subscriptions.ServerInfo;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static junit.framework.TestCase.*;

public class ClientTest {
    @Test
    public void testMockPairSubscription() throws JSONException {
        MockPair pair              = new MockPair().connect();
        MockPair.RippledMock mock  = pair.server;
        Client client              = pair.client;

        // We can pop the latest JSON message received from the client then
        // actually get the Request object (to inspect and set event handlers etc)
        final Request subscribeRequest = mock.popMessage().getRequest(client);
        assertEquals("We expect the first message sent by the client to be a subscribe request",
                     Command.subscribe,
                     subscribeRequest.cmd);

        // There's a buffer for read/unread messages sent from client
        assertEquals("There should be no unread messages", 0, mock.messages.size());
        assertEquals("There should be one archived",       1, mock.archived.size());

        int ledger_time = 434537640;
        final String random_seed = "064486ACF5FEE8A9FD1A9026E0D9D763DDFC51FC2220E13E6E392F81B0CA4D25";

        // We build a result to send
        JSONObject subscribeResult = JSONBuilder.build().

                fee_base(10).
                fee_ref(10).
                ledger_hash("95B5F9CA0AEAD59CBBA5D14F0F248472255F241DEF3CF881CECA3CA4B01FC178").
                ledger_index(2642686).
                ledger_time(ledger_time).
                load_base(256).
                load_factor(256).
                random(random_seed).
                reserve_base(50000000).
                reserve_inc(12500000).
                server_status("full").
                validated_ledgers("32570-2642686").

                finish();

        // A ServerInfo object is where we store all the fields above
        ServerInfo info = client.serverInfo;
        assertEquals("Right now we don't know", 0, info.fee_base);
        assertFalse(info.updated);


        final boolean[] successRan = new boolean[]{false};
        subscribeRequest.on(Request.OnSuccess.class, new Request.OnSuccess() {
            public void called(Response response) {
                successRan[0] = true; // yee old python 2.x trick
                assertEquals(random_seed, response.result.optString("random"));
            }
        });

        // Then we can actually respond to that Request object with a message result
        // The `id` is retrieved from the Request object
        assertNull(subscribeRequest.response);
        mock.respondSuccess(subscribeRequest, subscribeResult);

        assertTrue(successRan[0]);

        // Once responded the response object will hung off the Request
        assertNotNull(subscribeRequest.response);
        Response response = subscribeRequest.response;

        // You can retrieve the message and also the result
        JSONObject rsp    = response.message,
                   result = response.result;

        // The response id should match up, and result fields match those as passed
        // to respondSuccess
        assertEquals(subscribeRequest.id, rsp.optInt("id", -1));
        assertEquals(random_seed,         result.getString("random"));
        assertEquals(ledger_time,         result.getInt("ledger_time"));

        // We should now actually have some fields available on the client
        // We should be able to compute a fee
        assertEquals(true, info.updated);
        assertEquals(10,   info.fee_base);
        assertEquals(10,   info.fee_ref);
        assertEquals(256,  info.load_base);
        assertEquals(256,  info.load_factor);
    }
}
