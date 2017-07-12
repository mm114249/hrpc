package com.pairs.arch.rpc.server.handler;

import com.pairs.arch.rpc.common.bean.HrpcRequest;
import com.pairs.arch.rpc.common.bean.HrpcResponse;
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

        if(hrpcRequest.getType().shortValue()==HrpcRequest.RequestType.NORMAL.getValue()){
            //正常的rpc调用
            executorService.execute(new ChannelReadHelper(hrpcRequest,channelHandlerContext));
        }else{
            //心跳消息，回复一pong消息
            HrpcResponse response=new HrpcResponse(HrpcRequest.RequestType.HEART);
            response.setRequestId(hrpcRequest.getRequestId());
            channelHandlerContext.channel().writeAndFlush(response);
        }
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        if(logger.isInfoEnabled()){
            logger.info("1111");
        }

        super.exceptionCaught(ctx, cause);
    }
}
