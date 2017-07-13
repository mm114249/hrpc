package com.pairs.arch.rpc.common.bean;

/**
 * Created by hupeng on 2017/3/24.
 */
public class HrpcResponse {

    private Short type;//消息类型 1正常消息 2心跳消息
    private String requestId;
    private Throwable error;
    private Object result;

    public HrpcResponse(HrpcRequest.RequestType type) {
        this.type = type.getValue();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        this.type = type;
    }
}
