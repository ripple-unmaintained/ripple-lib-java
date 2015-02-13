package com.ripple.client.pubsub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Publisher<EventClass extends Publisher.Callback> {
    static final Logger logger = Logger.getLogger(Publisher.class.getName());
    private void log(Level level, String message, Object... params) {
        logger.log(level, message, params);
    }

    public static interface Callback<T> {
        public void called(T args);
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

    public <T extends EventClass> int emit(Class<T> key, Object args) {
        log(Level.FINE, "Emitting {0} from thread: {1}", key.getSimpleName(), Thread.currentThread());

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
                execute(args, pair);
                executed++;
            } else {
                if (context.shouldExecute()) {
                    context.execute(pair.runnableWrappedCallback(args));
                    executed++;
                }
                else if (context.shouldRemove()) {
                    iterator.remove();
                    removed = true;
                }
            }
            // we only want to call remove once
            if (pair.oneShot && !removed) {
                iterator.remove();
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

    private <T extends EventClass> void add(Class<T> key, Callback cb) {
        add(key, null, cb, false);
    }

    private <T extends EventClass> void add(Class<T> key, CallbackContext executor, Callback cb) {
        add(key, executor, cb, false);
    }

    private <T extends EventClass> void add(Class<T> key, CallbackContext executor, Callback cb, boolean b) {
        listFor(key).add(executor, cb, b);
    }

    public <T extends EventClass> boolean removeListener(Class<T> key, Callback cb) {
        return listFor(key).remove(cb);
    }

    public void clearAllListeners() {
        cbs.clear();
    }
}
