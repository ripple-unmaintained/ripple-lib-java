package com.ripple.client.subscriptions;

import com.ripple.client.pubsub.Publisher;
import com.ripple.core.types.AccountID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;
import java.util.TreeSet;

public class SubscriptionManager extends Publisher<SubscriptionManager.events> {
    public static abstract class events<T>      extends Publisher.Callback<T> {}

    public abstract static class OnSubscribed extends events<JSONObject> {}
    public abstract static class OnUnSubscribed extends events<JSONObject> {}

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
        emit(OnSubscribed.class, basicSubscriptionObject(single(s), null));
    }
    public void removeStream(Stream s) {
        streams.remove(s);
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
            if (streams != null && streams.size() > 0) subs.put("streams", new JSONArray(streams));
            if (accounts != null && accounts.size() > 0) subs.put("accounts", new JSONArray(accounts));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return subs;
    }

    public JSONObject allSubscribed() {
        return basicSubscriptionObject(streams, accounts);
    }
}
