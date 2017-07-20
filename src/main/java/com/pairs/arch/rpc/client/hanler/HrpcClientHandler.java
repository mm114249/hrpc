package com.pairs.arch.rpc.client.hanler;

import com.pairs.arch.rpc.client.HrpcConnect;
import com.pairs.arch.rpc.client.ResponseWrap;
import com.pairs.arch.rpc.client.SyncLock;
import com.pairs.arch.rpc.client.discovery.ServerDiscovery;
import com.pairs.arch.rpc.common.bean.HrpcRequest;
import com.pairs.arch.rpc.common.bean.HrpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
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
@ChannelHandler.Sharable
public class HrpcClientHandler extends SimpleChannelInboundHandler<HrpcResponse> {
    private Logger logger=Logger.getLogger(HrpcClientHandler.class);
    private Integer idelMax;//最大空闲连接检查次数,当连接检查次数达到最大值得时候,说明该channel已经长时间没有发送消息了,需要主动关闭该链路
    private ServerDiscovery serverDiscovery;

    public HrpcClientHandler(Integer idelMax,ServerDiscovery serverDiscovery){
        this.idelMax =idelMax;
        this.serverDiscovery=serverDiscovery;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HrpcResponse hrpcResponse) throws Exception {

        if(hrpcResponse.getType().shortValue()== HrpcRequest.RequestType.NORMAL.getValue()){
            CountDownLatch latch= SyncLock.getInstance().get(hrpcResponse.getRequestId());
            if(latch!=null){
                ResponseWrap.getInstance().put(hrpcResponse.getRequestId(),hrpcResponse);
                latch.countDown();
            }
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        serverDiscovery.removeConnect(ctx.channel());
    }

//    /**
//     * 空闲链路检查，当空闲心跳检查
//     * @param ctx
//     * @param evt
//     * @throws Exception
//     */
//    @Override
//    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//
//        if (evt.getClass().isAssignableFrom(IdleStateEvent.class)) {
//            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
//            if(idleStateEvent.state() == IdleState.WRITER_IDLE){
//                HrpcConnect connect= serverDiscovery.getConnect(ctx.channel());
//                if(connect.getHeartMax().get()>2){
//                    //累计发送3次心跳,表示服务器端不可,客户端主动关闭channel
//                    HrpcConnect hrpcConnect = serverDiscovery.getConnect(ctx.channel());
//                    String address=hrpcConnect.getAddress();
//                    serverDiscovery.removeConnect(ctx.channel());
//                    ctx.channel().close();
//                    //然后进行服务重新连接,重连10次
//                    for(int i=0;i<10;i++){
//                        Bootstrap bootstrap = hrpcConnect.getBootstrap();
//                        ChannelFuture future=bootstrap.connect().sync();
//                        if(future.isSuccess()){
//                            HrpcConnect c = new HrpcConnect(address, future.channel(), bootstrap);
//                            serverDiscovery.addConnect(c);
//                            break;
//                        }
//                    }
//                    return;
//                }
//                //读空闲,发送一次心跳检查
//                HrpcRequest request=new HrpcRequest(HrpcRequest.RequestType.HEART);
//                String uuid= UUID.randomUUID().toString().replaceAll("-","");
//                request.setRequestId(uuid);
//                ctx.channel().writeAndFlush(request);
//                if(connect!=null){
//                    connect.getHeartMax().incrementAndGet();
//                }
//            }
//        }
//    }

}
