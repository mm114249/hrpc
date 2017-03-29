package com.pairs.arch.rpc.server.util;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hupeng on 2017/3/28.
 */
public class ServerWrap {

    private static Map<String, Object> serverMap = new HashMap<String, Object>();


    public static void addServer(String key, Class<?> obj) {
        //通过 class 反射出一个对象
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(obj);
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                return methodProxy.invokeSuper(o, objects);
            }
        });
        serverMap.put(key, enhancer.create());
    }

    public static Object get(String key) {
        return serverMap.get(key);
    }


}
