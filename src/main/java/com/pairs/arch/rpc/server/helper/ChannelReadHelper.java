package com.pairs.arch.rpc.server.helper;

import com.pairs.arch.rpc.common.bean.HrpcRequest;
import com.pairs.arch.rpc.common.bean.HrpcResponse;
import com.pairs.arch.rpc.server.util.ServerWrap;
import io.netty.channel.ChannelHandlerContext;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by hupeng on 2017/3/31.
 */
public class ChannelReadHelper implements Runnable {

    private HrpcRequest hrpcRequest;
    private ChannelHandlerContext channelHandlerContext;

    @Override
    public void run() {
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

    public ChannelReadHelper(HrpcRequest hrpcRequest,ChannelHandlerContext channelHandlerContext) {
        this.hrpcRequest = hrpcRequest;
        this.channelHandlerContext=channelHandlerContext;
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
