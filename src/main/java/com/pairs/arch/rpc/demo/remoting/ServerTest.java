package com.pairs.arch.rpc.demo.remoting;

import com.pairs.arch.rpc.common.protocol.HrpcProtocol;
import com.pairs.arch.rpc.common.serializer.SerializerHolder;
import com.pairs.arch.rpc.common.transport.body.ACKCustomBody;
import com.pairs.arch.rpc.common.transport.body.RegisterServiceCustomBody;
import com.pairs.arch.rpc.remote.component.NettyRemetingServer;
import com.pairs.arch.rpc.remote.config.NettyServerConfig;
import com.pairs.arch.rpc.remote.model.NettyRequestProcessor;
import com.pairs.arch.rpc.remote.model.RemotingTransporter;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created on 2017年08月15日11:02
 * <p>
 * Title:[]
 * </p >
 * <p>
 * Description :[]
 * </p >
 * Company:武汉灵达科技有限公司
 *
 * @author [hupeng]
 * @version 1.0
 **/
public class ServerTest {

    public static void main(String[] args) {
        NettyServerConfig config=new NettyServerConfig();
        NettyRemetingServer server=new NettyRemetingServer(config);
        server.registerProcessor(HrpcProtocol.PUBLISH_SERVICE, new NettyRequestProcessor() {
            @Override
            public RemotingTransporter processRequest(ChannelHandlerContext ctx, RemotingTransporter request) throws Exception {
                RegisterServiceCustomBody customBody = SerializerHolder.serializerImpl().readObject(request.bytes(), RegisterServiceCustomBody.class);
                System.out.println(customBody.getServiceProviderName());
                ACKCustomBody body=new ACKCustomBody(request.getOpaque(),true,"aabb");
                RemotingTransporter responseTransport = RemotingTransporter.createResponseTransport(HrpcProtocol.ACK, body);
                responseTransport.setOpaque(request.getOpaque());
                return responseTransport;
            }
        },null);
        server.start();
    }
}
