package com.pairs.arch.rpc.common.bean;

/**
 * Created by hupeng on 2017/3/24.
 */
public class HrpcRequest {

    private Short type;
    private String requestId;
    private String className;
    private String methodName;
    private Class[] parameterTypes;
    private Object[] parameters;

    public HrpcRequest(RequestType requestType){
        this.type=requestType.getValue();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        this.type = type;
    }

    public enum RequestType{
        NORMAL(Short.valueOf("1")),
        HEART(Short.valueOf("2"));

        private short value;

        private RequestType(short value){
            this.value=value;
        }

        public short getValue() {
            return value;
        }
    }


}
