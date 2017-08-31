package com.pairs.arch.rpc.registry.defaultRegistry;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pairs.arch.rpc.common.protocol.HrpcProtocol;
import com.pairs.arch.rpc.common.rpc.registry.LoadBalanceStrategy;
import com.pairs.arch.rpc.common.rpc.registry.RegisterMeta;
import com.pairs.arch.rpc.common.rpc.registry.RegistryPersistRecord;
import com.pairs.arch.rpc.common.rpc.registry.ServiceReviewState;
import com.pairs.arch.rpc.common.serializer.SerializerHolder;
import com.pairs.arch.rpc.common.transport.body.ACKCustomBody;
import com.pairs.arch.rpc.common.transport.body.ManagerServiceCustomBody;
import com.pairs.arch.rpc.common.transport.body.PublishServiceCustomBody;
import com.pairs.arch.rpc.common.transport.body.SubcribeResultCustomBody;
import com.pairs.arch.rpc.common.transport.body.SubscribeRequestCustomBody;
import com.pairs.arch.rpc.common.util.PersistUtils;
import com.pairs.arch.rpc.remote.model.RemotingTransporter;
import com.sun.org.apache.bcel.internal.generic.BranchHandle;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ConcurrentSet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

    private DefaultRegistryServer defaultRegistryServer;

    //消费者channel上绑定的服务名
    private AttributeKey<ConcurrentSet<String>> S_SUBSCRIBED_KEY=AttributeKey.valueOf("server.subscribed");
    //channel绑定的服务单元
    private AttributeKey<ConcurrentSet<RegisterMeta>> S_PUBLISH_KEY = AttributeKey.valueOf("server.published");
    //服务列表 key服务名 value服务单元map
    private ConcurrentMap<String, ConcurrentMap<RegisterMeta.Address, RegisterMeta>> globalRegisterInfoMap = new ConcurrentHashMap<>();
    // 指定节点都注册了哪些服务
    private final ConcurrentMap<RegisterMeta.Address, ConcurrentSet<String>> globalServiceMetaMap = new ConcurrentHashMap<RegisterMeta.Address, ConcurrentSet<String>>();
    //每个服务的历史记录  key服务名
    private ConcurrentMap<String, RegistryPersistRecord> historyRecords = new ConcurrentHashMap<>();
    //每个服务对应的负载策略
    private final ConcurrentMap<String, LoadBalanceStrategy> globalServiceLoadBalance = new ConcurrentHashMap<String, LoadBalanceStrategy>();
    // 提供者某个地址对应的channel
    private final ConcurrentMap<RegisterMeta.Address, Channel> globalProviderChannelMetaMap = new ConcurrentHashMap<RegisterMeta.Address, Channel>();
    //某个服务 订阅它的消费者channel集合
    private final ConcurrentMap<String,ConcurrentSet<Channel>> globalConsumerMetaMap=new ConcurrentHashMap();

    public RegistryProviderManager(DefaultRegistryServer defaultRegistryServer) {
        this.defaultRegistryServer = defaultRegistryServer;
    }

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
                    persistProviderInfo.setIsReviewed(this.defaultRegistryServer.getRegistryServerConfig().getDefaultReviewState());

                    record = new RegistryPersistRecord();
                    record.setServiceName(registerMeta.getServiceName());
                    record.setBalanceStrategy(this.defaultRegistryServer.getRegistryServerConfig().getDefaultLoadBalanceStrategy());
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

            //默认的负载均衡策略
            LoadBalanceStrategy defaultLoadBalanceStrategy = this.defaultRegistryServer.getRegistryServerConfig().getDefaultLoadBalanceStrategy();

            if (historyRecords.get(registerMeta.getServiceName()) != null) {
                RegistryPersistRecord persistRecord = historyRecords.get(registerMeta.getServiceName());
                if (null != persistRecord.getBalanceStrategy()) {
                    defaultLoadBalanceStrategy = persistRecord.getBalanceStrategy();
                }
            }
            //将该服务默认的负载均衡策略加入到缓存中
            globalServiceLoadBalance.put(registerMeta.getServiceName(), defaultLoadBalanceStrategy);
            ackCustomBody.setSuccess(true);
            //通知订阅者更新新注册的服务单元
            this.defaultRegistryServer.getRegistryConsumerManager().notifyMacthedSubscriber(registerMeta, globalServiceLoadBalance.get(registerMeta.getServiceName()));
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
        ACKCustomBody ackCustomBody = new ACKCustomBody(request.getOpaque(), false, "");
        RemotingTransporter responseTransporter = RemotingTransporter.createResponseTransport(HrpcProtocol.ACK, ackCustomBody, request.getOpaque());
        // 接收到主体信息
        PublishServiceCustomBody publishServiceCustomBody = SerializerHolder.serializerImpl().readObject(request.bytes(), PublishServiceCustomBody.class);
        RegisterMeta meta = RegisterMeta.createRegiserMeta(publishServiceCustomBody, channel);
        handlePublishCancel(meta, channel);
        ackCustomBody.setSuccess(true);
        globalProviderChannelMetaMap.remove(meta.getAddress());
        return responseTransporter;
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
        attachPublishCancelEventOnChannel(meta, channel);

        ConcurrentMap<RegisterMeta.Address, RegisterMeta> registerMetaMaps = this.getRegisterMeta(meta.getServiceName());
        if (registerMetaMaps == null || registerMetaMaps.isEmpty()) {
            return;
        }

        synchronized (globalRegisterInfoMap) {
            RegisterMeta registerMeta = registerMetaMaps.remove(meta.getAddress());
            if (registerMeta != null) {
                this.getServiceMeta(registerMeta.getAddress()).remove(meta.getServiceName());

                if (registerMeta.getIsReviewed() == ServiceReviewState.PASS_REVIEW) {
                    try {
                        this.defaultRegistryServer.getRegistryConsumerManager().notifyMacthedSubscriberCancel(registerMeta);
                    } catch (InterruptedException e) {
                        logger.error("", e);
                    }
                }
            }
        }
    }

    /**
     * 消费者订阅服务
     * @param request
     * @param channel
     * @return
     */
    public RemotingTransporter handleSubscribe(RemotingTransporter request,Channel channel){
        SubcribeResultCustomBody subcribeResultCustomBody=new SubcribeResultCustomBody();
        RemotingTransporter response = RemotingTransporter.createResponseTransport(HrpcProtocol.SUBCRIBE_RESULT, subcribeResultCustomBody, request.getOpaque());

        SubscribeRequestCustomBody requestCustomBody = SerializerHolder.serializerImpl().readObject(request.bytes(), SubscribeRequestCustomBody.class);
        String serviceName=requestCustomBody.getServiceName();
        //将消费channel加入到channelgroup中去
        this.defaultRegistryServer.getRegistryConsumerManager().getSubscriberChannels().add(channel);

        if(CollectionUtils.isEmpty(globalConsumerMetaMap.get(serviceName))){
            ConcurrentSet<Channel> _set=new ConcurrentSet<>();
            globalConsumerMetaMap.put(serviceName,_set);
        }
        // 存储消费者信息
        globalConsumerMetaMap.get(serviceName).add(channel);

        //将服务名和消费者channel绑定
        attachSubscribeEventOnChannel(serviceName,channel);

        ConcurrentMap<RegisterMeta.Address, RegisterMeta> registerMeta = this.getRegisterMeta(serviceName);
        if(registerMeta.isEmpty()){
            return response;
        }

        this.buildSubcribeResultCustomBody(registerMeta,subcribeResultCustomBody);
        return response;
    }


    /**
     * 服务管理接口,用来处理服务审核、降级、修改权重等
     * @param request
     * @param channel
     * @return
     */
    public RemotingTransporter handleManager(RemotingTransporter request,Channel channel) throws InterruptedException {
        ManagerServiceCustomBody manager = SerializerHolder.serializerImpl().readObject(request.bytes(), ManagerServiceCustomBody.class);
        switch (manager.getManagerServiceRequestType()){
            case REVIEW:
                this.handleReview(manager.getSerivceName(),manager.getAddress(),request.getOpaque(),manager.getServiceReviewState());
                break;
            case DEGRADE:
                this.handleDegradeService(request,channel);
                break;
        }
    }



    /**
     * 持久化注册信息到硬盘
     * 优先从globalRegisterInfoMap 持久化
     * 如果globalRegisterInfoMap没有的信息单元就从historyRecords持久老版本信息
     */
    public void persistServiceInfo() {
        Map<String, RegistryPersistRecord> persistRecordMap = Maps.newHashMap();
        if (globalRegisterInfoMap.size() > 0) {
            for (Map.Entry<String, ConcurrentMap<RegisterMeta.Address, RegisterMeta>> entry : globalRegisterInfoMap.entrySet()) {
                RegistryPersistRecord record = new RegistryPersistRecord();
                record.setServiceName(entry.getKey());
                record.setBalanceStrategy(globalServiceLoadBalance.get(entry.getKey()));

                for (Map.Entry<RegisterMeta.Address, RegisterMeta> providerInfo : entry.getValue().entrySet()) {
                    RegistryPersistRecord.PersistProviderInfo o = new RegistryPersistRecord.PersistProviderInfo();
                    o.setAddress(providerInfo.getKey());
                    o.setIsReviewed(providerInfo.getValue().getIsReviewed());
                    record.getProviderInfos().add(o);
                }
                persistRecordMap.put(entry.getKey(), record);
            }
        }

        if(historyRecords.size()>0){
            for(Map.Entry<String, RegistryPersistRecord> entity:historyRecords.entrySet()){
                if(!persistRecordMap.containsKey(entity.getKey())){
                    //全局中没有直接从历史中获取
                    persistRecordMap.put(entity.getKey(),entity.getValue());
                }else {
                    //可能需要合并的信息，合并原则，如果同地址的审核策略以globalRegisterInfoMap为准，如果不同地址，则合并信息
                    List<RegistryPersistRecord.PersistProviderInfo> globalList = persistRecordMap.get(entity.getKey()).getProviderInfos();
                    List<RegistryPersistRecord.PersistProviderInfo> historyList=entity.getValue().getProviderInfos();
                    for (RegistryPersistRecord.PersistProviderInfo hisInfo : historyList) {
                        boolean exits=false;
                        for (RegistryPersistRecord.PersistProviderInfo globalInfo : globalList) {
                            if(globalInfo.getAddress().equals(hisInfo.getAddress())){
                                exits=true;
                                break;
                            }
                        }

                        if(!exits){
                            globalList.add(hisInfo);
                        }

                    }

                }
            }
        }

        if(MapUtils.isNotEmpty(persistRecordMap)){
            String json= JSONObject.toJSONString(persistRecordMap);
            try {
                PersistUtils.string2File(json,this.defaultRegistryServer.getRegistryServerConfig().getStorePathRootDir());
            } catch (IOException e) {
                e.printStackTrace();
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
     * 将服务名和消费者channel绑定
     * @param serviceName
     * @param channel
     */
    private void attachSubscribeEventOnChannel(String serviceName,Channel channel){
        ConcurrentSet<String> serviceNameSet = channel.attr(S_SUBSCRIBED_KEY).get();
        if(serviceNameSet==null){
            ConcurrentSet<String>  _temp=new ConcurrentSet<>();
            serviceNameSet= channel.attr(S_SUBSCRIBED_KEY).setIfAbsent(_temp);
            if(serviceNameSet==null){
                serviceNameSet=_temp;
            }
        }
        serviceNameSet.add(serviceName);

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

    /**
     * 组装消费者订阅服务的服务单元列表
     * @param maps
     * @param subcribeResultCustomBody
     */
    private void buildSubcribeResultCustomBody(ConcurrentMap<RegisterMeta.Address, RegisterMeta> maps, SubcribeResultCustomBody subcribeResultCustomBody) {
        Collection<RegisterMeta> values = maps.values();
        if (values != null && values.size() > 0) {
            List<RegisterMeta> registerMetas = new ArrayList<RegisterMeta>();
            for (RegisterMeta meta : values) {
                // 判断是否人工审核过，审核过的情况下，组装给consumer的响应主体，返回个consumer
                if (meta.getIsReviewed() == ServiceReviewState.PASS_REVIEW) {
                    registerMetas.add(meta);
                }
            }
            subcribeResultCustomBody.setRegisterMeta(registerMetas);
        }
    }


    //---------------------处理服务管理业务

    private RemotingTransporter handleReview(String serviceName, RegisterMeta.Address address,long requestId,ServiceReviewState reviewState){
        ACKCustomBody ackCustomBody=new ACKCustomBody(requestId,false,null);
        RemotingTransporter response=RemotingTransporter.createResponseTransport(HrpcProtocol.ACK,ackCustomBody,requestId);
        try {
            ConcurrentMap<RegisterMeta.Address, RegisterMeta> metaMap = this.getRegisterMeta(serviceName);
            synchronized (globalProviderChannelMetaMap){
                if(address!=null){
                    if(metaMap.isEmpty()){
                        return response;
                    }
                    RegisterMeta registerMeta = metaMap.get(address);
                    if(registerMeta!=null){
                        ackCustomBody.setSuccess(true);
                        ServiceReviewState oldState=registerMeta.getIsReviewed();
                        registerMeta.setIsReviewed(reviewState);

                        notifyConsumer(reviewState,oldState,registerMeta,serviceName);
                    }
                }else{
                    //adress 不填说明要改全部服务的审核状态
                    if(MapUtils.isNotEmpty(metaMap)){
                        ackCustomBody.setSuccess(true);
                        for (Map.Entry<RegisterMeta.Address, RegisterMeta> entity : metaMap.entrySet()) {
                            ServiceReviewState oldState=entity.getValue().getIsReviewed();
                            entity.getValue().setIsReviewed(reviewState);
                            notifyConsumer(reviewState,oldState,entity.getValue(),serviceName);
                        }
                    }


                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            return response;
        }
    }


    /**
     * 服务降级
     * @param request
     * @param channel
     * @return
     */
    private RemotingTransporter handleDegradeService(RemotingTransporter request, Channel channel) throws InterruptedException {
        ACKCustomBody ackCustomBody=new ACKCustomBody(request.getOpaque(),false,null);
        RemotingTransporter response = RemotingTransporter.createResponseTransport(HrpcProtocol.ACK, ackCustomBody, ackCustomBody.getRequestId());
        ManagerServiceCustomBody managerBody=SerializerHolder.serializerImpl().readObject(request.bytes(),ManagerServiceCustomBody.class);

        RegisterMeta.Address address=null;
        synchronized (globalRegisterInfoMap){
            ConcurrentMap<RegisterMeta.Address, RegisterMeta> registerMetaMap = this.getRegisterMeta(managerBody.getSerivceName());
            if(registerMetaMap.isEmpty()){
                return response;
            }
            RegisterMeta registerMeta = registerMetaMap.get(managerBody.getAddress());
            if(registerMeta.getIsReviewed()!=ServiceReviewState.PASS_REVIEW){
                return response;
            }
            address=registerMeta.getAddress();
        }

        if(address==null){
            return response;
        }else {
            Channel metaChannel = globalProviderChannelMetaMap.get(address);
            if(metaChannel==null){
                return request;
            }
            request.setCode(HrpcProtocol.DEGRADE_SERVICE);
            request.setCustomHeader(managerBody);
            return defaultRegistryServer.getNettyRemetingServer().invokeSync(metaChannel,request,3000l);
        }
    }


    private void notifyConsumer(ServiceReviewState currentState,ServiceReviewState oldState,RegisterMeta registerMeta,String serviceName) throws InterruptedException {
        if(currentState!=oldState){
            switch (currentState){
                case PASS_REVIEW:
                    this.defaultRegistryServer.getRegistryConsumerManager().notifyMacthedSubscriber(registerMeta,globalServiceLoadBalance.get(serviceName));
                    break;
                case FORBIDDEN:
                    this.defaultRegistryServer.getRegistryConsumerManager().notifyMacthedSubscriberCancel(registerMeta);
                    break;
                default:break;
            }
        }
    }




    public ConcurrentMap<String, RegistryPersistRecord> getHistoryRecords() {
        return historyRecords;
    }

    public void setHistoryRecords(ConcurrentMap<String, RegistryPersistRecord> historyRecords) {
        this.historyRecords = historyRecords;
    }
}
