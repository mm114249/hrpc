package com.pairs.arch.rpc.server.config;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;

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
        this.eventExecutors=new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors(),new DefaultThreadFactory("netty.server.executor"));
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
