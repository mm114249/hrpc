package com.pairs.arch.rpc.client.hanler;

import com.pairs.arch.rpc.client.HrpcConnect;
import com.pairs.arch.rpc.client.ResponseWrap;
import com.pairs.arch.rpc.client.SyncLock;
import com.pairs.arch.rpc.client.registry.ServerDiscovery;
import com.pairs.arch.rpc.common.bean.HrpcRequest;
import com.pairs.arch.rpc.common.bean.HrpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.log4j.Logger;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * Created by hupeng on 2017/3/24.
 */
public class HrpcClientHandler extends SimpleChannelInboundHandler<HrpcResponse> {


    private Logger logger=Logger.getLogger(HrpcClientHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HrpcResponse hrpcResponse) throws Exception {

        if(hrpcResponse.getType().shortValue()== HrpcRequest.RequestType.NORMAL.getValue()){
            CountDownLatch latch= SyncLock.getInstance().get(hrpcResponse.getRequestId());
            if(latch!=null){
                ResponseWrap.getInstance().put(hrpcResponse.getRequestId(),hrpcResponse);
                latch.countDown();
            }
        }else{
            //接收到服务器的pong消息，计数器减一
            HrpcConnect connect=ServerDiscovery.getInstance().getConnect(channelHandlerContext.channel());
            if(connect!=null){
                connect.getIdelTotal().incrementAndGet();
            }
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ServerDiscovery.getInstance().removeServer(ctx.channel());
    }

    /**
     * 空闲链路检查，当空闲心跳检查
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt.getClass().isAssignableFrom(IdleStateEvent.class)) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.ALL_IDLE) {
                HrpcConnect connect=ServerDiscovery.getInstance().getConnect(ctx.channel());
                if(connect==null){
                    return;
                }

                int count = connect.getIdelTotal().get();
                if (count > 3) {
                    //当空闲链路检查积累到三次的时候，认为服务器不可达，关闭channel
                    ctx.close();
                }else{
                    connect.getIdelTotal().incrementAndGet();

                    if(logger.isInfoEnabled()){
                        logger.info("开始发送心跳包，第"+connect.getIdelTotal().get()+"次");
                    }
                    //发送一次心跳检查包
                    HrpcRequest request=new HrpcRequest(HrpcRequest.RequestType.HEART);
                    String uuid= UUID.randomUUID().toString().replaceAll("-","");
                    request.setRequestId(uuid);
                    ctx.channel().writeAndFlush(request);
                }
            }
        }
    }

}
