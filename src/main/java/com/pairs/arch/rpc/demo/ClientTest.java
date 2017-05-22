package com.pairs.arch.rpc.demo;

import com.google.common.collect.Lists;
import com.pairs.arch.rpc.client.proxy.HrpcProxy;
import com.pairs.arch.rpc.client.registry.ServerDiscovery;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hupeng on 2017/3/28.
 */
public class ClientTest {

    public static void main(String[] args) throws InterruptedException {

        ExecutorService executorService= Executors.newFixedThreadPool(1);
        final HelloServer helloServer= HrpcProxy.getInstance(ServerDiscovery.getInstance()).getBean(HelloServer.class);

        for(int i=0;i<1;i++){
            Thread.sleep(2000);
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println(helloServer.getName("aaa"));
                }
            });
        }

//        HelloServer helloServer= HrpcProxy.getInstance(ServerDiscovery.getInstance()).getBean(HelloServer.class);
//        Thread.sleep(2000);
//
//
//        StudentModel s1=new StudentModel(11,"小明", BigDecimal.TEN);
//        StudentModel s2=new StudentModel(100,"小红", BigDecimal.valueOf(11.1));
//        StudentModel s3=new StudentModel(1,"小王", BigDecimal.valueOf(-20.01));
//
//        List<StudentModel> studentModels= Lists.newArrayList(s1,s2,s3);
//
//        SchoolMode schoolMode=new SchoolMode("春田花花",BigDecimal.TEN,studentModels);
//
//        long start=new Date().getTime();
//        for(int i=0;i<1;i++){
//            helloServer.descSchool(schoolMode);
//
//            helloServer.getName("aaa");
//        }
//        System.out.println("expend time is "+ String.valueOf(new Date().getTime()-start));


    }
}
