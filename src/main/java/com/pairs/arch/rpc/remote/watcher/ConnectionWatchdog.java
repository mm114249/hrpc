package com.pairs.arch.rpc.remote.watcher;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

/**
 * Created on 2017年08月11日17:37
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
@ChannelHandler.Sharable
public abstract class ConnectionWatchdog extends ChannelInboundHandlerAdapter implements TimerTask, ChannelHandlerHolder {

    private Bootstrap bootstrap;
    private boolean isReconnect=true;//是否重连

    @Override
    public ChannelHandler[] handlers() {
        return new ChannelHandler[0];
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        synchronized (bootstrap){
            bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                }
            });
        }
    }
}
