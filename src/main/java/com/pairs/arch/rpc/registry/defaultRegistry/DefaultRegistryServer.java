package com.pairs.arch.rpc.registry.defaultRegistry;

import com.alibaba.fastjson.JSONArray;
import com.pairs.arch.rpc.common.rpc.registry.RegistryPersistRecord;
import com.pairs.arch.rpc.common.util.NamedThreadFactory;
import com.pairs.arch.rpc.common.util.PersistUtils;
import com.pairs.arch.rpc.registry.RegistryServer;
import com.pairs.arch.rpc.remote.component.NettyRemetingServer;
import com.pairs.arch.rpc.remote.component.RemotingServer;
import com.pairs.arch.rpc.remote.config.NettyServerConfig;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.server.RemoteServer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    private Logger logger= LoggerFactory.getLogger(DefaultRegistryServer.class);

    private RegistryProviderManager registryProviderManager;
    private RegistryConsumerManager registryConsumerManager;
    private NettyRemetingServer nettyRemetingServer;


    private RegistryServerConfig registryServerConfig;
    private NettyServerConfig nettyServerConfig;

    private ExecutorService remotingExecutor;
    private ExecutorService remotingChannelInactiveExecutor;

    //定时任务
    private final ScheduledExecutorService scheduledExecutorService = Executors
            .newSingleThreadScheduledExecutor(new NamedThreadFactory("registry-timer"));

    public DefaultRegistryServer(RegistryServerConfig registryServerConfig, NettyServerConfig nettyServerConfig) {
        this.registryServerConfig = registryServerConfig;
        this.nettyServerConfig = nettyServerConfig;
        this.registryProviderManager = new RegistryProviderManager(this);
        this.registryConsumerManager=new RegistryConsumerManager(this);
        initialize();
    }

    private void initialize() {
        this.nettyRemetingServer=new NettyRemetingServer(nettyServerConfig);
        this.remotingExecutor= Executors.newFixedThreadPool(this.nettyServerConfig.getServerWorkerThreads(),new NamedThreadFactory("RegistryCenterExecutorThread_"));
        this.remotingChannelInactiveExecutor=Executors.newFixedThreadPool(this.nettyServerConfig.getChannelInactiveHandlerThreads(),
                new NamedThreadFactory("RegistryCenterChannelInActiveExecutorThread_")
                );
        //注册处理器
        this.registerProcessor();
        //从硬盘恢复服务
        this.recoverServiceInfoFromDisk();

        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                // 延迟60秒，每隔一段时间将一些服务信息持久化到硬盘上
                try {
                   DefaultRegistryServer.this.getRegistryProviderManager().persistServiceInfo();
                } catch (Exception e) {
                    logger.warn("schedule persist failed [{}]",e.getMessage());
                }
            }
        }, 60, this.registryServerConfig.getPersistTime(), TimeUnit.SECONDS);

    }

    /**
     * 注册处理器
     */
    private void registerProcessor() {
        this.nettyRemetingServer.registerDefaultProcessor(new DefaultRegistryProcessor(this),this.remotingExecutor);
        this.nettyRemetingServer.registerChannelInactiveProcessor(new DefaultRegistryChannelInactiveProcessor(this),this.remotingChannelInactiveExecutor);
    }

    //从硬盘恢复服务
    private void recoverServiceInfoFromDisk(){
        String persistString= PersistUtils.file2String(registryServerConfig.getStorePathRootDir());
        if(StringUtils.isNotBlank(persistString)){
            List<RegistryPersistRecord> recordList= JSONArray.parseArray(persistString,RegistryPersistRecord.class);
            for (RegistryPersistRecord record : recordList) {
                this.getRegistryProviderManager().getHistoryRecords().put(record.getServiceName(),record);
            }
        }
    }


    @Override
    public void start() {
        this.nettyRemetingServer.start();
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

    public NettyServerConfig getNettyServerConfig() {
        return nettyServerConfig;
    }

    public void setNettyServerConfig(NettyServerConfig nettyServerConfig) {
        this.nettyServerConfig = nettyServerConfig;
    }

    public RegistryServerConfig getRegistryServerConfig() {
        return registryServerConfig;
    }

    public void setRegistryServerConfig(RegistryServerConfig registryServerConfig) {
        this.registryServerConfig = registryServerConfig;
    }

    public RegistryConsumerManager getRegistryConsumerManager() {
        return registryConsumerManager;
    }

    public void setRegistryConsumerManager(RegistryConsumerManager registryConsumerManager) {
        this.registryConsumerManager = registryConsumerManager;
    }
}
