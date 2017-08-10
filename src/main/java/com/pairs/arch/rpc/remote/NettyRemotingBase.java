package com.pairs.arch.rpc.remote;

import com.pairs.arch.rpc.common.protocol.HrpcProtocol;
import com.pairs.arch.rpc.common.util.Pair;
import com.pairs.arch.rpc.remote.model.NettyChannelnactiveProcessor;
import com.pairs.arch.rpc.remote.model.NettyRequestProcessor;
import com.pairs.arch.rpc.remote.model.RemotingResponse;
import com.pairs.arch.rpc.remote.model.RemotingTransporter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 2017年08月09日10:47
 * 提供给客户端和服务器端的处理父类
 * @author [hupeng]
 * @version 1.0
 **/
public abstract class NettyRemotingBase {
    private static final Logger logger = LoggerFactory.getLogger(NettyRemotingBase.class);
    //返回的结果篮子,key是request的opaque
    protected final ConcurrentHashMap<Long,RemotingResponse> responseTable=new ConcurrentHashMap<Long,RemotingResponse>(256);
    //如果使用者没有指定处理器,默认使用该系统内置的处理器
    protected Pair<NettyRequestProcessor,ExecutorService> defaultRequestProcess;
    //同上
    protected Pair<NettyChannelnactiveProcessor,ExecutorService> defaultChannelInactiveProcessor;
    protected ExecutorService publicExecutor= Executors.newFixedThreadPool(4, new ThreadFactory() {
        private AtomicInteger index=new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"NettyClientPublicExecutor_" + this.index.incrementAndGet());
        }
    });
    /**
     * 不同的请求类型使用不同的处理器
     *  1订阅服务2发布服务
     *  key 服务类型
     */
    protected Map<Byte,Pair<NettyRequestProcessor,ExecutorService>> processorTable=new HashMap<Byte,Pair<NettyRequestProcessor,ExecutorService>>(64);

    /**
     * 客户端发送请求,远端调用的具体实现
     * @param channel
     * @param request
     * @param timeoutMillis
     * @return
     * @throws InterruptedException
     */
    public RemotingTransporter invokeSyncImpl(final Channel channel, final RemotingTransporter request,final long timeoutMillis) throws InterruptedException {
        try{
            //构建一个响应对象
            final RemotingResponse remotingResponse=new RemotingResponse(request.getOpaque(),timeoutMillis,null);
            //将响应对象加入篮子,等待rpc请求完成去修改篮子中对象的状态和释放countdown
            responseTable.put(request.getOpaque(),remotingResponse);
            //发请求
            channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(channelFuture.isSuccess()){
                        remotingResponse.setSendRequestOK(true);
                        return;
                    }else{
                        remotingResponse.setSendRequestOK(false);
                    }
                    remotingResponse.setCause(channelFuture.cause());
                    remotingResponse.putResponse(null);
                }
            });

            RemotingTransporter remotingTransporter = remotingResponse.waitResponse();
            if(remotingResponse==null){
                if(remotingResponse.isSendRequestOK()){
                    // TODO: 2017/8/9 请求超时
                }else{
                    // TODO: 2017/8/9 远程请求失败
                }
            }
            return remotingTransporter;
        }finally {
            responseTable.remove(request.getOpaque());
        }
    }


    /**
     * 处理接收到的消息
     * @param ctx
     * @param remotingTransporter
     */
    protected void processMessageReviced(ChannelHandlerContext ctx,RemotingTransporter remotingTransporter){

        final RemotingTransporter transporter=remotingTransporter;
        if(transporter!=null){
            switch (remotingTransporter.getTransporterType()){
                case HrpcProtocol.REQUEST_REMOTING:
                    processRemotingRequest(ctx,transporter);
                    break;
                case HrpcProtocol.RESPONSE_REMOTING:
                    processRemotingResponse(ctx,transporter);
                    break;
                default:break;

            }
        }
    }


    /**
     * 处理客户端的请求消息
     * @param ctx
     * @param remotingTransporter
     */
    protected void processRemotingRequest(final ChannelHandlerContext ctx, final RemotingTransporter remotingTransporter){
        //拿到请求类型对应的处理器
        Pair<NettyRequestProcessor, ExecutorService> matchedPair = processorTable.get(remotingTransporter.getCode());
        //如果用户没有定义处理器,就是使用默认的处理
        final Pair<NettyRequestProcessor, ExecutorService> pair=matchedPair==null?defaultRequestProcess:matchedPair;
        Runnable run=new Runnable() {
            @Override
            public void run() {
                try {
                    //执行处理器,将执行结果返回给调用端
                    RemotingTransporter responseTransporter = pair.getKey().processRequest(ctx, remotingTransporter);
                    ctx.channel().writeAndFlush(responseTransporter).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            if(!channelFuture.isSuccess()){
                                logger.error("fail send response ,exception is [{}]",channelFuture.cause().getMessage());
                            }
                        }
                    });

                } catch (Exception e) {
                    //如果处理异常,返回调用端处理异常消息
                    logger.error("processor occur exception [{}]",e.getMessage());
                    RemotingTransporter instance = RemotingTransporter.newInstance(remotingTransporter.getOpaque(), HrpcProtocol.HANDLER_ERROR, HrpcProtocol.RESPONSE_REMOTING, null);
                    ctx.channel().writeAndFlush(instance);
                }
            }
        };

        try{
            pair.getValue().submit(run);
        }catch (Exception e){
            logger.error("server is busy,[{}]",e.getMessage());
            RemotingTransporter instance = RemotingTransporter.newInstance(remotingTransporter.getOpaque(), HrpcProtocol.HANDLER_BUSY, HrpcProtocol.RESPONSE_REMOTING, null);
            ctx.channel().writeAndFlush(instance);
        }
    }

    /**
     * 处理服务器端发送来的响应消息
     * @param ctx
     * @param remotingTransporter
     */
    protected void processRemotingResponse(ChannelHandlerContext ctx,RemotingTransporter remotingTransporter){
        RemotingResponse remotingResponse = responseTable.get(remotingTransporter.getOpaque());
        if(remotingResponse!=null){
            remotingResponse.putResponse(remotingTransporter);
            //从篮子中把这次请求删除
            responseTable.remove(remotingTransporter.getOpaque());
        }else{
            logger.warn("received response but matched Id is removed from responseTable maybe timeout");
        }
    }

    /**
     * 处理链路关闭的事件
     * @param ctx
     */
    protected void processChannelInaction(final ChannelHandlerContext ctx){
        final Pair<NettyChannelnactiveProcessor, ExecutorService> pair = this.defaultChannelInactiveProcessor;
        if(pair!=null){
            Runnable run=new Runnable() {
                @Override
                public void run() {
                    try {
                        pair.getKey().processChannelInacitve(ctx);
                    } catch (InterruptedException e) {
                        logger.error("server occor exception [{}]",e.getMessage());
                    }
                }
            };
            try{
                pair.getValue().submit(run);
            }catch (Exception  e){
                logger.error("server is busy,[{}]",e.getMessage());
            }

        }
    }



}
