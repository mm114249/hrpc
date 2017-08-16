package com.pairs.arch.rpc.remote.component;

import com.pairs.arch.rpc.common.constant.HRPConstants;
import com.pairs.arch.rpc.common.util.Pair;
import com.pairs.arch.rpc.remote.*;
import com.pairs.arch.rpc.remote.codec.RemotingTransporterDecoder;
import com.pairs.arch.rpc.remote.codec.RemotingTransporterEncoder;
import com.pairs.arch.rpc.remote.config.NettyServerConfig;
import com.pairs.arch.rpc.remote.idle.AcceptorIdleStateTrigger;
import com.pairs.arch.rpc.remote.model.NettyChannelnactiveProcessor;
import com.pairs.arch.rpc.remote.model.NettyRequestProcessor;
import com.pairs.arch.rpc.remote.model.RemotingTransporter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 2017年08月10日11:44
 * <p>
 * Title:[]
 * </p >
 * <p>
 * Description :[服务器端创建服务的组件]
 * </p >
 * Company:
 *
 * @author [hupeng]
 * @version 1.0
 **/
public class NettyRemetingServer extends NettyRemotingBase implements RemotingServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyRemetingServer.class);

    private ServerBootstrap bootstrapServer;
    private EventLoopGroup boss;
    private EventLoopGroup work;

    private NettyServerConfig nettyServerConfig;
    private HashedWheelTimer timer = new HashedWheelTimer();
    private ByteBufAllocator allocator;
    private EventExecutorGroup eventExecutorGroup;
    private ExecutorService publicExecutor;
    private final AcceptorIdleStateTrigger idleStateTrigger = new AcceptorIdleStateTrigger();

    private int workerNum;//io线程数量
    private int writeBufferHighWaterMark;
    private int writeBufferLowWaterMark;


    public NettyRemetingServer(NettyServerConfig nettyServerConfig) {
        this.nettyServerConfig = nettyServerConfig;
        this.workerNum = nettyServerConfig.getServerWorkerThreads();

        this.writeBufferLowWaterMark = nettyServerConfig.getWriteBufferLowWaterMark();
        this.writeBufferHighWaterMark = nettyServerConfig.getWriteBufferHighWaterMark();
        publicExecutor = Executors.newFixedThreadPool(4, new ThreadFactory() {
            private AtomicInteger index = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "serverExecutorThread_" + index.getAndIncrement());
            }
        });

        init();
    }

    @Override
    public void init() {
        ThreadFactory bossFactory = new DefaultThreadFactory("netty_bossFactory");
        ThreadFactory workFactory=new DefaultThreadFactory("netty_workFactory");

        boss=new NioEventLoopGroup(1,bossFactory);
        work=new NioEventLoopGroup(workerNum,workFactory);
        bootstrapServer=new ServerBootstrap();
        bootstrapServer.group(work,work);

        this.allocator = PooledByteBufAllocator.DEFAULT;
        //开启io线程bytebuff池化
        bootstrapServer.childOption(ChannelOption.ALLOCATOR,this.allocator)
                .childOption(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
        //设置boss线程的io线程执行频率为100%,不会被用户的自定义线程打扰.提高io线程的处理能力,
        ((NioEventLoopGroup)boss).setIoRatio(100);
        ((NioEventLoopGroup)work).setIoRatio(100);
        //设置服务器最大连接数
        bootstrapServer.option(ChannelOption.SO_BACKLOG, 32768);
        bootstrapServer.option(ChannelOption.SO_REUSEADDR, true);

        bootstrapServer.childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.ALLOW_HALF_CLOSURE, false);

        //设置服务器端写的水位
        if(writeBufferHighWaterMark>0&&writeBufferLowWaterMark>0){
            WriteBufferWaterMark mark=new WriteBufferWaterMark(writeBufferLowWaterMark,writeBufferHighWaterMark);
            bootstrapServer.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, mark);
        }


    }

    @Override
    public void start() {
        eventExecutorGroup =new DefaultEventExecutorGroup(HRPConstants.AVAILABLE_PROCESSORS, new ThreadFactory() {
            private AtomicInteger index=new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r,"serverExecutorThread_"+index.getAndIncrement());
            }
        });

        bootstrapServer.channel(NioServerSocketChannel.class);
        bootstrapServer.localAddress(new InetSocketAddress(nettyServerConfig.getListenPort()));

        bootstrapServer.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline().addLast(eventExecutorGroup,
                        new RemotingTransporterDecoder(),
                        new RemotingTransporterEncoder(),
                        new IdleStateHandler(HRPConstants.READER_IDLE_TIME_SECONDS,0,0),
                        idleStateTrigger,
                        new ServerHandler()
                );
            }
        });

        try {
            logger.info("netty bind [{}] serverBootstrap start...",this.nettyServerConfig.getListenPort());
            bootstrapServer.bind().sync();
            logger.info("netty start success at port [{}]",this.nettyServerConfig.getListenPort());
        } catch (InterruptedException e) {
            logger.error("start serverBootstrap exception [{}]",e);
            throw new RuntimeException("this.serverBootstrap.bind().sync() InterruptedException", e);
        }


    }

    @Override
    public void shutdown() {
        if(timer!=null){
            timer.stop();
        }

        boss.shutdownGracefully();
        work.shutdownGracefully();
        if(eventExecutorGroup !=null){
            eventExecutorGroup.shutdownGracefully();
        }

        if(publicExecutor!=null){
            publicExecutor.shutdownNow();
        }

    }

    @Override
    public void registerProcessor(byte code, NettyRequestProcessor processor, ExecutorService executorService) {
        ExecutorService _executor=executorService;
        if(_executor==null){
            _executor=publicExecutor;
        }
        Pair<NettyRequestProcessor,ExecutorService> pair=new Pair<NettyRequestProcessor,ExecutorService>(processor,_executor);
        processorTable.put(code,pair);
    }

    @Override
    public void registerChannelInactiveProcessor(NettyChannelnactiveProcessor processor, ExecutorService executorService) {
        if(executorService==null){
            executorService=publicExecutor;
        }
        super.defaultChannelInactiveProcessor=new Pair<NettyChannelnactiveProcessor,ExecutorService>(processor,executorService);
    }

    @Override
    public void registerDefaultProcessor(NettyRequestProcessor processor, ExecutorService executorService) {
        if(executorService==null){
            executorService=publicExecutor;
        }
        super.defaultRequestProcess=new Pair<NettyRequestProcessor,ExecutorService>(processor,executorService);
    }

    @Override
    public Pair<NettyRequestProcessor, ExecutorService> getProcessPair(int requestCode) {
        return super.processorTable.get(requestCode);
    }

    @Override
    public RemotingTransporter invokeSync(Channel channel, RemotingTransporter remotingTransporter, long timeoutMillis) throws InterruptedException {
        return super.invokeSyncImpl(channel, remotingTransporter, timeoutMillis);
    }

    class ServerHandler extends SimpleChannelInboundHandler<RemotingTransporter>{
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RemotingTransporter remotingTransporter) throws Exception {
            processMessageReviced(ctx,remotingTransporter);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            processChannelInaction(ctx);
        }
    }

}
