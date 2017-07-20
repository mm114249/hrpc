package com.pairs.arch.rpc.common.util;

import com.sun.org.apache.xpath.internal.SourceTree;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2017年07月19日 14:18
 * <P>
 * Title:[]
 * </p>
 * <p>
 * Description :[]
 * </p>
 * Company:
 *
 * @author [hupeng]
 * @version 1.0
 **/
@ChannelHandler.Sharable
public abstract class ConnectWatchDog extends ChannelInboundHandlerAdapter implements ChannelHanlderHolder, TimerTask {

    private final Logger logger = LoggerFactory.getLogger(Logger.class);

    private Bootstrap bootstrap;
    private String host;
    private Integer port;
    private Timer timer;
    private volatile boolean connected = true;//是否已经连接 true已经连接 fasle没有连接
    private int attemtps;
    private CountDownLatch downLatch = null;


    public ConnectWatchDog(Timer timer, Bootstrap bootstrap, String host, Integer port) {
        this.timer = timer;
        this.bootstrap = bootstrap;
        this.host = host;
        this.port = port;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        ChannelFuture channelFuture = bootstrap.connect(host, port);
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (!channelFuture.isSuccess()) {
                    logger.info("重连失败");
                    channelFuture.channel().pipeline().fireChannelInactive();
                } else {
                    logger.info("重连成功");
                    channelFuture.channel().pipeline().fireChannelActive();
                }
                downLatch.countDown();
            }
        });


    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        attemtps = 0;
        connected = true;
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        connected = false;
        while (!connected) {
            logger.info("链路开始重连接");
            downLatch = new CountDownLatch(1);
            long taskTime = 1 << attemtps;
            attemtps++;
            run(null);
            downLatch.await();
            if (attemtps == 5) {
                connected = true;
                logger.info(host + ":" + port + "服务已失效,不再进行重连");
            }
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

}
