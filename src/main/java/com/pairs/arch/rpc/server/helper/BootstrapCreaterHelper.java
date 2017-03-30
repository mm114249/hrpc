package com.pairs.arch.rpc.server.helper;

import com.pairs.arch.rpc.common.bean.HrpcRequest;
import com.pairs.arch.rpc.common.bean.HrpcResponse;
import com.pairs.arch.rpc.common.codec.HrpcDecoder;
import com.pairs.arch.rpc.common.codec.HrpcEncoder;
import com.pairs.arch.rpc.server.handler.HrpcHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;

/**
 * Created by hupeng on 2017/3/30.
 */
public class BootstrapCreaterHelper implements Runnable {


    private Logger logger=Logger.getLogger(BootstrapCreaterHelper.class);
    private Integer serverPort;

    public BootstrapCreaterHelper(Integer serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        createBootstrap();
    }


    /**
     * 启动netty 服务
     */
    private void createBootstrap() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(4);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline().addLast(new HrpcDecoder(HrpcRequest.class));
                        channel.pipeline().addLast(new HrpcEncoder(HrpcResponse.class));
                        channel.pipeline().addLast(new HrpcHandler());
                    }
                });

        ChannelFuture future = null;
        try {
            future = bootstrap.bind(serverPort).sync();
            if(logger.isDebugEnabled()){
                logger.debug("server netty start success");
            }
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error(e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
