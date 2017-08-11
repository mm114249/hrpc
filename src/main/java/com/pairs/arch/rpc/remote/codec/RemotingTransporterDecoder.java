package com.pairs.arch.rpc.remote.codec;

import com.pairs.arch.rpc.common.protocol.HrpcProtocol;
import com.pairs.arch.rpc.remote.model.RemotingTransporter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import java.util.List;

/**
 * Created on 2017年08月10日17:46
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
public class RemotingTransporterDecoder extends ReplayingDecoder<RemotingTransporterDecoder.State> {
    private static Logger logger = LoggerFactory.getLogger(RemotingTransporterDecoder.class);

    private final HrpcProtocol header = new HrpcProtocol();
    private static final int MAX_BODY_SIZE=1024;//一条消息最大长度

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        switch (state()) {
            case HEADER_MAGIC:
                checkMagic(byteBuf.readShort());
                checkpoint(State.HEADER_TYPE);
            case HEADER_TYPE:
                header.setType(byteBuf.readByte());
                checkpoint(State.HEADER_SIGN);
            case HEADER_SIGN:
                header.setSign(byteBuf.readByte());
                checkpoint(State.HEADER_ID);
            case HEADER_ID:
                header.setId(byteBuf.readLong());
                checkpoint(State.HEADER_BODY_LENGTH);
            case HEADER_BODY_LENGTH:
                header.setBodyLength(byteBuf.readInt());
                checkBodyLength(header.getBodyLength());
                checkpoint(State.HEADER_COMPRESS);
            case HEADER_COMPRESS:
                header.setCompress(byteBuf.readByte());
                checkpoint(State.BODY);
            case BODY:
                checkBodyLength(header.getBodyLength());
                byte[] bodys=new byte[header.getBodyLength()];
                byteBuf.writeBytes(bodys);
                //解压
                if(header.getCompress()==HrpcProtocol.COMPRESS){
                    bodys= Snappy.uncompress(bodys);
                }
                RemotingTransporter outObj = RemotingTransporter.newInstance(header.getId(), header.getSign(), header.getType(), bodys);
                list.add(outObj);
                break;
            default:break;
        }

        checkpoint(State.HEADER_MAGIC);
    }

    private void checkBodyLength(int length) {
        if(length>MAX_BODY_SIZE){
            throw new RuntimeException("body of request is bigger than limit value "+ MAX_BODY_SIZE);
        }
    }

    private void checkMagic(short magic) {
        if (HrpcProtocol.MAGIC != magic) {
            throw new RuntimeException("magic value is not equal "+HrpcProtocol.MAGIC);
        }
    }


    enum State {
        HEADER_MAGIC, HEADER_TYPE, HEADER_SIGN, HEADER_ID, HEADER_BODY_LENGTH, HEADER_COMPRESS, BODY
    }
}
