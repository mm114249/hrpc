package com.pairs.arch.rpc.remote.config;

import com.pairs.arch.rpc.common.constant.HRPConstants;

/**
 * Created on 2017年08月10日11:46
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
public class NettyServerConfig {

    /**服务器监听端口*/
    private int listenPort=7080;
    /**io线程数量*/
    private int serverWorkerThreads= HRPConstants.AVAILABLE_PROCESSORS;
    /**默认的低水位*/
    private int writeBufferLowWaterMark = -1;
    /**默认的高水位*/
    private int writeBufferHighWaterMark = -1;




    public int getServerWorkerThreads() {
        return serverWorkerThreads;
    }

    public void setServerWorkerThreads(int serverWorkerThreads) {
        this.serverWorkerThreads = serverWorkerThreads;
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

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }
}
