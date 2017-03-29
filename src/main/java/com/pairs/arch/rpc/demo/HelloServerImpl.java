package com.pairs.arch.rpc.demo;

import com.pairs.arch.rpc.server.annotation.HrpcServer;

/**
 * Created by hupeng on 2017/3/28.
 */
@HrpcServer(value = HelloServer.class)
public class HelloServerImpl implements HelloServer {
    @Override
    public String getName(String name) {
        System.out.println(121212+name);
        return name+"call success";
    }
}
