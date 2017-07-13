package com.pairs.arch.rpc.client.config;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * Created on 2017年07月13日 9:15
 * <P>
 * Title:[]
 * </p>
 * <p>
 * Description :[]
 * </p>
 * Company:武汉灵达科技有限公司
 *
 * @author [hupeng]
 * @version 1.0
 **/
public class HrpcClientConfig {

    private String rootPath="/hrpc";
    private String zkAddress="127.0.0.1:2181";
    private Integer idelTime=5;//空闲链路检查时间
    private EventExecutorGroup eventExecutorGroup;//处理rpc业务的线程数量

    public HrpcClientConfig(){

    }

    public HrpcClientConfig(String zkAddress) {
       this(zkAddress,2);
    }

    public HrpcClientConfig( String zkAddress,Integer executorCount) {
        this.zkAddress = zkAddress;
        this.eventExecutorGroup=new DefaultEventExecutorGroup(executorCount);
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getZkAddress() {
        return zkAddress;
    }

    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    public Integer getIdelTime() {
        return idelTime;
    }

    public void setIdelTime(Integer idelTime) {
        this.idelTime = idelTime;
    }

    public EventExecutorGroup getEventExecutorGroup() {
        return eventExecutorGroup;
    }
}
