package com.pairs.arch.rpc.remote;


import com.pairs.arch.rpc.remote.model.RemotingResponse;

/**
 * 
 * @author BazingaLyn
 * @description 远程调用之后的回调函数
 * @time 2016年8月10日11:06:40
 * @modifytime
 */
public interface InvokeCallback {
	
    void operationComplete(final RemotingResponse remotingResponse);
    
}
