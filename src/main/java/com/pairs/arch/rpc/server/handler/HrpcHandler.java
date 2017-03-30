package com.pairs.arch.rpc.server.handler;

import com.pairs.arch.rpc.common.bean.HrpcRequest;
import com.pairs.arch.rpc.common.bean.HrpcResponse;
import com.pairs.arch.rpc.server.util.ServerWrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by hupeng on 2017/3/28.
 */
public class HrpcHandler extends SimpleChannelInboundHandler<HrpcRequest> {

    private Logger logger=Logger.getLogger(HrpcHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HrpcRequest hrpcRequest) throws Exception {
        HrpcResponse response = new HrpcResponse();
        response.setRequestId(hrpcRequest.getRequestId());
        try {
            Object result = callTarget(hrpcRequest);
            response.setResult(result);
        } catch (Exception e) {
            response.setError(e);
        }
        channelHandlerContext.writeAndFlush(response);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(logger.isDebugEnabled()){
            logger.debug("channel close id is --->"+ctx.channel().id().asShortText());
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        if(logger.isDebugEnabled()){
            logger.debug("channel active id is --->"+ctx.channel().id().asShortText());
        }
    }

    private Object callTarget(HrpcRequest hrpcRequest) throws InvocationTargetException {
        String className = hrpcRequest.getClassName();
        Object serviceBean = ServerWrap.get(className);
        if (serviceBean == null) {
            throw new RuntimeException(className + " no provider");
        }
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = hrpcRequest.getMethodName();
        Class<?>[] parameterTypes = hrpcRequest.getParameterTypes();
        Object[] parameters = hrpcRequest.getParameters();

        //使用cglig反射 避免发射带来的效率问题
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);

    }


}
