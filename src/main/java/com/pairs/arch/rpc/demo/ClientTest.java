package com.pairs.arch.rpc.demo;

import org.apache.log4j.Logger;

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

//        HelloServer helloServer= HrpcProxy.getInstance(ServerDiscovery.getInstance()).getBean(HelloServer.class);
//        Thread.sleep(2000);
//
//        for(int i=0;i<1;i++){
//            helloServer.getName("aaa");
//        }
        System.out.println(System.getProperty("user.dir"));
        System.setProperty("log.root",System.getProperty("user.dir"));
        Logger logger = Logger.getLogger(ClientTest.class);
        logger.debug(String.format("---connect zookeeper starts . address :%s---","aab"));


    }
}
