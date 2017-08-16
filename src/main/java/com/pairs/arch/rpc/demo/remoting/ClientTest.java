package com.pairs.arch.rpc.demo.remoting;

import com.pairs.arch.rpc.common.protocol.HrpcProtocol;
import com.pairs.arch.rpc.common.serializer.SerializerHolder;
import com.pairs.arch.rpc.common.transport.body.ACKCustomBody;
import com.pairs.arch.rpc.common.transport.body.RegisterServiceCustomBody;
import com.pairs.arch.rpc.remote.component.NettyRemotingClient;
import com.pairs.arch.rpc.remote.config.NettyClientConfig;
import com.pairs.arch.rpc.remote.model.NettyRequestProcessor;
import com.pairs.arch.rpc.remote.model.RemotingTransporter;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created on 2017年08月15日18:13
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
public class ClientTest {

    public static void main(String[] args) {
        NettyClientConfig nettyClientConfig=new NettyClientConfig();
        NettyRemotingClient client=new NettyRemotingClient(nettyClientConfig);
        client.registerProcess(HrpcProtocol.ACK, new NettyRequestProcessor() {
            @Override
            public RemotingTransporter processRequest(ChannelHandlerContext ctx, RemotingTransporter request) throws Exception {
                ACKCustomBody ackCustomBody = SerializerHolder.serializerImpl().readObject(request.bytes(), ACKCustomBody.class);
                request.setCustomHeader(ackCustomBody);
                return request;
            }
        },null);

        client.start();

        RegisterServiceCustomBody registerBody=new RegisterServiceCustomBody();
        registerBody.setServiceProviderName("service.name");
        RemotingTransporter requestTransprot = RemotingTransporter.createRequestTransprot(HrpcProtocol.PUBLISH_SERVICE, registerBody);
        RemotingTransporter response = client.invokeSync("127.0.0.1:7080", requestTransprot, 200000l);
        System.out.println(((ACKCustomBody)response.getCustomHeader()).getMsg());

    }
}
