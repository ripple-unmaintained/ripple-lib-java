package com.ripple.client.pubsub;

import com.ripple.client.ClientLogger;
import com.ripple.client.executors.ClientExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Publisher<EventClass extends Publisher.ICallback> {
    ClientExecutor executor;

    public static interface ICallback<T> {
        public void call(Object... args);
    }

    public abstract static class Callback<Result> implements ICallback {
        abstract public void called(Result result);

        @Override
        @SuppressWarnings("unchecked")
        public void call(Object... args) {
            called((Result) args[0]);
        }
    }

    //    @Override
    public <T extends EventClass> void on(Class<T> key, T cb) {
        add(key, cb);
    }

    //    @Override
    public <T extends EventClass> void once(final Class<T> key, final T cb) {
        add(key, new ICallback() {

            public void call(Object... args) {
                removeListener(key, this);
                cb.call(args);
            }
        });
    }

    //    @Override
    public <T extends EventClass> int emit(Class<T> key, Object... args) {
        ClientLogger.log("Emitting %s from thread: %s", key.getSimpleName(), Thread.currentThread());
        // Make a copy of the array at the time of emission
        ArrayList<ICallback> iCallbacks = listFor(key);
        ICallback[] callbacks = new ICallback[iCallbacks.size()];
        callbacks = iCallbacks.toArray(callbacks);
        for (ICallback callback : callbacks) {
            callback.call(args);
        }
        return callbacks.length;
    }

    public static class ExecutorCallbackPair {
        ClientExecutor executor;
        ICallback t;
    }
    public static class CallbackList extends ArrayList<ExecutorCallbackPair> {
        public boolean remove(ICallback t) {
            Iterator<ExecutorCallbackPair> iter = iterator();
            while (iter.hasNext()) {
                ExecutorCallbackPair next = iter.next();
                if (next.t == t) {
                    iter.remove();
                    return true;
                }
            }
            return false;
        }
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

    private <T extends EventClass> void add(Class<T> key, ICallback cb) {
        listFor(key).add(cb);
    }

    //    @Override
    public void removeListener(Class<? extends EventClass> key, ICallback cb) {
        listFor(key).remove(cb);
    }
}
