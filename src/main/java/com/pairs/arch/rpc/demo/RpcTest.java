package com.pairs.arch.rpc.demo;

import com.google.common.collect.Lists;
import com.pairs.arch.rpc.client.proxy.HrpcProxy;
import com.pairs.arch.rpc.server.HrpcStartServer;
import com.pairs.arch.rpc.server.config.HrpcServerConfig;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by hupeng on 2017/3/28.
 */
public class RpcTest {
    public static void main(String[] args) {
        AbstractApplicationContext appContext = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try{
            HelloServer helloServer = HrpcProxy.getInstance().getBean(HelloServer.class);
            System.out.println(helloServer.getName("aaa"));
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
