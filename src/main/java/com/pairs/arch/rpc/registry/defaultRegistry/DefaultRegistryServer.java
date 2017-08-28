package com.pairs.arch.rpc.registry.defaultRegistry;

import com.pairs.arch.rpc.common.util.NamedThreadFactory;
import com.pairs.arch.rpc.registry.RegistryServer;
import com.pairs.arch.rpc.remote.component.NettyRemetingServer;
import com.pairs.arch.rpc.remote.component.RemotingServer;
import com.pairs.arch.rpc.remote.config.NettyServerConfig;

import java.rmi.server.RemoteServer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 2017年08月21日16:22
 * <p/>
 * Title:[]
 * </p >
 * <p/>
 * Description :[默认注册中心服务]
 * </p >
 * Company:
 *
 * @author [hupeng]
 * @version 1.0
 **/
public class DefaultRegistryServer implements RegistryServer {

    private RegistryProviderManager registryProviderManager;
    private NettyRemetingServer nettyRemetingServer;

    private RegistryServerConfig registryServerConfig;
    private NettyServerConfig nettyServerConfig;

    private ExecutorService remotingExecutor;                //执行器
    private ExecutorService remotingChannelInactiveExecutor; //channel inactive的线程执行器


    public DefaultRegistryServer(RegistryServerConfig registryServerConfig, NettyServerConfig nettyServerConfig, RegistryProviderManager registryProviderManager) {
        this.registryServerConfig = registryServerConfig;
        this.nettyServerConfig = nettyServerConfig;
        this.registryProviderManager = registryProviderManager;
    }

    private void initialize() {
        this.nettyRemetingServer=new NettyRemetingServer(nettyServerConfig);
        this.remotingExecutor= Executors.newFixedThreadPool(this.nettyServerConfig.getServerWorkerThreads(),new NamedThreadFactory("RegistryCenterExecutorThread_"));
        this.remotingChannelInactiveExecutor=Executors.newFixedThreadPool(this.nettyServerConfig.getChannelInactiveHandlerThreads(),
                new NamedThreadFactory("RegistryCenterChannelInActiveExecutorThread_")
                );
    }

    private void registerProcessor() {
    }


    @Override
    public void start() {

    }


    public RegistryProviderManager getRegistryProviderManager() {
        return registryProviderManager;
    }

    public void setRegistryProviderManager(RegistryProviderManager registryProviderManager) {
        this.registryProviderManager = registryProviderManager;
    }

    public NettyRemetingServer getNettyRemetingServer() {
        return nettyRemetingServer;
    }

    public void setNettyRemetingServer(NettyRemetingServer nettyRemetingServer) {
        this.nettyRemetingServer = nettyRemetingServer;
    }
}
