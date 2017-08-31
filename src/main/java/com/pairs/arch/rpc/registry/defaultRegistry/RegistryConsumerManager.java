package com.pairs.arch.rpc.registry.defaultRegistry;

import com.google.common.collect.Lists;
import com.pairs.arch.rpc.common.protocol.HrpcProtocol;
import com.pairs.arch.rpc.common.rpc.registry.LoadBalanceStrategy;
import com.pairs.arch.rpc.common.rpc.registry.RegisterMeta;
import com.pairs.arch.rpc.common.transport.body.ACKCustomBody;
import com.pairs.arch.rpc.common.transport.body.SubcribeResultCustomBody;
import com.pairs.arch.rpc.remote.model.RemotingTransporter;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.ConcurrentSet;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 2017年08月22日11:45
 * <p/>
 * Title:[]
 * </p >
 * <p/>
 * Description :[消费端管理]
 * </p >
 * Company:
 *
 * @author [hupeng]
 * @version 1.0
 **/
public class RegistryConsumerManager {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private RegistryServerConfig registryServerConfig;
    private DefaultRegistryServer defaultRegistryServer;


    private ChannelGroup subscriberChannels = new DefaultChannelGroup("subscriber", GlobalEventExecutor.INSTANCE);
    private static final AttributeKey<ConcurrentSet<String>> S_SUBSCRIBED_KEY = AttributeKey.valueOf("server.subscribed");
    private ConcurrentSet<MessageNonAck> nonAcks = new ConcurrentSet<MessageNonAck>();


    public RegistryConsumerManager(DefaultRegistryServer defaultRegistryServer) {
        this.defaultRegistryServer = defaultRegistryServer;
    }

    /**
     * 通知相关的订阅者服务的信息
     *
     * @param meta
     * @param loadBalanceStrategy
     * @throws InterruptedException
     */
    public void notifyMacthedSubscriber(final RegisterMeta meta, LoadBalanceStrategy loadBalanceStrategy) throws InterruptedException {

        // 构建订阅通知的主体传输对象
        SubcribeResultCustomBody subcribeResultCustomBody = new SubcribeResultCustomBody();
        buildSubcribeResultCustomBody(meta, subcribeResultCustomBody, loadBalanceStrategy);
        // 传送给consumer对象的RemotingTransporter
        RemotingTransporter sendConsumerRemotingTrasnporter = RemotingTransporter.createRequestTransprot(HrpcProtocol.SUBCRIBE_RESULT, subcribeResultCustomBody);
        pushMessageToConsumer(sendConsumerRemotingTrasnporter, meta.getServiceName());
    }

    /**
     * 通知订阅者服务关闭
     * @param registerMeta
     * @throws InterruptedException
     */
    public void notifyMacthedSubscriberCancel(final RegisterMeta registerMeta) throws InterruptedException {
        SubcribeResultCustomBody subcribeResultCustomBody = new SubcribeResultCustomBody();
        this.buildSubcribeResultCustomBody(registerMeta, subcribeResultCustomBody, null);
        RemotingTransporter remotingTransporter = RemotingTransporter.createRequestTransprot(HrpcProtocol.SUBCRIBE_SERVICE_CANCEL, subcribeResultCustomBody);
        pushMessageToConsumer(remotingTransporter,registerMeta.getServiceName());
    }

    /**
     * 检查失败队列中重发的消息,重发并清空
     * @throws InterruptedException
     */
    public void checkSendFailMessage() throws InterruptedException {
        ConcurrentSet<MessageNonAck> set=nonAcks;
        nonAcks.clear();
        if(CollectionUtils.isNotEmpty(set)){
            for (MessageNonAck messageNonAck : set) {
                pushMessageToConsumer(messageNonAck.getMsg(),messageNonAck.getServiceName());
            }
        }
    }


    private void buildSubcribeResultCustomBody(RegisterMeta registerMeta, SubcribeResultCustomBody subcribeResultCustomBody, LoadBalanceStrategy loadBalanceStrategy) {
        LoadBalanceStrategy defaultLoadBalanceStrategy = this.registryServerConfig.getDefaultLoadBalanceStrategy();
        subcribeResultCustomBody.setRegisterMeta(Lists.newArrayList(registerMeta));
        LoadBalanceStrategy l = loadBalanceStrategy == null ? defaultLoadBalanceStrategy : loadBalanceStrategy;
        subcribeResultCustomBody.setLoadBalanceStrategy(l);
    }

    private void pushMessageToConsumer(RemotingTransporter remotingTransporter, String serviceName) throws InterruptedException {
        if (!subscriberChannels.isEmpty()) {
            for (Channel subscriberChannel : subscriberChannels) {
                if (isChannelSubscribeOnServiceMeta(serviceName, subscriberChannel)) {
                    RemotingTransporter responseTransporter = this.defaultRegistryServer.getNettyRemetingServer().invokeSync(subscriberChannel, remotingTransporter, 3000l);
                    //如果返回对象为空,说明请求超时,需要重新发送
                    if (responseTransporter == null) {
                        logger.warn("push consumer message time out,need send again");
                        MessageNonAck messageNonAck = new MessageNonAck(remotingTransporter, subscriberChannel, serviceName);
                        nonAcks.add(messageNonAck);
                        return;
                    }

                    ACKCustomBody ackCustomBody = (ACKCustomBody) responseTransporter.getCustomHeader();
                    if (!ackCustomBody.getIsSuccess()) {
                        logger.warn("consumer fail handler this message");
                        MessageNonAck messageNonAck = new MessageNonAck(remotingTransporter, subscriberChannel, serviceName);
                        nonAcks.add(messageNonAck);
                    }

                }
            }
        }
    }

    private boolean isChannelSubscribeOnServiceMeta(String serviceName, Channel channel) {
        Attribute<ConcurrentSet<String>> attr = channel.attr(S_SUBSCRIBED_KEY);
        ConcurrentSet<String> set = attr.get();
        return set != null & set.contains(serviceName);
    }

    static class MessageNonAck {
        private final long id;
        private final String serviceName;
        private final RemotingTransporter msg;
        private final Channel channel;

        public MessageNonAck(RemotingTransporter msg, Channel channel, String serviceName) {
            this.msg = msg;
            this.channel = channel;
            this.serviceName = serviceName;

            id = msg.getOpaque();
        }

        public long getId() {
            return id;
        }

        public String getServiceName() {
            return serviceName;
        }

        public RemotingTransporter getMsg() {
            return msg;
        }

        public Channel getChannel() {
            return channel;
        }
    }


    public ChannelGroup getSubscriberChannels() {
        return subscriberChannels;
    }

    public void setSubscriberChannels(ChannelGroup subscriberChannels) {
        this.subscriberChannels = subscriberChannels;
    }
}
