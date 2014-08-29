package com.ripple.client.subscriptions;

import com.ripple.client.pubsub.Publisher;
import com.ripple.core.coretypes.AccountID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class SubscriptionManager extends Publisher<SubscriptionManager.events> {
    public void pauseEventEmissions() {
        paused = true;
    }

    public void unpauseEventEmissions() {
        paused = false;
    }

    public static abstract class events<T>      extends Publisher.Callback<T> {}

    public abstract static class OnSubscribed extends events<JSONObject> {}
    public abstract static class OnUnSubscribed extends events<JSONObject> {}

    public boolean paused = false;

    public enum Stream {
        server,
        ledger,
        transactions,
        transactions_propose
    }

    Set<Stream>                  streams = new TreeSet<Stream>();
    Set<AccountID>              accounts = new TreeSet<AccountID>();

    <T> Set<T> single(T element) {
        Set<T> set = new TreeSet<T>();
        set.add(element);
        return set;
    }

    public void addStream(Stream s) {
        streams.add(s);
        subscribeStream(s);
    }

    public void removeStream(Stream s) {
        streams.remove(s);
        unsubscribeStream(s);
    }

    private void subscribeStream(Stream s) {
       emit(OnSubscribed.class, basicSubscriptionObject(single(s), null));
    }

    @Override
    public <T extends events> int emit(Class<T> key, Object... args) {
        if (paused) {
            return 0;
        }
        return super.emit(key, args);
    }

    private void unsubscribeStream(Stream s) {
        emit(OnUnSubscribed.class, basicSubscriptionObject(single(s), null));
    }

    public void addAccount(AccountID a) {
        accounts.add(a);
        emit(OnSubscribed.class, basicSubscriptionObject(null, single(a)));
    }
    public void removeAccount(AccountID a) {
        accounts.remove(a);
        emit(OnUnSubscribed.class, basicSubscriptionObject(null, single(a)));
    }

    private JSONObject basicSubscriptionObject(Set<Stream> streams, Set<AccountID> accounts) {
        JSONObject subs = new JSONObject();
        try {
            if (streams != null && streams.size() > 0) subs.put("streams", getJsonArray(streams));
            if (accounts != null && accounts.size() > 0) subs.put("accounts", getJsonArray(accounts));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return subs;
    }

    private JSONArray getJsonArray(Collection<?> streams) {
        JSONArray jsonArray = new JSONArray();
        for (Object obj : streams) {
            jsonArray.put(obj);
        }

        return jsonArray;
    }

    public JSONObject allSubscribed() {
        return basicSubscriptionObject(streams, accounts);
    }
}
