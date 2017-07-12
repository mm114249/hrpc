package com.pairs.arch.rpc.server.config;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * Created on 2017年07月11日 17:17
 * <P>
 * Title:[]
 * </p>
 * <p>
 * Description :[]
 * </p>
 * Company:武汉灵达科技有限公司
 *
 * @author [hupeng]
 * @version 1.0
 **/
public class ThreadConfig {

    //服务器端处理业务的线程数量
    public static EventExecutorGroup eventExecutors=new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors());

    public static EventLoopGroup work=new NioEventLoopGroup(1);
    public static EventLoopGroup boss=new NioEventLoopGroup(2);
    //全局channel管理
    public static ChannelGroup channelGroup=new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

}
