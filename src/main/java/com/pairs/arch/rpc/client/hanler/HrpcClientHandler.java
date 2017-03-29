package com.pairs.arch.rpc.client.hanler;

import com.pairs.arch.rpc.client.ResponseWrap;
import com.pairs.arch.rpc.client.SyncLock;
import com.pairs.arch.rpc.common.bean.HrpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.CountDownLatch;

/**
 * Created by hupeng on 2017/3/24.
 */
public class HrpcClientHandler extends SimpleChannelInboundHandler<HrpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HrpcResponse hrpcResponse) throws Exception {
        CountDownLatch latch= SyncLock.getInstance().get(hrpcResponse.getRequestId());
        if(latch!=null){
            ResponseWrap.getInstance().put(hrpcResponse.getRequestId(),hrpcResponse);
            latch.countDown();
        }
    }

}
