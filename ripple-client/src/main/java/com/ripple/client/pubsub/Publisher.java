package com.ripple.client.pubsub;

import com.ripple.client.ClientLogger;

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

    public <T extends EventClass> void on(Class<T> key, CallbackContext executor, T cb) {
        add(key, executor, cb);
    }

    public <T extends EventClass> void once(final Class<T> key, final T cb) {
        once(key, null, cb);
    }

    public <T extends EventClass> void once(final Class<T> key, CallbackContext executor, final T cb) {
        add(key, executor, cb, true);
    }

    public <T extends EventClass> int emit(Class<T> key, Object... args) {
        ClientLogger.log("Emitting %s from thread: %s", key.getSimpleName(), Thread.currentThread());
        CallbackList callbacks = cbs.get(key);
        if (callbacks == null) {
            return 0;
        }

        Iterator<ContextedCallback> iterator = callbacks.iterator();
        boolean removed;
        int executed = 0;

        while (iterator.hasNext()) {
            removed = false;

            ContextedCallback pair = iterator.next();
            CallbackContext context = pair.context;
            if (context == null) {
                pair.callback.call(args);
                executed++;
                // explicitly repeated
            } else {
                if (context.shouldExecute()) {
                    context.execute(pair.runnableWrappedCallback(args));
                    executed++;
                }
                // we only want to call remove once
                else if (context.shouldRemove()) {
                    iterator.remove();
                    removed = true;
                }
            }
            if (pair.oneShot && !removed) {
                iterator.remove();
            }
        }
        return executed;
    }

    private static class ContextedCallback {
        CallbackContext context;
        ICallback callback;
        boolean oneShot;

        public ContextedCallback(ICallback callback, CallbackContext context, boolean oneShot) {
            this.context = context;
            this.callback = callback;
            this.oneShot = oneShot;
        }

        public Runnable runnableWrappedCallback(final Object... args) {
            return new Runnable() {
                @Override
                public void run() {
                    callback.call(args);
                }
            };
        }
    }
    private static class CallbackList extends ArrayList<ContextedCallback> {
        public boolean remove(ICallback t) {
            Iterator<ContextedCallback> iter = iterator();
            while (iter.hasNext()) {
                ContextedCallback next = iter.next();
                if (next.callback == t) {
                    iter.remove();
                    return true;
                }
            }
            return false;
        }

        public void add(CallbackContext exec, ICallback cb, boolean oneShot) {
            add(new ContextedCallback(cb, exec, oneShot));
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
        add(key, null, cb, false);
    }

    private <T extends EventClass> void add(Class<T> key, CallbackContext executor, ICallback cb) {
        add(key, executor, cb, false);
    }

    private <T extends EventClass> void add(Class<T> key, CallbackContext executor, ICallback cb, boolean b) {
        listFor(key).add(executor, cb, b);
    }

    public <T extends EventClass> boolean removeListener(Class<T> key, ICallback cb) {
        return listFor(key).remove(cb);
    }
}
