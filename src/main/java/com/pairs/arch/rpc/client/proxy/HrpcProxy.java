package com.pairs.arch.rpc.client.proxy;

import com.pairs.arch.rpc.client.HrpcConnect;
import com.pairs.arch.rpc.client.ResponseWrap;
import com.pairs.arch.rpc.client.SyncLock;
import com.pairs.arch.rpc.client.discovery.ServerDiscovery;
import com.pairs.arch.rpc.common.bean.HrpcRequest;
import com.pairs.arch.rpc.common.bean.HrpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by hupeng on 2017/3/28.
 */
public class HrpcProxy {

    private static HrpcProxy instance = new HrpcProxy();
    private ServerDiscovery serverDiscovery;
    private Long connectTime=5l;

    private HrpcProxy() {
    }

    public <T> T getBean(Class<?> interfaceClazz){
       return (T)Proxy.newProxyInstance(interfaceClazz.getClassLoader(), new Class<?>[]{interfaceClazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                HrpcRequest request=new HrpcRequest(HrpcRequest.RequestType.NORMAL);
                String uuid= UUID.randomUUID().toString().replaceAll("-","");
                request.setRequestId(uuid);
                request.setMethodName(method.getName());
                request.setClassName(method.getDeclaringClass().getName());
                request.setParameterTypes(method.getParameterTypes());
                request.setParameters(args);

                HrpcConnect channel=serverDiscovery.getServer(request);
                channel.writeAndFlush(request);

                CountDownLatch latch=new CountDownLatch(1);
                SyncLock.getInstance().put(request.getRequestId(),latch);
                latch.await(connectTime, TimeUnit.SECONDS);
                HrpcResponse response=ResponseWrap.getInstance().get(request.getRequestId());
                if(response==null){
                    throw new RuntimeException("call "+request.getClassName()+" time out");
                }

                if(response.getError()!=null){
                    throw response.getError();
                }
                return response.getResult();
            }
        });

    }


    public static HrpcProxy getInstance(ServerDiscovery serverDiscovery) {
        instance.serverDiscovery=serverDiscovery;
        return instance;
    }

}
