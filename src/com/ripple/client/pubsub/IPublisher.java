package com.ripple.client.pubsub;

public interface IPublisher<EventClass extends IPublisher.ICallback2> {
    interface ICallback2<T> {
        public void call(Object... args);
    }

    <T extends EventClass> void on(Class<T> key, T cb);
    <T extends EventClass> void once(Class<T> key, T cb);
    <T extends EventClass> int emit(Class<T> key, Object... args);
    void remove(Class<? extends EventClass> key, ICallback2 cb);
}
