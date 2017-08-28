package com.pairs.arch.rpc.registry.defaultRegistry;

import com.google.common.collect.Lists;
import com.pairs.arch.rpc.common.protocol.HrpcProtocol;
import com.pairs.arch.rpc.common.rpc.registry.LoadBalanceStrategy;
import com.pairs.arch.rpc.common.rpc.registry.RegisterMeta;
import com.pairs.arch.rpc.common.rpc.registry.RegistryPersistRecord;
import com.pairs.arch.rpc.common.rpc.registry.ServiceReviewState;
import com.pairs.arch.rpc.common.serializer.SerializerHolder;
import com.pairs.arch.rpc.common.transport.body.ACKCustomBody;
import com.pairs.arch.rpc.common.transport.body.PublishServiceCustomBody;
import com.pairs.arch.rpc.remote.model.RemotingTransporter;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ConcurrentSet;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created on 2017年08月18日10:48
 * <p/>
 * Title:[]
 * </p >
 * <p/>
 * Description :[]
 * </p >
 * Company:
 *
 * @author [hupeng]
 * @version 1.0
 **/
public class RegistryProviderManager implements RegistryProviderServer {

    private Logger logger = LoggerFactory.getLogger(RegistryProviderManager.class);

    //channel绑定的服务单元
    private AttributeKey<ConcurrentSet<RegisterMeta>> S_PUBLISH_KEY = AttributeKey.valueOf("server.published");

    //服务列表 key服务名 value服务单元map
    private ConcurrentMap<String, ConcurrentMap<RegisterMeta.Address, RegisterMeta>> globalRegisterInfoMap = new ConcurrentHashMap<>();
    // 指定节点都注册了哪些服务
    private final ConcurrentMap<RegisterMeta.Address, ConcurrentSet<String>> globalServiceMetaMap = new ConcurrentHashMap<RegisterMeta.Address, ConcurrentSet<String>>();
    //每个服务的历史记录  key服务名
    private ConcurrentMap<String, RegistryPersistRecord> historyRecords = new ConcurrentHashMap<>();
    //默认的负载均衡策略
    private LoadBalanceStrategy defaultLoadBalanceStrategy = LoadBalanceStrategy.RANDOM;
    //每个服务对应的负载策略
    private final ConcurrentMap<String, LoadBalanceStrategy> globalServiceLoadBalance = new ConcurrentHashMap<String, LoadBalanceStrategy>();
    // 提供者某个地址对应的channel
    private final ConcurrentMap<RegisterMeta.Address, Channel> globalProviderChannelMetaMap = new ConcurrentHashMap<RegisterMeta.Address, Channel>();

    @Override
    public RemotingTransporter handlerRegister(RemotingTransporter remotingTransporter, Channel channel) throws InterruptedException {
        ACKCustomBody ackCustomBody = new ACKCustomBody(remotingTransporter.getOpaque(), false, "");
        RemotingTransporter responseTransporter = RemotingTransporter.createResponseTransport(HrpcProtocol.ACK, ackCustomBody, remotingTransporter.getOpaque());
        PublishServiceCustomBody publishServiceCustomBody = SerializerHolder.serializerImpl().readObject(remotingTransporter.bytes(), PublishServiceCustomBody.class);

        //创建一个服务注册单元
        RegisterMeta registerMeta = RegisterMeta.createRegiserMeta(publishServiceCustomBody, channel);
        logger.info("Publish [{}] on channel[{}].", registerMeta, channel);

        //channel绑定一个服务单元
        attachPublishEventOnChannel(registerMeta, channel);

        //获取一个最小服务单元.找出提供此服务的全部地址和该服务在该地址下的审核情况
        ConcurrentMap<RegisterMeta.Address, RegisterMeta> addressAndMetaMap = this.getRegisterMeta(registerMeta.getServiceName());

        synchronized (globalRegisterInfoMap) {
            RegistryPersistRecord record = historyRecords.get(registerMeta.getServiceName());
            //如果在全局map中没有服务单元信息,说明该服务单元没有注册过,可以从历史信息中恢复
            if (addressAndMetaMap.get(registerMeta.getAddress()) == null) {
                //注册历史中没有,就新建一个历史记录
                if (historyRecords.get(registerMeta.getServiceName()) == null) {
                    RegistryPersistRecord.PersistProviderInfo persistProviderInfo = new RegistryPersistRecord.PersistProviderInfo();
                    persistProviderInfo.setAddress(registerMeta.getAddress());

                    record = new RegistryPersistRecord();
                    record.setServiceName(registerMeta.getServiceName());
                    record.setBalanceStrategy(LoadBalanceStrategy.RANDOM);
                    record.setProviderInfos(Lists.newArrayList(persistProviderInfo));
                } else {
                    boolean isHis = isContainChildrenInfo(record, registerMeta.getAddress());
                    if (!isHis) {
                        RegistryPersistRecord.PersistProviderInfo persistProviderInfo = new RegistryPersistRecord.PersistProviderInfo();
                        persistProviderInfo.setAddress(registerMeta.getAddress());
                        historyRecords.get(registerMeta.getServiceName()).getProviderInfos().add(persistProviderInfo);
                    }

                }
            }

            for (RegistryPersistRecord.PersistProviderInfo persistProviderInfo : record.getProviderInfos()) {
                if (persistProviderInfo.getAddress().equals(registerMeta.getAddress())) {
                    registerMeta.setIsReviewed(persistProviderInfo.getIsReviewed());
                }
            }

            addressAndMetaMap.put(registerMeta.getAddress(), registerMeta);

            this.getServiceMeta(registerMeta.getAddress()).add(registerMeta.getServiceName());

            if (historyRecords.get(registerMeta.getServiceName()) != null) {
                RegistryPersistRecord persistRecord = historyRecords.get(registerMeta.getServiceName());
                if (null != persistRecord.getBalanceStrategy()) {
                    defaultLoadBalanceStrategy = persistRecord.getBalanceStrategy();
                }
            }
            // 设置该服务默认的负载均衡的策略
            globalServiceLoadBalance.put(registerMeta.getServiceName(), defaultLoadBalanceStrategy);
            ackCustomBody.setSuccess(true);
            // TODO: 2017/8/21   如果审核通过，则通知相关服务的订阅者
        }

        this.globalProviderChannelMetaMap.put(registerMeta.getAddress(), channel);

        return responseTransporter;
    }


    /**
     * provider端发送的请求，取消对某个服务的提供
     *
     * @param request
     * @param channel
     * @return
     * @throws InterruptedException
     */
    public RemotingTransporter handlerRegisterCancel(RemotingTransporter request, Channel channel) {


        // 准备好ack信息返回个provider，悲观主义，默认返回失败ack，要求provider重新发送请求
//        AckCustomBody ackCustomBody = new AckCustomBody(request.getOpaque(), false);
//        RemotingTransporter responseTransporter = RemotingTransporter.createResponseTransporter(LaopopoProtocol.ACK, ackCustomBody, request.getOpaque());
//
//        // 接收到主体信息
//        PublishServiceCustomBody publishServiceCustomBody = serializerImpl().readObject(request.bytes(), PublishServiceCustomBody.class);
//
//        RegisterMeta meta = RegisterMeta.createRegiserMeta(publishServiceCustomBody,channel);
//
//        handlePublishCancel(meta, channel);
//
//        ackCustomBody.setSuccess(true);
//
//        globalProviderChannelMetaMap.remove(meta.getAddress());
//
//        return responseTransporter;
    }


    /***
     * 服务下线的接口
     *
     * @param meta
     * @param channel
     */
    public void handlePublishCancel(RegisterMeta meta, Channel channel) {
        logger.info("Cancel publish {} on channel{}.", meta, channel);
        //将服务单元和channel解除绑定
        attachPublishCancelEventOnChannel(meta,channel);

        ConcurrentMap<RegisterMeta.Address, RegisterMeta> registerMetaMaps = this.getRegisterMeta(meta.getServiceName());
        if(registerMetaMaps==null||registerMetaMaps.isEmpty()){
            return ;
        }

        synchronized (globalRegisterInfoMap){
            RegisterMeta registerMeta = registerMetaMaps.remove(meta.getAddress());
            if(registerMeta!=null){
                this.getServiceMeta(registerMeta.getAddress()).remove(meta.getServiceName());

                if(registerMeta.getIsReviewed()== ServiceReviewState.PASS_REVIEW){
                    // TODO: 2017/8/22  
                }
            }
        }
    }

    /**
     * 清除channel上绑定的注册信息单元
     *
     * @param registerMeta
     * @param channel
     */
    private void attachPublishCancelEventOnChannel(RegisterMeta registerMeta, Channel channel) {
        Attribute<ConcurrentSet<RegisterMeta>> attr = channel.attr(S_PUBLISH_KEY);
        ConcurrentSet<RegisterMeta> concurrentSet = attr.get();
        if (concurrentSet == null) {
            ConcurrentSet<RegisterMeta> newRegisterMetaSet = new ConcurrentSet<>();
            concurrentSet = attr.setIfAbsent(newRegisterMetaSet);
            if (concurrentSet == null) {
                concurrentSet = newRegisterMetaSet;
            }
        }
        concurrentSet.remove(registerMeta);
    }

    /**
     * 将一个注册信息单元绑定到channel上
     *
     * @param registerMeta
     * @param channel
     */
    private void attachPublishEventOnChannel(RegisterMeta registerMeta, Channel channel) {
        Attribute<ConcurrentSet<RegisterMeta>> attr = channel.attr(S_PUBLISH_KEY);
        ConcurrentSet<RegisterMeta> concurrentSet = attr.get();
        if (concurrentSet == null) {
            ConcurrentSet<RegisterMeta> newRegisterMetaSet = new ConcurrentSet<>();
            concurrentSet = attr.setIfAbsent(newRegisterMetaSet);
            if (concurrentSet == null) {
                concurrentSet = newRegisterMetaSet;
            }
        }
        concurrentSet.add(registerMeta);
    }

    /**
     * 获取一个服务下所有的最小服务单元
     *
     * @param serverName 服务名
     * @return
     */
    private ConcurrentMap<RegisterMeta.Address, RegisterMeta> getRegisterMeta(String serverName) {
        ConcurrentMap<RegisterMeta.Address, RegisterMeta> mateMap = this.globalRegisterInfoMap.get(serverName);
        if (mateMap == null) {
            ConcurrentMap<RegisterMeta.Address, RegisterMeta> newMap = new ConcurrentHashMap<>();
            mateMap = globalRegisterInfoMap.putIfAbsent(serverName, newMap);
            if (mateMap == null) {
                mateMap = newMap;
            }
        }
        return mateMap;
    }

    private ConcurrentSet<String> getServiceMeta(RegisterMeta.Address address) {
        ConcurrentSet<String> serviceMetaSet = globalServiceMetaMap.get(address);
        if (serviceMetaSet == null) {
            ConcurrentSet<String> newServiceMetaSet = new ConcurrentSet<>();
            serviceMetaSet = globalServiceMetaMap.putIfAbsent(address, newServiceMetaSet);
            if (serviceMetaSet == null) {
                serviceMetaSet = newServiceMetaSet;
            }
        }
        return serviceMetaSet;
    }


    /**
     * 是否在历史记录中有服务单元信息
     *
     * @param persistRecord
     * @param address
     * @return
     */
    private boolean isContainChildrenInfo(RegistryPersistRecord persistRecord, RegisterMeta.Address address) {
        if (persistRecord != null && CollectionUtils.isNotEmpty(persistRecord.getProviderInfos())) {
            for (RegistryPersistRecord.PersistProviderInfo info : persistRecord.getProviderInfos()) {
                if (info.getAddress().equals(address)) {
                    return true;
                }
            }
        }

        return false;
    }


}
