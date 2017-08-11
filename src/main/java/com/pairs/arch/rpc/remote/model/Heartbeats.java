package com.pairs.arch.rpc.remote.model;

import com.pairs.arch.rpc.common.protocol.HrpcProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Created on 2017年08月11日15:43
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
public class Heartbeats {

    private static final ByteBuf HEART_BEATS;

    static {
        ByteBuf byteBuf=Unpooled.buffer(HrpcProtocol.HEAD_LENGTH);
        byteBuf.writeShort(HrpcProtocol.MAGIC);
        byteBuf.writeByte(HrpcProtocol.REQUEST_REMOTING);
        byteBuf.writeByte(HrpcProtocol.HEARTBEAT);
        byteBuf.writeLong(0);
        byteBuf.writeInt(0);
        byteBuf.writeByte(HrpcProtocol.UNCOMPRESS);
        HEART_BEATS=Unpooled.unmodifiableBuffer(Unpooled.unreleasableBuffer(byteBuf));
    }

    public static ByteBuf heartbeatContent(){
        return HEART_BEATS;
    }

}
