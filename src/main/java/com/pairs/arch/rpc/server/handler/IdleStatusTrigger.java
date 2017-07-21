package com.pairs.arch.rpc.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 2017年07月18日 19:07
 * <P>
 * Title:[]
 * </p>
 * <p>
 * Description :[]
 * </p>
 * Company:
 *
 * @author [hupeng]
 * @version 1.0
 **/
public class IdleStatusTrigger extends ChannelInboundHandlerAdapter {
    public static AttributeKey<Integer> countKey = AttributeKey.newInstance("count");
    private static final Logger logger= LoggerFactory.getLogger(IdleStatusTrigger.class);
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt.getClass().isAssignableFrom(IdleStateEvent.class)) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                Channel channel = ctx.channel();
                logger.info("readre_idle message. channel id is :"+channel.id().asShortText());
                Attribute<Integer> countAttribute = channel.attr(countKey);
                Integer count = countAttribute.get();
                if (count == null) {
                    countAttribute.set(1);
                    return;
                } else {
                    if (count >= 1) {
                        ctx.fireChannelInactive();
                    }else{
                        countAttribute.set(++count);
                    }
                }

            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
