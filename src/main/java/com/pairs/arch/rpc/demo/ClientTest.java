package com.pairs.arch.rpc.demo;

import com.pairs.arch.rpc.client.proxy.HrpcProxy;
import com.pairs.arch.rpc.client.registry.ServerDiscovery;

/**
 * Created by hupeng on 2017/3/28.
 */
public class ClientTest {

    public static void main(String[] args) throws InterruptedException {

//        ExecutorService executorService= Executors.newFixedThreadPool(3);
//        final HelloServer helloServer= HrpcProxy.getInstance(ServerDiscovery.getInstance()).getBean(HelloServer.class);
//
//
//        for(int i=0;i<10;i++){
//            executorService.execute(new Runnable() {
//                @Override
//                public void run() {
//                    System.out.println(helloServer.getName("aaa"));
//                }
//            });
//        }

        HelloServer helloServer= HrpcProxy.getInstance(ServerDiscovery.getInstance()).getBean(HelloServer.class);
        Thread.sleep(2000);
        helloServer.getName("aaa");


    }
}
