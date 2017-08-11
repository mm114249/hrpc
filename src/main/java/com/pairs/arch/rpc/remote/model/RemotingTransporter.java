package com.pairs.arch.rpc.remote.model;

import com.pairs.arch.rpc.common.protocol.HrpcProtocol;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created on 2017年08月09日11:00
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
public class RemotingTransporter extends ByteHolder {
    private static final AtomicLong requestId = new AtomicLong(0l);
    /**
     *  1订阅服务2发布服务
     */
    private byte code;
    /**
     * 消息请求主体
     */
    private transient CommonCustomBody customHeader;
    /**
     * 请求的id
     */
    private long opaque = requestId.getAndIncrement();
    /**
     * 定义该传输对象是请求还是响应信息
     */
    private byte transporterType;
    /**
     * 时间戳
     */
    private long timestamp;

    public static RemotingTransporter createRequestTransprot(byte code,CommonCustomBody customHeader){
        RemotingTransporter remotingTransporter=new RemotingTransporter();
        remotingTransporter.setCode(code);
        remotingTransporter.setTransporterType(HrpcProtocol.REQUEST_REMOTING);
        remotingTransporter.setCustomHeader(customHeader);
        return remotingTransporter;
    }

    public static RemotingTransporter createResponseTransport(byte code,CommonCustomBody customHeader){
        RemotingTransporter remotingTransporter=new RemotingTransporter();
        remotingTransporter.setCode(code);
        remotingTransporter.setTransporterType(HrpcProtocol.RESPONSE_REMOTING);
        remotingTransporter.setCustomHeader(customHeader);
        return remotingTransporter;
    }

    public static RemotingTransporter newInstance(long id, byte sign,byte type, byte[] bytes) {
        RemotingTransporter remotingTransporter = new RemotingTransporter();
        remotingTransporter.setCode(sign);
        remotingTransporter.setTransporterType(type);
        remotingTransporter.setOpaque(id);
        remotingTransporter.bytes(bytes);
        return remotingTransporter;
    }

    @Override
    public String toString() {
        return "RemotingTransporter [code=" + code + ", customHeader=" + customHeader + ", timestamp=" + timestamp + ", opaque=" + opaque
                + ", transporterType=" + transporterType + "]";
    }



    public static AtomicLong getRequestId() {
        return requestId;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public CommonCustomBody getCustomHeader() {
        return customHeader;
    }

    public void setCustomHeader(CommonCustomBody customHeader) {
        this.customHeader = customHeader;
    }

    public long getOpaque() {
        return opaque;
    }

    public void setOpaque(long opaque) {
        this.opaque = opaque;
    }

    public byte getTransporterType() {
        return transporterType;
    }

    public void setTransporterType(byte transporterType) {
        this.transporterType = transporterType;
    }
}
