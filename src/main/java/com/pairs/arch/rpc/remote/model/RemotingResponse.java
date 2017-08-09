package com.pairs.arch.rpc.remote.model;

import com.pairs.arch.rpc.remote.InvokeCallback;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2017年08月09日11:43
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
public class RemotingResponse {
    // 远程端返回的结果集
    private volatile RemotingTransporter remotingTransporter;
    // 该请求抛出的异常，如果存在的话
    private volatile Throwable cause;
    // 发送端是否发送成功
    private volatile boolean sendRequestOK = true;
    // 请求的opaque
    private final long opaque;
    // 默认的回调函数
    private final InvokeCallback invokeCallback;
    // 请求的默认超时时间
    private final long timeoutMillis;

    private final long beginTimestamp = System.currentTimeMillis();
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    public RemotingResponse(long opaque, long timeoutMillis, InvokeCallback invokeCallback) {
        this.invokeCallback = invokeCallback;
        this.opaque = opaque;
        this.timeoutMillis = timeoutMillis;
    }

    public void executeInvokeCallback() {
        if (invokeCallback != null) {
            invokeCallback.operationComplete(this);
        }
    }

    public RemotingTransporter waitResponse() throws InterruptedException{
        this.countDownLatch.await(this.timeoutMillis, TimeUnit.MILLISECONDS);
        return this.remotingTransporter;
    }

    public void putResponse(final RemotingTransporter remotingTransporter){
        this.remotingTransporter = remotingTransporter;
        this.countDownLatch.countDown();
    }

    public RemotingTransporter getRemotingTransporter() {
        return remotingTransporter;
    }

    public void setRemotingTransporter(RemotingTransporter remotingTransporter) {
        this.remotingTransporter = remotingTransporter;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public boolean isSendRequestOK() {
        return sendRequestOK;
    }

    public void setSendRequestOK(boolean sendRequestOK) {
        this.sendRequestOK = sendRequestOK;
    }

    public long getOpaque() {
        return opaque;
    }

    public InvokeCallback getInvokeCallback() {
        return invokeCallback;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public long getBeginTimestamp() {
        return beginTimestamp;
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }
}
