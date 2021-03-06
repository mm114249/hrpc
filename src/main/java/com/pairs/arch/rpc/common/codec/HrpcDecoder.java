package com.pairs.arch.rpc.common.codec;

import com.pairs.arch.rpc.common.util.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by hupeng on 2017/3/24.
 */
public class HrpcDecoder extends ByteToMessageDecoder {

    private Class<?> clazz;

    public HrpcDecoder(Class<?> clazz){
        this.clazz=clazz;
    }


    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if(byteBuf.readableBytes()<2){
            return;
        }

        byte[] magicByte=new byte[2];

        byteBuf.readBytes(magicByte);

        //检查消息开头是否是魔数，用来判断消息的开头
        String magic=new String(magicByte);
        if(!"#$".equals(magic)){
            return;
        }

        if(byteBuf.readableBytes()<4){
            return;
        }

        byteBuf.markReaderIndex();

        int dataLength=byteBuf.readInt();

        if(byteBuf.readableBytes()<dataLength){
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] data=new byte[dataLength];
        byteBuf.readBytes(data);
        Object obj= SerializationUtil.deserialize(data,clazz);
        list.add(obj);
    }
}
