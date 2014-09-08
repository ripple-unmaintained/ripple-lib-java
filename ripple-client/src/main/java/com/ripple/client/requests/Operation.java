package com.ripple.client.requests;

import com.ripple.client.async.ComposedOperation;
import com.ripple.client.responses.Response;
import org.json.JSONException;

public class Operation<T> extends Request.Manager<T> {
    public int nth;
    public ComposedOperation composed;

    public Operation(int nth, ComposedOperation composed) {
        this.nth = nth;
        this.composed = composed;
    }

    @Override
    public void beforeRequest(Request r) {
        super.beforeRequest(r);
    }

    @Override
    public boolean retryOnUnsuccessful(Response r) {
        return super.retryOnUnsuccessful(r);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void cb(Response response, T t) throws JSONException {
        switch (nth) {
            case 1:
                composed.first(t);
                break;
            case 2:
                composed.second(t);
                break;
            case 3:
                composed.third(t);
                break;
            case 4:
                composed.fourth(t);
                break;
        }
    }
}
