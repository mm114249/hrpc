package com.pairs.arch.rpc.common.codec;

import com.pairs.arch.rpc.common.util.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by hupeng on 2017/3/24.
 */
public class HrpcEncoder extends MessageToByteEncoder {

    private Class<?> clazz;

    public HrpcEncoder(Class<?> clazz) {
        this.clazz = clazz;
    }


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if(clazz.isInstance(o)){
            byte[] data= SerializationUtil.serialize(o);
            int length=data.length;
            String magic="#$";
            byteBuf.writeBytes(magic.getBytes());
            byteBuf.writeInt(length);
            byteBuf.writeBytes(data);
        }
    }
}
