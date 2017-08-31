package com.pairs.arch.rpc.registry.defaultRegistry;

import com.pairs.arch.rpc.common.rpc.registry.RegisterMeta;
import com.pairs.arch.rpc.remote.model.NettyChannelnactiveProcessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ConcurrentSet;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 2017年08月28日18:01
 * <p/>
 * Title:[]
 * </p >
 * <p/>
 * Description :[]
 * </p >
 * Company:武汉灵达科技有限公司
 *
 * @author [hupeng]
 * @version 1.0
 **/
public class DefaultRegistryChannelInactiveProcessor implements NettyChannelnactiveProcessor {
    private Logger logger= LoggerFactory.getLogger(this.getClass());

    private DefaultRegistryServer defaultRegistryServer;
    private AttributeKey<ConcurrentSet<RegisterMeta>> S_PUBLISH_KEY=AttributeKey.valueOf("server.published");

    public DefaultRegistryChannelInactiveProcessor(DefaultRegistryServer defaultRegistryServer) {
        this.defaultRegistryServer = defaultRegistryServer;
    }

    @Override
    public void processChannelInacitve(ChannelHandlerContext ctx) throws InterruptedException {
        Channel channel=ctx.channel();
        ConcurrentSet<RegisterMeta> set=channel.attr(S_PUBLISH_KEY).get();
        if(CollectionUtils.isEmpty(set)){
            logger.debug("registerMetaSet is empty");
            return;
        }

        for (RegisterMeta registerMeta : set) {
            this.defaultRegistryServer.getRegistryConsumerManager().notifyMacthedSubscriberCancel(registerMeta);
        }
    }
}
