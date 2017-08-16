package com.pairs.arch.rpc.remote.watcher;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.concurrent.TimeUnit;

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
    private EventExecutorGroup executorGroup;
    private int attempts;//重试次数
    private Timer timer;

    public ConnectionWatchdog(Bootstrap bootstrap,EventExecutorGroup executorGroup,Timer timer){
        this.bootstrap=bootstrap;
        this.executorGroup=executorGroup;
        this.timer=timer;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        synchronized (bootstrap){
            bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    channel.pipeline().addLast(executorGroup,handlers());
                }
            });
        }
        ChannelFuture channelFuture = bootstrap.connect().sync();
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if(!channelFuture.isSuccess()){
                    channelFuture.channel().pipeline().fireChannelInactive();
                }
            }
        });
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.attempts=0;//连接成功,重置重连计数
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(isReconnect()){
            if(attempts++<10){
                long taskTime=2<<attempts;
                timer.newTimeout(this,taskTime, TimeUnit.SECONDS);
            }
        }
        super.channelInactive(ctx);
    }

    public boolean isReconnect() {
        return isReconnect;
    }

    public void setReconnect(boolean reconnect) {
        isReconnect = reconnect;
    }
}
