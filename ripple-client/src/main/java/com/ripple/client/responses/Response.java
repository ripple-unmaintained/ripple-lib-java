package com.ripple.client.responses;

import org.json.JSONObject;

import com.ripple.client.enums.RPCErr;
import com.ripple.client.requests.Request;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.serialized.enums.EngineResult;

public class Response {
    public JSONObject message;
    public Request request;
    public JSONObject result;
    public boolean succeeded;
    public String status;
    public RPCErr rpcerr;
    public String error;
    public String error_message;

    public Response(Request request, JSONObject message) {
        this.message = message;
        this.request = request;
        status = message.getString("status");
        succeeded = status.equals("success");
        if (succeeded) {
            this.result = message.getJSONObject("result");
            rpcerr = null;
        } else {
            try {
                error = message.getString("error");
                this.rpcerr = RPCErr.valueOf(error);
            } catch (Exception e) {
                rpcerr = RPCErr.unknownError;
            }
        }
    }

    public EngineResult engineResult() {
        return EngineResult.valueOf(result.getString("engine_result"));
    }

    public UInt32 getSubmitSequence() {
        return new UInt32(result.optJSONObject("tx_json").optInt("Sequence"));
    }
}
