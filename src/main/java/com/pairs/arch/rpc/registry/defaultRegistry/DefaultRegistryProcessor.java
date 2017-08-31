package com.pairs.arch.rpc.registry.defaultRegistry;

import com.pairs.arch.rpc.common.protocol.HrpcProtocol;
import com.pairs.arch.rpc.common.util.ConnectionUtils;
import com.pairs.arch.rpc.remote.model.NettyRequestProcessor;
import com.pairs.arch.rpc.remote.model.RemotingTransporter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 2017年08月21日16:45
 * <p/>
 * Title:[]
 * </p >
 * <p/>
 * Description :[注册中心默认的处理器]
 * </p >
 * Company:
 *
 * @author [hupeng]
 * @version 1.0
 **/
public class DefaultRegistryProcessor implements NettyRequestProcessor {
    private Logger logger= LoggerFactory.getLogger(DefaultRegistryProcessor.class);
    private DefaultRegistryServer defaultRegistryServer;

    public DefaultRegistryProcessor(DefaultRegistryServer defaultRegistryServer) {
        this.defaultRegistryServer = defaultRegistryServer;
    }

    @Override
    public RemotingTransporter processRequest(ChannelHandlerContext ctx, RemotingTransporter request) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("receive request, {} {} {}", request.getCode(), ConnectionUtils.parseChannelRemoteAddr(ctx.channel()), request);
        }

        switch (request.getCode()){
            case HrpcProtocol.PUBLISH_SERVICE://处理服务注册
                defaultRegistryServer.getRegistryProviderManager().handlerRegister(request,ctx.channel());
                break;
            case  HrpcProtocol.PUBLISH_CANCEL_SERVICE: //处理provider服务取消
                return this.defaultRegistryServer.getRegistryProviderManager().handlerRegisterCancel(request, ctx.channel());
            case HrpcProtocol.SUBSCRIBE_SERVICE: //处理服务消费者订阅服务的请求
                return this.defaultRegistryServer.getRegistryProviderManager().handleSubscribe(request, ctx.channel());
            case HrpcProtocol.MANAGER_SERVICE: // 处理管理者发送过来的服务管理服务
                return this.defaultRegistryServer.getRegistryProviderManager().handleManager(request, ctx.channel());
        }



        return null;
    }
}
