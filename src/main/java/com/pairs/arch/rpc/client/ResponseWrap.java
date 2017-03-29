package com.pairs.arch.rpc.client;

import com.pairs.arch.rpc.common.bean.HrpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hupeng on 2017/3/28.
 */
public class ResponseWrap {

    private static ResponseWrap instance=new ResponseWrap();

    private Map<String,HrpcResponse> responseMap=new ConcurrentHashMap<String,HrpcResponse>();


    public HrpcResponse get(String uuid){
        return responseMap.get(uuid);
    }

    public void put(String uuid,HrpcResponse response){
        responseMap.put(uuid,response);
    }


    private ResponseWrap(){

    }

    public static ResponseWrap getInstance(){
        return instance;
    }


}
