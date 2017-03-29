package com.pairs.arch.rpc.demo;

import com.pairs.arch.rpc.client.proxy.HrpcProxy;
import com.pairs.arch.rpc.client.registry.ServerDiscovery;

/**
 * Created by hupeng on 2017/3/28.
 */
public class ClientTest {

    public static void main(String[] args) {
        HelloServer helloServer= HrpcProxy.getInstance(ServerDiscovery.getInstance()).getBean(HelloServer.class);
        System.out.println(helloServer.getName("aaa"));
    }
}
