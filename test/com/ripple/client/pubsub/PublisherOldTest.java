package com.ripple.client.pubsub;

import com.ripple.client.pubsub.IPublisher;
import com.ripple.client.pubsub.Publisher;
import org.junit.Test;

import static junit.framework.TestCase.*;

//public class PublisherTest {
//    private final Class<?> = Publisher.Callback<Object>
//    private final String KEY = "event2";
//    boolean itran;
//
//    @Test
//    public void test_No_Arguments() {
//        itran = false;
//
//        IPublisher<String> pub = new Publisher<String>();
//        pub.on(KEY, new IPublisher.ICallback() {
//            @Override
//            public void call(Object... args) {
//                itran = true;
//            }
//        });
//        pub.emit(KEY);
//        assertTrue(itran);
//
//    }
//
//    @Test
//    public void test_With_Arguments() {
//        itran = false;
//
//        final Object a1 = "string";
//        final Object a2 = 2;
//
//        IPublisher<String> pub = new Publisher<String>();
//        pub.on(KEY, new IPublisher.ICallback() {
//            @Override
//            public void call(Object... args) {
//                assertEquals(2, args.length);
//                assertEquals(a1, args[0]);
//                assertEquals(a2, args[1]);
//                itran = true;
//            }
//        });
//
//        pub.emit(KEY, a1, a2);
//        assertTrue(itran);
//    }
//
//    @Test
//    public void test_remove() {
//        itran = false;
//
//        IPublisher<String> pub = new Publisher<String>();
//        IPublisher.ICallback cb = new IPublisher.ICallback() {
//            @Override
//            public void call(Object... args) {
//                itran = true;
//            }
//        };
//        pub.on(KEY, cb);
//        pub.emit(KEY);
//        assertTrue(itran);
//
//        itran = false;
//        pub.emit(KEY);
//        assertTrue(itran);
//
//        pub.remove(KEY, cb);
//        itran = false;
//        pub.emit(KEY);
//        assertFalse(itran);
//    }
//
//    @Test
//    public void test_once() {
//        itran = false;
//
//        IPublisher<String> pub = new Publisher<String>();
//        pub.once(KEY, new IPublisher.ICallback() {
//            @Override
//            public void call(Object... args) {
//                itran = true;
//            }
//        });
//
//        pub.emit(KEY);
//        assertTrue(itran);
//        itran = false;
//        pub.emit(KEY);
//        assertFalse(itran);
//    }
//}
