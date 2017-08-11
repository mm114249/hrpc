package com.pairs.arch.rpc.remote.idle;

import com.pairs.arch.rpc.remote.model.Heartbeats;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Created on 2017年08月11日15:35
 * <p>
 * Title:[]
 * </p >
 * <p>
 * Description :[]
 * </p >
 * Company:
 *
 * @author [hupeng]
 * @version 1.0
 **/
@ChannelHandler.Sharable
public class ConnectorIdleStateTrigger extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent idleStateEvent= (IdleStateEvent) evt;
            if(idleStateEvent.state()== IdleState.WRITER_IDLE){
                ctx.channel().writeAndFlush(Heartbeats.heartbeatContent());
            }
        }else{
            super.userEventTriggered(ctx, evt);
        }

    }
}
