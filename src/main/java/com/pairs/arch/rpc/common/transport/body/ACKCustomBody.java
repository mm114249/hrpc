package com.pairs.arch.rpc.common.transport.body;

import com.pairs.arch.rpc.remote.model.CommonCustomBody;

/**
 * Created on 2017年08月15日16:58
 * <p>
 * Title:[]
 * </p >
 * <p>
 * Description :[]
 * </p >
 * Company:
 *
 * @author [hupeng]
 * @version 1.0
 **/
public class ACKCustomBody implements CommonCustomBody {
    private Long requestId;//消息id
    private boolean isSuccess;//是否成功
    private String msg;

    public ACKCustomBody(){

    }

    public ACKCustomBody(Long requestId, boolean isSuccess,String msg) {
        this.requestId = requestId;
        this.isSuccess = isSuccess;
        this.msg=msg;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean getIsSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}
