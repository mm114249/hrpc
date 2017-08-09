package com.pairs.arch.rpc.remote;

import com.pairs.arch.rpc.common.util.Pair;
import com.pairs.arch.rpc.remote.model.NettyChannelnactiveProcessor;
import com.pairs.arch.rpc.remote.model.NettyRequestProcessor;
import com.pairs.arch.rpc.remote.model.RemotingResponse;
import com.pairs.arch.rpc.remote.model.RemotingTransporter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 2017年08月09日10:47
 * <p>
 * Title:[]
 * </p >
 * <p>
 * Description :[]
 * </p >
 * Company:
 *
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



}
