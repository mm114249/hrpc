package com.pairs.arch.rpc.server.helper;

import com.pairs.arch.rpc.common.bean.HrpcRequest;
import com.pairs.arch.rpc.common.bean.HrpcResponse;
import com.pairs.arch.rpc.common.codec.HrpcDecoder;
import com.pairs.arch.rpc.common.codec.HrpcEncoder;
import com.pairs.arch.rpc.server.config.HrpcServerConfig;
import com.pairs.arch.rpc.server.config.ThreadConfig;
import com.pairs.arch.rpc.server.handler.HrpcHandler;
import com.sun.org.apache.xml.internal.security.Init;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * Created by hupeng on 2017/3/30.
 */
public class BootstrapCreaterHelper implements InitializingBean {
    private static Logger logger=Logger.getLogger(BootstrapCreaterHelper.class);

    private HrpcServerConfig hrpcServerConfig;

    public BootstrapCreaterHelper(){

    }

    public BootstrapCreaterHelper(HrpcServerConfig hrpcServerConfig) {
        this.hrpcServerConfig = hrpcServerConfig;
    }

    /**
     * 启动netty 服务
     */
    public void createBootstrap() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(ThreadConfig.boss, ThreadConfig.work)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline().addLast(ThreadConfig.eventExecutors,new HrpcDecoder(HrpcRequest.class));
                        channel.pipeline().addLast(ThreadConfig.eventExecutors,new HrpcHandler());
                        channel.pipeline().addLast(ThreadConfig.eventExecutors,new HrpcEncoder(HrpcResponse.class));
                        //加入到全局管理中
                        ThreadConfig.channelGroup.add(channel);
                    }
                });

        ChannelFuture future = bootstrap.bind(hrpcServerConfig.getServerPort());
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if(channelFuture.isSuccess()){
                    logger.info("server netty start success");
                }
            }
        });
    }

    public void setHrpcServerConfig(HrpcServerConfig hrpcServerConfig) {
        this.hrpcServerConfig = hrpcServerConfig;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        createBootstrap();
    }
}
