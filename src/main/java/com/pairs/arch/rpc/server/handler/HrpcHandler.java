package com.pairs.arch.rpc.server.handler;

import com.pairs.arch.rpc.common.bean.HrpcRequest;
import com.pairs.arch.rpc.common.bean.HrpcResponse;
import com.pairs.arch.rpc.server.helper.ChannelReadHelper;
import com.pairs.arch.rpc.server.util.ConnectTimeException;
import com.sun.org.apache.regexp.internal.RE;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hupeng on 2017/3/28.
 */
public class HrpcHandler extends SimpleChannelInboundHandler<HrpcRequest> {

    private Logger logger=Logger.getLogger(HrpcHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HrpcRequest hrpcRequest) throws Exception {

        if(hrpcRequest.getType().shortValue()==HrpcRequest.RequestType.NORMAL.getValue()){
            logger.info("正常rpc调用");
            //正常的rpc调用
            ChannelReadHelper.excute(hrpcRequest,ctx);
        }else{
            //心跳消息
            logger.info("心跳消息");
            Channel channel=ctx.channel();
            Attribute<Integer> attribute=channel.attr(IdleStatusTrigger.countKey);
            if(attribute!=null){
                attribute.set(0);
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(logger.isDebugEnabled()){
            logger.debug("channel close id is --->"+ctx.channel().id().asShortText());
        }
        ctx.channel().close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
            logger.info("channel active id is --->"+ctx.channel().id().asShortText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("异常捕获:-->",cause);
    }
}
