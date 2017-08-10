package com.pairs.arch.rpc.remote.component;

/**
 * Created on 2017年08月09日10:22
 * 定义客户端和服务器端共有的行为
 * @author [hupeng]
 * @version 1.0
 **/
public interface BaseRemotingService {

    void init();

    void start();

    void shutdown();

}
