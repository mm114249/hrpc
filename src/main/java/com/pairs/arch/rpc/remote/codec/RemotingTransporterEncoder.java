package com.pairs.arch.rpc.remote.codec;

import com.pairs.arch.rpc.common.protocol.HrpcProtocol;
import com.pairs.arch.rpc.common.serializer.SerializerHolder;
import com.pairs.arch.rpc.remote.model.CommonCustomBody;
import com.pairs.arch.rpc.remote.model.RemotingTransporter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created on 2017年08月11日12:00
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
@ChannelHandler.Sharable
public class RemotingTransporterEncoder extends MessageToByteEncoder<RemotingTransporter> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RemotingTransporter remotingTransporter, ByteBuf byteBuf) throws Exception {
        CommonCustomBody customHeader = remotingTransporter.getCustomHeader();
        byte[] bytes = SerializerHolder.serializerImpl().writeObject(customHeader);

        byteBuf.writeShort(HrpcProtocol.MAGIC);
        byteBuf.writeByte(remotingTransporter.getTransporterType());
        byteBuf.writeByte(remotingTransporter.getCode());
        byteBuf.writeLong(remotingTransporter.getOpaque());
        byteBuf.writeInt(bytes.length);
        byteBuf.writeByte(HrpcProtocol.UNCOMPRESS);
        byteBuf.writeBytes(bytes);

    }
}
