package com.pairs.arch.rpc.remote.component;

import com.pairs.arch.rpc.common.constant.HRPConstants;
import com.pairs.arch.rpc.common.util.ConnectionUtils;
import com.pairs.arch.rpc.common.util.NamedThreadFactory;
import com.pairs.arch.rpc.common.util.Pair;
import com.pairs.arch.rpc.remote.NettyRemotingBase;
import com.pairs.arch.rpc.remote.codec.RemotingTransporterDecoder;
import com.pairs.arch.rpc.remote.codec.RemotingTransporterEncoder;
import com.pairs.arch.rpc.remote.config.NettyClientConfig;
import com.pairs.arch.rpc.remote.idle.ConnectorIdleStateTrigger;
import com.pairs.arch.rpc.remote.model.NettyChannelnactiveProcessor;
import com.pairs.arch.rpc.remote.model.NettyRequestProcessor;
import com.pairs.arch.rpc.remote.model.RemotingTransporter;
import com.pairs.arch.rpc.remote.watcher.ConnectionWatchdog;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created on 2017年08月11日15:14
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
public class NettyRemotingClient extends NettyRemotingBase implements RemotingClient {
    private Logger logger = LoggerFactory.getLogger(NettyRemotingClient.class);

    private Bootstrap bootstrap;
    private EventLoopGroup work;

    private int workNum;
    private Lock lockChannelTable = new ReentrantLock();
    protected volatile ByteBufAllocator allocator;
    private final static long lockedTimeout = 1000l;
    private EventExecutorGroup defaultEventExecutorGroup;
    private NettyClientConfig nettyClientConfig;
    private volatile int writeBufferHighWaterMark = -1;
    private volatile int writeBufferLowWaterMark = -1;

    private ConnectorIdleStateTrigger idleStateTrigger = new ConnectorIdleStateTrigger();
    private HashedWheelTimer timer = new HashedWheelTimer(new NamedThreadFactory("timer"));
    private boolean reConnect = true;
    private Map<String, ChannelWarp> channelTable = new HashMap<String, ChannelWarp>();


    public NettyRemotingClient(NettyClientConfig nettyClientConfig) {
        this.nettyClientConfig = nettyClientConfig;
        allocator = PooledByteBufAllocator.DEFAULT;
        if (this.nettyClientConfig != null) {
            workNum = HRPConstants.AVAILABLE_PROCESSORS;
            this.writeBufferHighWaterMark = nettyClientConfig.getWriteBufferHighWaterMark();
            this.writeBufferLowWaterMark = nettyClientConfig.getWriteBufferLowWaterMark();
        }

        init();
    }


    @Override
    public RemotingTransporter invokeSync(String address, RemotingTransporter request, long timeout) {
        Channel channel;
        try {
            channel=getAndcreateChannel(address);
        } catch (InterruptedException e) {
            logger.info("get channel exception");
            throw new RuntimeException("get channel exception");
        }

        if(channel==null){
            throw new RuntimeException("this address "+address+" is not  channel");
        }

        if(!channel.isActive()){
            try {
                closeChannel(address,channel);
            } catch (InterruptedException e) {
                throw new RuntimeException("connection exception");
            }
        }


        try {
            RemotingTransporter response = this.invokeSyncImpl(channel, request, timeout);
            return response;
        } catch (InterruptedException e) {
            logger.error("",e);
        }


        return null;
    }

    @Override
    public void registerProcess(byte requestCode, NettyRequestProcessor processor, ExecutorService executor) {
        if (executor == null) {
            executor = publicExecutor;
        }

        Pair<NettyRequestProcessor, ExecutorService> pair = new Pair(processor, executor);
        this.processorTable.put(requestCode, pair);

    }

    @Override
    public void registerChannelInactiveProcess(NettyChannelnactiveProcessor processor, ExecutorService executor) {
        if (executor == null) {
            executor = publicExecutor;
        }
        this.defaultChannelInactiveProcessor = new Pair<NettyChannelnactiveProcessor, ExecutorService>(processor, executor);
    }

    @Override
    public boolean isChannelWriteable(String address) {
        ChannelWarp warp = this.channelTable.get(address);
        if (warp != null && warp.isOk()) {
            return warp.isWriteable();
        }
        return false;
    }

    @Override
    public void setReconnect(boolean isReconnect) {
        this.reConnect = isReconnect;
    }

    @Override
    public void init() {
        bootstrap = new Bootstrap();
        work = new NioEventLoopGroup(workNum, new DefaultThreadFactory("netty.client"));
        bootstrap.group(work);
        ((NioEventLoopGroup) work).setIoRatio(100);
        bootstrap.option(ChannelOption.ALLOCATOR, allocator)
                .option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) SECONDS.toMillis(3))
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOW_HALF_CLOSURE, false);

        if (this.writeBufferHighWaterMark > 0 && this.writeBufferLowWaterMark > 0) {
            WriteBufferWaterMark mark = new WriteBufferWaterMark(this.writeBufferLowWaterMark, this.writeBufferHighWaterMark);
            bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, mark);
        }

    }

    @Override
    public void start() {
        defaultEventExecutorGroup = new DefaultEventExecutorGroup(nettyClientConfig.getClientWorkerThreads(), new NamedThreadFactory("NettyClientWorkerThread_"));
        bootstrap.channel(NioSocketChannel.class);
        final ConnectionWatchdog dog = new ConnectionWatchdog(bootstrap, defaultEventExecutorGroup, timer) {
            @Override
            public ChannelHandler[] handlers() {
                return new ChannelHandler[]{
                        this,
                        new RemotingTransporterDecoder(),
                        new RemotingTransporterEncoder(),
                        new IdleStateHandler(0, HRPConstants.WRITE_IDLE_TIME_SECONDS, 0),
                        idleStateTrigger,
                        new NettyClientHandler()
                };
            }
        };

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) throws Exception {
                channel.pipeline().addLast(defaultEventExecutorGroup, dog.handlers());
            }
        });

        dog.setReconnect(reConnect);

    }

    @Override
    public void shutdown() {
        this.timer.stop();
        this.timer=null;
        for (Map.Entry<String, ChannelWarp> entry : this.channelTable.entrySet()) {
            try {
                this.closeChannel(null,entry.getValue().getChannel());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.channelTable.clear();
        work.shutdownGracefully();

        if(this.defaultEventExecutorGroup!=null){
            this.defaultEventExecutorGroup.shutdownGracefully();
        }

        if(this.publicExecutor!=null){
            this.publicExecutor.shutdown();
        }

    }

    private Channel getAndcreateChannel(String address) throws InterruptedException {

        if(StringUtils.isBlank(address)){
            logger.warn("address is null");
        }

        ChannelWarp cw = this.channelTable.get(address);
        if(cw!=null &&cw.isOk()){
            return cw.getChannel();
        }

        boolean iscreateNewChannel = false;
        if (lockChannelTable.tryLock(lockedTimeout, TimeUnit.MILLISECONDS)) {
            try {
                if (cw == null) {
                    iscreateNewChannel = true;
                } else {
                    if (cw.isOk()) {
                        return cw.getChannel();
                    } else if (!cw.getChannelFuture().isDone()) {
                        iscreateNewChannel = false;
                    } else {
                        //从缓存中删除无效的channel
                        iscreateNewChannel = true;
                        this.channelTable.remove(address);
                    }
                }

                if (iscreateNewChannel) {
                    ChannelFuture channelFuture = bootstrap.connect(ConnectionUtils.string2SocketAddress(address));
                    cw = new ChannelWarp(channelFuture);
                }
            } catch (Exception e) {
                logger.error("createChannel: create channel exception", e);
            } finally {
                lockChannelTable.unlock();
            }
        } else {
            logger.warn("createChannel: try to lock channel table, but timeout, {}ms", lockedTimeout);
        }

        if (cw != null) {
            if (cw.getChannelFuture().awaitUninterruptibly(nettyClientConfig.getConnectTimeoutMillis())) {
                if (cw.isOk()) {
                    return cw.getChannel();
                } else {
                    logger.warn("createChannel: connect remote host[" + address + "] failed, " + cw.getChannelFuture(), cw.getChannelFuture().cause());
                }
            } else {
                logger.warn("createChannel: connect remote host[{}] timeout {}ms, {}", address, this.nettyClientConfig.getConnectTimeoutMillis(),
                        cw.getChannelFuture().toString());
            }
        }
        return null;
    }


    private void closeChannel(String address,final Channel channel) throws InterruptedException {
        if(channel==null){
            logger.warn("channel is null");
            return ;
        }
        final String ads=StringUtils.isBlank(address)?ConnectionUtils.parseChannelRemoteAddr(channel):address;

        if(lockChannelTable.tryLock(lockedTimeout,TimeUnit.MILLISECONDS)){
            try {
                if(this.channelTable.containsKey(ads)){
                    ChannelWarp cw=this.channelTable.get(ads);
                    if(cw.getChannel()==channel){
                        this.channelTable.remove(ads);
                        ConnectionUtils.closeChannel(channel);
                    }
                }
            }finally {
                lockChannelTable.unlock();
            }
        }

    }

    class NettyClientHandler extends SimpleChannelInboundHandler<RemotingTransporter> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RemotingTransporter remotingTransporter) throws Exception {
            processMessageReviced(ctx, remotingTransporter);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            processChannelInaction(ctx);
        }
    }

    class ChannelWarp {
        protected ChannelFuture channelFuture;

        public ChannelWarp(ChannelFuture channelFuture) {
            this.channelFuture = channelFuture;
        }

        protected boolean isOk() {
            return channelFuture != null && channelFuture.channel() != null && channelFuture.channel().isActive();
        }

        protected boolean isWriteable() {
            return channelFuture.channel().isWritable();
        }

        protected Channel getChannel() {
            return channelFuture.channel();
        }

        protected ChannelFuture getChannelFuture() {
            return channelFuture;
        }


    }

}
