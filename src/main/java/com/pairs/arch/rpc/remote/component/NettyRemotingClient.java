package com.pairs.arch.rpc.remote.component;

import com.pairs.arch.rpc.common.constant.HRPConstants;
import com.pairs.arch.rpc.common.util.NamedThreadFactory;
import com.pairs.arch.rpc.remote.NettyRemotingBase;
import com.pairs.arch.rpc.remote.config.NettyClientConfig;
import com.pairs.arch.rpc.remote.idle.ConnectorIdleStateTrigger;
import com.pairs.arch.rpc.remote.model.NettyChannelnactiveProcessor;
import com.pairs.arch.rpc.remote.model.NettyRequestProcessor;
import com.pairs.arch.rpc.remote.model.RemotingTransporter;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.concurrent.ExecutorService;
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
    private Bootstrap bootstrap;
    private EventLoopGroup work;

    private int workNum;
    private Lock lockChannelTable=new ReentrantLock();
    protected volatile ByteBufAllocator allocator;
    private final static long lockedTimeout=1000l;
    private EventExecutorGroup defaultEventExecutorGroup;
    private NettyClientConfig nettyClientConfig;
    private volatile int writeBufferHighWaterMark = -1;
    private volatile int writeBufferLowWaterMark = -1;

    private ConnectorIdleStateTrigger idleStateTrigger=new ConnectorIdleStateTrigger();
    private HashedWheelTimer timer=new HashedWheelTimer(new NamedThreadFactory("timer"));

    public NettyRemotingClient(NettyClientConfig nettyClientConfig){
        this.nettyClientConfig=nettyClientConfig;
        allocator= PooledByteBufAllocator.DEFAULT;
        if(this.nettyClientConfig!=null){
            workNum= HRPConstants.AVAILABLE_PROCESSORS;
            this.writeBufferHighWaterMark=nettyClientConfig.getWriteBufferHighWaterMark();
            this.writeBufferLowWaterMark=nettyClientConfig.getWriteBufferLowWaterMark();
        }

        init();
    }


    @Override
    public RemotingTransporter invokeSync(String address, RemotingTransporter request, long timeout) {
        return null;
    }

    @Override
    public void registerProcess(byte requestCode, NettyRequestProcessor processor, ExecutorService executor) {

    }

    @Override
    public void registerChannelInactiveProcess(NettyChannelnactiveProcessor processor, ExecutorService executor) {

    }

    @Override
    public boolean isChannelWriteable(String address) {
        return false;
    }

    @Override
    public void setReconnect(boolean isReconnect) {

    }

    @Override
    public void init() {
        bootstrap=new Bootstrap();
        work=new NioEventLoopGroup(workNum,new DefaultThreadFactory("netty.client"));
        bootstrap.group(work);
        ((NioEventLoopGroup)work).setIoRatio(100);
        bootstrap.option(ChannelOption.ALLOCATOR,allocator)
        .option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT)
        .option(ChannelOption.SO_REUSEADDR, true)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) SECONDS.toMillis(3))
        .option(ChannelOption.SO_KEEPALIVE, true)
        .option(ChannelOption.TCP_NODELAY, true)
        .option(ChannelOption.ALLOW_HALF_CLOSURE, false);

        if(this.writeBufferHighWaterMark>0&&this.writeBufferLowWaterMark>0){
            WriteBufferWaterMark mark=new WriteBufferWaterMark(this.writeBufferLowWaterMark,this.writeBufferHighWaterMark);
            bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK,mark);
        }

    }

    @Override
    public void start() {
        defaultEventExecutorGroup=new DefaultEventExecutorGroup(nettyClientConfig.getClientWorkerThreads(),new NamedThreadFactory("NettyClientWorkerThread_"));
        bootstrap.channel(NioSocketChannel.class);
    }

    @Override
    public void shutdown() {

    }
}
