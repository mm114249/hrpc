package com.pairs.arch.rpc.client.hanler;

import com.pairs.arch.rpc.common.bean.HrpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.UUID;

/**
 * Created on 2017年07月19日 11:22
 * <P>
 * Title:[]
 * </p>
 * <p>
 * Description :[]
 * </p>
 * Company:武汉灵达科技有限公司
 *
 * @author [hupeng]
 * @version 1.0
 **/
public class ClientIdleTrigger extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt.getClass().isAssignableFrom(IdleStateEvent.class)){
            IdleStateEvent idleStateEvent= (IdleStateEvent) evt;
            IdleState idleState = idleStateEvent.state();
            if(idleState==IdleState.WRITER_IDLE){
                //读空闲,发送一次心跳检查
                HrpcRequest request=new HrpcRequest(HrpcRequest.RequestType.HEART);
                String uuid= UUID.randomUUID().toString().replaceAll("-","");
                request.setRequestId(uuid);
                ctx.channel().writeAndFlush(request);
            }
        }

        super.userEventTriggered(ctx, evt);
    }
}
