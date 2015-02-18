package com.ripple.client.pubsub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Publisher<CompatHack extends Publisher.Callback> {
    static final Logger logger = Logger.getLogger(Publisher.class.getName());
    private void log(Level level, String message, Object... params) {
        logger.log(level, message, params);
    }

    public static interface Callback<T> {
        public void called(T args);
    }

    public static interface ErrBack<T> extends Callback<T> {
        public void erred(RuntimeException args);
    }

    public <A, T extends Callback<A>> void on(Class<T> key, T cb) {
        add(key, cb);
    }

    public <A, T extends Callback<A>> void on(Class<T> key, CallbackContext executor, T cb) {
        add(key, executor, cb);
    }

    public <A, T extends Callback<A>> void once(final Class<T> key, final T cb) {
        once(key, null, cb);
    }

    public <A, T extends Callback<A>> void once(final Class<T> key, CallbackContext executor, final T cb) {
        add(key, executor, cb, true);
    }

    public <A, T extends Callback<A>> int emit(Class<T> key, A args) {
        if (logger.isLoggable(Level.FINE)) {
            log(Level.FINE, "Emitting {0} from thread: {1}", key.getSimpleName(), Thread.currentThread());
        }

        int executed = 0;
        CallbackList callbacks = (cbs.get(key));

        if (callbacks != null) {
            CallbackList copy = new CallbackList(callbacks);

            for (ContextedCallback pair : copy) {
                boolean removed = false;

                CallbackContext context = pair.context;
                if (context == null) {
                    execute(args, pair);
                    executed++;
                } else {
                    if (context.shouldExecute()) {
                        context.execute(pair.runnableWrappedCallback(args));
                        executed++;
                    } else if (context.shouldRemove()) {
                        callbacks.remove(pair);
                        removed = true;
                    }
                }
                // we only want to call remove once
                if (pair.oneShot && !removed) {
                    callbacks.remove(pair);
                }
            }
        }
        return executed;
    }

    @SuppressWarnings("unchecked")
    public static void execute(Object args, ContextedCallback pair) {
        pair.callback.called(args);
    }

    private static class ContextedCallback {
        CallbackContext context;
        Callback callback;
        boolean oneShot;

        public ContextedCallback(Callback callback, CallbackContext context, boolean oneShot) {
            this.context = context;
            this.callback = callback;
            this.oneShot = oneShot;
        }

        public Runnable runnableWrappedCallback(final Object args) {
            return new Runnable() {
                @Override
                public void run() {
                    execute(args, ContextedCallback.this);
                }
            };
        }
    }

    private static class CallbackList extends ArrayList<ContextedCallback> {
        public CallbackList() {}
        public CallbackList(CallbackList callbacks) {
            super(callbacks);
        }

        @Override
        public ContextedCallback get(int index) {
            return super.get(index);
        }

        public boolean remove(Callback t) {
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

        public void add(CallbackContext exec, Callback cb, boolean oneShot) {
            add(new ContextedCallback(cb, exec, oneShot));
        }
    }

    private class DefaultCallbackListMap extends HashMap<Class<? extends Callback>, CallbackList> {
        public CallbackList getDefault(Class<? extends Callback> key) {
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

    private CallbackList listFor(Class<? extends Callback> key) {
        return cbs.getDefault(key);
    }

    private <A, T extends Callback<A>> void add(Class<T> key, Callback<A> cb) {
        add(key, null, cb, false);
    }

    private <A, T extends Callback<A>> void add(Class<T> key, CallbackContext executor, Callback<A> cb) {
        add(key, executor, cb, false);
    }

    private <A, T extends Callback<A>> void add(Class<T> key, CallbackContext executor, final Callback<A> cb, boolean b) {
        listFor(key).add(executor, cb, b);
    }

    public <A, T extends Callback<A>> boolean removeListener(Class<T> key, Callback<A> cb) {
        return listFor(key).remove(cb);
    }

    public void clearAllListeners() {
        cbs.clear();
    }
}
