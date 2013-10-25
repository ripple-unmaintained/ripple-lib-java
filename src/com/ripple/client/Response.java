package com.ripple.client;

import com.ripple.client.enums.RPCErr;
import com.ripple.core.enums.TransactionEngineResult;
import org.json.JSONException;
import org.json.JSONObject;

public class Response {
    public JSONObject message;
    public Request request;
    public JSONObject result;
    public boolean succeeded;
    public String status;
    public RPCErr error;

    public Response(Request request, JSONObject message) {
        try {
            // TODO: do error handling, then check usages
            this.message = message;
            this.request = request;
            status = message.getString("status");
            succeeded = status.equals("success");
            if (succeeded) {
                this.result = message.getJSONObject("result");
                error = null;
            } else {
                error = RPCErr.valueOf(message.getString("error"));
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public TransactionEngineResult engineResult() {
        try {
            return TransactionEngineResult.valueOf(result.getString("engine_result"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
