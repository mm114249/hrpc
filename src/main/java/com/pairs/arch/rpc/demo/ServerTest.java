package com.pairs.arch.rpc.demo;

import com.google.common.collect.Lists;
import com.pairs.arch.rpc.server.config.HrpcServerConfig;

/**
 * Created by hupeng on 2017/3/28.
 */
public class ServerTest {
    public static void main(String[] args) {
        HrpcServerConfig hrpcServerConfig=HrpcServerConfig.getInstance(Lists.newArrayList("com.pairs.arch.rpc.demo"));
    }
}