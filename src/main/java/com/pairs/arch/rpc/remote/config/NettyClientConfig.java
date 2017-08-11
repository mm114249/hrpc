package com.pairs.arch.rpc.remote.config;

import com.pairs.arch.rpc.common.constant.HRPConstants;
import com.pairs.arch.rpc.common.protocol.HrpcProtocol;

/**
 * Created on 2017年08月11日15:22
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
public class NettyClientConfig {
    private int clientWorkerThreads=4;
    private int clientCallBackExecutorThreads= HRPConstants.AVAILABLE_PROCESSORS;
    private long connectTimeoutMillis=3000;
    private long channelNotActiveIntervalMillis=1000*60;

    private String defaultAddress;
    private int clientChannelMaxIdleTimeSeconds = 120;

    private int clientSocketSndBufSize = -1;
    private int clientSocketRcvBufSize = -1;

    private int writeBufferLowWaterMark = -1;
    private int writeBufferHighWaterMark = -1;

    public int getClientWorkerThreads() {
        return clientWorkerThreads;
    }

    public void setClientWorkerThreads(int clientWorkerThreads) {
        this.clientWorkerThreads = clientWorkerThreads;
    }

    public int getClientCallBackExecutorThreads() {
        return clientCallBackExecutorThreads;
    }

    public void setClientCallBackExecutorThreads(int clientCallBackExecutorThreads) {
        this.clientCallBackExecutorThreads = clientCallBackExecutorThreads;
    }

    public long getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(long connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public long getChannelNotActiveIntervalMillis() {
        return channelNotActiveIntervalMillis;
    }

    public void setChannelNotActiveIntervalMillis(long channelNotActiveIntervalMillis) {
        this.channelNotActiveIntervalMillis = channelNotActiveIntervalMillis;
    }

    public String getDefaultAddress() {
        return defaultAddress;
    }

    public void setDefaultAddress(String defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    public int getClientChannelMaxIdleTimeSeconds() {
        return clientChannelMaxIdleTimeSeconds;
    }

    public void setClientChannelMaxIdleTimeSeconds(int clientChannelMaxIdleTimeSeconds) {
        this.clientChannelMaxIdleTimeSeconds = clientChannelMaxIdleTimeSeconds;
    }

    public int getClientSocketSndBufSize() {
        return clientSocketSndBufSize;
    }

    public void setClientSocketSndBufSize(int clientSocketSndBufSize) {
        this.clientSocketSndBufSize = clientSocketSndBufSize;
    }

    public int getClientSocketRcvBufSize() {
        return clientSocketRcvBufSize;
    }

    public void setClientSocketRcvBufSize(int clientSocketRcvBufSize) {
        this.clientSocketRcvBufSize = clientSocketRcvBufSize;
    }

    public int getWriteBufferLowWaterMark() {
        return writeBufferLowWaterMark;
    }

    public void setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        this.writeBufferLowWaterMark = writeBufferLowWaterMark;
    }

    public int getWriteBufferHighWaterMark() {
        return writeBufferHighWaterMark;
    }

    public void setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        this.writeBufferHighWaterMark = writeBufferHighWaterMark;
    }
}
