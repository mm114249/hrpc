package com.pairs.arch.rpc.server.config;

import com.google.common.base.Joiner;
import com.pairs.arch.rpc.server.annotation.HrpcServer;
import com.pairs.arch.rpc.server.container.ApplicationContextBuilder;
import com.pairs.arch.rpc.server.container.ApplicationContextContainer;
import com.pairs.arch.rpc.server.helper.BootstrapCreaterHelper;
import com.pairs.arch.rpc.server.util.ClassScaner;
import com.pairs.arch.rpc.server.util.ServerWrap;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hupeng on 2017/3/28.
 */
public class HrpcServerConfig  {

    private Integer serverPort = 8010;
    private String zkAddress = "127.0.0.1:2181";
    private String rootPath = "/hrpc";

    public HrpcServerConfig(){

    }

    public HrpcServerConfig(Integer serverPort, String zkAddress) {
        this.serverPort = serverPort;
        this.zkAddress = zkAddress;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getZkAddress() {
        return zkAddress;
    }

    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    public String getRootPath() {
        return rootPath;
    }
}
