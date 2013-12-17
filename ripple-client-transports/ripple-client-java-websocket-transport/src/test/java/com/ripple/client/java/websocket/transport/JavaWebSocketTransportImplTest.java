
package com.ripple.client.java.websocket.transport;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.ripple.client.transport.TransportEventHandler;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;

public class JavaWebSocketTransportImplTest {
    private JavaWebSocketTransportImpl webSocketTransport;

    @Before
    public void setUp() {
        webSocketTransport = new JavaWebSocketTransportImpl();
    }

    @Test
    public void should_return_not_null() {
        assertNotNull(webSocketTransport);
    }

    @Test
    public void should_return_false_when_call_is_connected() {
        assertFalse(webSocketTransport.isConnected());
    }

    @Test
    public void should_return_true_when_call_is_connected() throws URISyntaxException, InterruptedException {
        //Asynchronous unit test
        MockHandler mockHandler = new MockHandler();
        webSocketTransport.setHandler(mockHandler);
        // 1. Execute the connect
        webSocketTransport.connect(new URI("wss://s1.ripple.com"));
        // 2. Wait for the connect to complete
        synchronized (mockHandler) {
            mockHandler.wait(2000);
        }
        // 3. check the results
        assertTrue(webSocketTransport.isConnected());
    }

    class MockHandler implements TransportEventHandler {
        @Override
        public void onMessage(JSONObject msg) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onConnecting(int attempt) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onDisconnected(boolean willReconnect) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onError(Exception error) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onConnected() {
            synchronized (this) {
                webSocketTransport.setConnected(true);
                notifyAll();
            }
        }
    }

}
