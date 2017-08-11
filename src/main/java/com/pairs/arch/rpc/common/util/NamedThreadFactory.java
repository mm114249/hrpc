package com.pairs.arch.rpc.common.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 2017年08月11日16:46
 * <p>
 * Title:[]
 * </p >
 * <p>
 * Description :[]
 * </p >
 * Company:武汉灵达科技有限公司
 *
 * @author [hupeng]
 * @version 1.0
 **/
public class NamedThreadFactory implements ThreadFactory {

    private static final AtomicInteger poolId=new AtomicInteger(0);
    private final AtomicInteger nextId=new AtomicInteger(0);

    private String profix;
    private boolean domain;
    private ThreadGroup threadGroup;

    public NamedThreadFactory(){
        this("pool_"+poolId.getAndIncrement());
    }

    public NamedThreadFactory(String profix){
        this(profix,false);
    }

    public NamedThreadFactory(String profix,boolean domain){
        this.profix=profix+"#";
        this.domain=domain;
        SecurityManager securityManager = System.getSecurityManager();
        this.threadGroup=securityManager==null?Thread.currentThread().getThreadGroup():securityManager.getThreadGroup();
    }


    @Override
    public Thread newThread(Runnable r) {
        Thread t=new Thread(threadGroup,r,this.profix+nextId.getAndIncrement(),0);
        if(t.isDaemon()){
            if(!domain){
                t.setDaemon(false);
            }
        }else{
            if(domain){
                t.setDaemon(true);
            }
        }
        return t;
    }
}
