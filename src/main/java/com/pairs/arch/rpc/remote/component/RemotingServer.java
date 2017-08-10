package com.pairs.arch.rpc.remote.component;

import com.pairs.arch.rpc.common.util.Pair;
import com.pairs.arch.rpc.remote.model.NettyChannelnactiveProcessor;
import com.pairs.arch.rpc.remote.model.NettyRequestProcessor;
import com.pairs.arch.rpc.remote.model.RemotingTransporter;
import io.netty.channel.Channel;

import java.util.concurrent.ExecutorService;

/**
 * Created on 2017年08月10日15:04
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
public interface RemotingServer extends BaseRemotingService {
    /**注册请求处理器*/
    void registerProcessor(final byte code, final NettyRequestProcessor processor, final ExecutorService executorService);
    /**注册通过关闭处理器*/
    void registerChannelInactiveProcessor(final NettyChannelnactiveProcessor processor,final ExecutorService executorService);
    /**注册默认处理器*/
    void registerDefaultProcessor(final NettyRequestProcessor processor,final ExecutorService executorService);
    /**获得处理器pair*/
    Pair<NettyRequestProcessor,ExecutorService> getProcessPair(final int requestCode);
    /**执行请求方法*/
    RemotingTransporter invokeSync(final Channel channel,final RemotingTransporter remotingTransporter,long timeoutMillis)throws InterruptedException;





}
