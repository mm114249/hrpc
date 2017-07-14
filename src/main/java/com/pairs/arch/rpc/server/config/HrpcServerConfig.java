package com.pairs.arch.rpc.server.config;

import com.google.common.base.Joiner;
import com.pairs.arch.rpc.server.annotation.HrpcServer;
import com.pairs.arch.rpc.server.container.ApplicationContextBuilder;
import com.pairs.arch.rpc.server.container.ApplicationContextContainer;
import com.pairs.arch.rpc.server.helper.BootstrapCreaterHelper;
import com.pairs.arch.rpc.server.util.ClassScaner;
import com.pairs.arch.rpc.server.util.ServerWrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
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
    //服务器端处理业务的线程数量
    private  EventExecutorGroup eventExecutors=null;
    private  EventLoopGroup boss=null;
    private  EventLoopGroup work=null;

    public HrpcServerConfig(){
        this.eventExecutors=new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors());
        this.boss=new NioEventLoopGroup(1);
        this.work=new NioEventLoopGroup(2);
    }

    public HrpcServerConfig(Integer serverPort, String zkAddress) {
        this(serverPort,zkAddress,Runtime.getRuntime().availableProcessors(),1,2);
    }

    public HrpcServerConfig(Integer serverPort, String zkAddress,Integer eventExecutorsCount,Integer bossCount,Integer workCount){
        this.serverPort = serverPort;
        this.zkAddress = zkAddress;
        this.eventExecutors=new DefaultEventExecutorGroup(eventExecutorsCount);
        this.boss=new NioEventLoopGroup(bossCount);
        this.work=new NioEventLoopGroup(workCount);
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

    public EventExecutorGroup getEventExecutors() {
        return eventExecutors;
    }

    public void setEventExecutors(EventExecutorGroup eventExecutors) {
        this.eventExecutors = eventExecutors;
    }

    public EventLoopGroup getBoss() {
        return boss;
    }

    public void setBoss(EventLoopGroup boss) {
        this.boss = boss;
    }

    public EventLoopGroup getWork() {
        return work;
    }

    public void setWork(EventLoopGroup work) {
        this.work = work;
    }
}
