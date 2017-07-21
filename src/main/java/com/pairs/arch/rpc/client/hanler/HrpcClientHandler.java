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
    private ServerDiscovery serverDiscovery;

    public HrpcClientHandler(ServerDiscovery serverDiscovery){
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

}
