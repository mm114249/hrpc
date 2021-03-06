package com.pairs.arch.rpc.server.helper;

import com.pairs.arch.rpc.common.bean.HrpcRequest;
import com.pairs.arch.rpc.common.bean.HrpcResponse;
import com.pairs.arch.rpc.server.container.ApplicationContextBuilder;
import com.pairs.arch.rpc.server.util.ServerWrap;
import io.netty.channel.ChannelHandlerContext;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by hupeng on 2017/3/31.
 */
public class ChannelReadHelper  {
    private static Logger logger= LoggerFactory.getLogger(ChannelReadHelper.class);

    public static void excute(HrpcRequest hrpcRequest,ChannelHandlerContext channelHandlerContext) {
        HrpcResponse response = new HrpcResponse(HrpcRequest.RequestType.NORMAL);
        response.setRequestId(hrpcRequest.getRequestId());
        try {
            Object result = callTarget(hrpcRequest);
            response.setResult(result);
        } catch (Exception e) {
            response.setError(e);
        }
        channelHandlerContext.channel().writeAndFlush(response);
    }

    private static Object callTarget(HrpcRequest hrpcRequest) throws InvocationTargetException, ClassNotFoundException {
        String className = hrpcRequest.getClassName();
        Class<?> clazz=Class.forName(className);
        Method method = ReflectionUtils.findMethod(clazz, hrpcRequest.getMethodName(),hrpcRequest.getParameterTypes());
        return ReflectionUtils.invokeMethod(method, ApplicationContextBuilder.getInstance().getAppContext().getBean(clazz), hrpcRequest.getParameters());
    }
}
