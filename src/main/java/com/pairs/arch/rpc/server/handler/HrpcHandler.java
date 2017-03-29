package com.pairs.arch.rpc.server.handler;

import com.pairs.arch.rpc.common.bean.HrpcRequest;
import com.pairs.arch.rpc.common.bean.HrpcResponse;
import com.pairs.arch.rpc.server.util.ServerWrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by hupeng on 2017/3/28.
 */
public class HrpcHandler extends SimpleChannelInboundHandler<HrpcRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HrpcRequest hrpcRequest) throws Exception {
        System.out.println("in server handler");
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
        System.out.println("channel close id is --->"+ctx.channel().id().asShortText() );
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel active id is --->"+ctx.channel().id().asShortText() );
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

//        Method method = null;
//        try {
//            method = serviceClass.getMethod(methodName, parameterTypes);
//            method.setAccessible(true);
//            Object obj=method.invoke(serviceBean, parameters);
//            return obj;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }



        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);

    }


}
