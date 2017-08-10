package com.pairs.arch.rpc.remote.idle;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Created on 2017年08月10日14:33
 * 服务器端处理读空闲
 * @author [hupeng]
 * @version 1.0
 **/
@ChannelHandler.Sharable
public class AcceptorIdleStateTrigger extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event= (IdleStateEvent) evt;
            if(event.state()== IdleState.READER_IDLE){
                // TODO: 2017/8/10 处理读空闲
            }
        }else{
            super.userEventTriggered(ctx, evt);
        }

    }
}
