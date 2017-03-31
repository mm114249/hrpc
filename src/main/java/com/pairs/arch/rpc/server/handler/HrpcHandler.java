package com.pairs.arch.rpc.server.handler;

import com.pairs.arch.rpc.common.bean.HrpcRequest;
import com.pairs.arch.rpc.server.helper.ChannelReadHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hupeng on 2017/3/28.
 */
public class HrpcHandler extends SimpleChannelInboundHandler<HrpcRequest> {

    private Logger logger=Logger.getLogger(HrpcHandler.class);

    private ExecutorService executorService= Executors.newFixedThreadPool(4);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HrpcRequest hrpcRequest) throws Exception {
        executorService.execute(new ChannelReadHelper(hrpcRequest,channelHandlerContext));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(logger.isDebugEnabled()){
            logger.debug("channel close id is --->"+ctx.channel().id().asShortText());
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        if(logger.isDebugEnabled()){
            logger.debug("channel active id is --->"+ctx.channel().id().asShortText());
        }
    }


}
