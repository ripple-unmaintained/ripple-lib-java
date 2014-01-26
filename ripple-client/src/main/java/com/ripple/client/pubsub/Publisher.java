package com.ripple.client.pubsub;

import com.ripple.client.ClientLogger;
import com.ripple.client.executors.ClientExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Publisher<EventClass extends Publisher.ICallback> {
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

    public <T extends EventClass> void on(Class<T> key, T cb) {
        add(key, cb);
    }

    public <T extends EventClass> void on(Class<T> key, ClientExecutor executor, T cb) {
        add(key, executor, cb);
    }

    public <T extends EventClass> void once(final Class<T> key, final T cb) {
        once(key, null, cb);
    }

    public <T extends EventClass> void once(final Class<T> key, ClientExecutor executor, final T cb) {
        add(key, executor, new ICallback() {
            public void call(Object... args) {
                removeListener(key, this);
                cb.call(args);
            }
        });
    }

    public <T extends EventClass> int emit(Class<T> key, Object... args) {
        ClientLogger.log("Emitting %s from thread: %s", key.getSimpleName(), Thread.currentThread());
        // Make a copy of the array at the time of emission
        CallbackList iCallbacks = listFor(key);
        ExecutorCallbackPair[] callbacks = new ExecutorCallbackPair[iCallbacks.size()];
        callbacks = iCallbacks.toArray(callbacks);

        for (ExecutorCallbackPair callback : callbacks) {
            if (callback.executor == null) {
                callback.callback.call(args);
            } else {
//                ClientLogger.log("Using executor to execute for " + key.getSimpleName());
                callback.executor.execute(callback.getRunnable(args));
            }
        }
        return callbacks.length;
    }

    private static class ExecutorCallbackPair {
        ClientExecutor executor;
        ICallback callback;

        public ExecutorCallbackPair(ICallback callback, ClientExecutor executor) {
            this.executor = executor;
            this.callback = callback;
        }

        public Runnable getRunnable(final Object... args) {
            return new Runnable() {
                @Override
                public void run() {
                    callback.call(args);
                }
            };
        }
    }
    private static class CallbackList extends ArrayList<ExecutorCallbackPair> {
        public boolean remove(ICallback t) {
            Iterator<ExecutorCallbackPair> iter = iterator();
            while (iter.hasNext()) {
                ExecutorCallbackPair next = iter.next();
                if (next.callback == t) {
                    iter.remove();
                    return true;
                }
            }
            return false;
        }

        public void add(ICallback cb) {
            add(new ExecutorCallbackPair(cb, null));
        }

        public void add(ClientExecutor exec, ICallback cb) {
            add(new ExecutorCallbackPair(cb, exec));
        }
    }

    private class DefaultCallbackListMap extends HashMap<Class<? extends EventClass>, CallbackList> {
        public CallbackList getDefault(Class<? extends EventClass> key) {
            CallbackList list = super.get(key);
            if (list == null) {
                CallbackList newList = new CallbackList();
                put(key, newList);
                return newList;
            }
            return list;
        }
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final DefaultCallbackListMap cbs = new DefaultCallbackListMap();

    private CallbackList listFor(Class<? extends EventClass> key) {
        return cbs.getDefault(key);
    }

    private <T extends EventClass> void add(Class<T> key, ICallback cb) {
        listFor(key).add(cb);
    }

    private <T extends EventClass> void add(Class<T> key, ClientExecutor executor, ICallback cb) {
        listFor(key).add(executor, cb);
    }

    public boolean removeListener(Class<? extends EventClass> key, ICallback cb) {
        return listFor(key).remove(cb);
    }
}
