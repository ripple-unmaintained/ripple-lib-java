package com.ripple.client.pubsub;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

public class Publisher<EventClass extends IPublisher.ICallback2> implements IPublisher<EventClass> {
    public abstract static class Callback<Result> implements ICallback2 {
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
        add(key, new ICallback2() {
            
            public void call(Object... args) {
                remove(key, this);
                cb.call(args);
            }
        });
    }

    @Override
    public <T extends EventClass> int emit(Class<T> key, Object... args) {
        // Make a copy of the array at the time of emission
        // toArray will notice length then allocate a new ICallback2[]
        ICallback2[] callbacks = new ICallback2[0];
        callbacks = listFor(key).toArray(callbacks);
        for (ICallback2 callback : callbacks) {
            callback.call(args);
        }
        return callbacks.length;
    }

    private final HashMap<Class<? extends EventClass>, ArrayList<ICallback2>> cbs;
    {
        cbs = new HashMap<Class<? extends EventClass>, ArrayList<ICallback2>>();
    }

    private ArrayList<ICallback2> listFor(Class<? extends EventClass> key) {
        if (cbs.get(key) == null) {
            cbs.put(key, new ArrayList<ICallback2>());
        }
        return cbs.get(key);
    }
    private <T extends EventClass> void add(Class<T> key, ICallback2 cb) {listFor(key).add(cb); }
    
    @Override
    public void remove(Class<? extends EventClass> key, ICallback2 cb) {listFor(key).remove(cb);}
}
