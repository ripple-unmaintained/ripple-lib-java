package com.ripple.client.pubsub;

import com.ripple.client.ClientLogger;

import java.util.ArrayList;
import java.util.HashMap;

public class Publisher<EventClass extends IPublisher.ICallback> implements IPublisher<EventClass> {

    public abstract static class Callback<Result> implements ICallback {
        abstract public void called(Result result);

        @Override
        @SuppressWarnings("unchecked")
        public void call(Object... args) {
            called((Result) args[0]);
        }
    }

    @Override
    public <T extends EventClass> void on(Class<T> key, T cb) {
        add(key, cb);
    }
    
    @Override
    public <T extends EventClass> void once(final Class<T> key, final T cb) {
        add(key, new ICallback() {
            
            public void call(Object... args) {
                removeListener(key, this);
                cb.call(args);
            }
        });
    }

    @Override
    public <T extends EventClass> int emit(Class<T> key, Object... args) {
        ClientLogger.log("Emitting %s from thread: %s", key.getSimpleName(), Thread.currentThread());
        // Make a copy of the array at the time of emission
        // toArray will notice length then allocate a new ICallback[]
        ICallback[] callbacks = new ICallback[0];
        callbacks = listFor(key).toArray(callbacks);
        for (ICallback callback : callbacks) {
            callback.call(args);
        }
        return callbacks.length;
    }

    private final HashMap<Class<? extends EventClass>, ArrayList<ICallback>> cbs;
    {
        cbs = new HashMap<Class<? extends EventClass>, ArrayList<ICallback>>();
    }

    private ArrayList<ICallback> listFor(Class<? extends EventClass> key) {
        if (cbs.get(key) == null) {
            cbs.put(key, new ArrayList<ICallback>());
        }
        return cbs.get(key);
    }
    private <T extends EventClass> void add(Class<T> key, ICallback cb) {listFor(key).add(cb); }
    
    @Override
    public void removeListener(Class<? extends EventClass> key, ICallback cb) {listFor(key).remove(cb);}
}
