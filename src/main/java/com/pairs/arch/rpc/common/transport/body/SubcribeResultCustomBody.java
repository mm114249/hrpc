package com.pairs.arch.rpc.common.transport.body;

import com.google.common.collect.Lists;
import com.pairs.arch.rpc.common.rpc.registry.LoadBalanceStrategy;
import com.pairs.arch.rpc.common.rpc.registry.RegisterMeta;
import com.pairs.arch.rpc.remote.model.CommonCustomBody;

import java.util.List;

/**
 * Created on 2017年08月23日16:39
 * <p/>
 * Title:[]
 * </p >
 * <p/>
 * Description :[消费者消息订阅返回对象]
 * </p >
 * Company:武汉灵达科技有限公司
 *
 * @author [hupeng]
 * @version 1.0
 **/
public class SubcribeResultCustomBody implements CommonCustomBody {

    private String serviceName;
    private LoadBalanceStrategy loadBalanceStrategy;
    private List<RegisterMeta> registerMeta= Lists.newArrayList();

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public LoadBalanceStrategy getLoadBalanceStrategy() {
        return loadBalanceStrategy;
    }

    public void setLoadBalanceStrategy(LoadBalanceStrategy loadBalanceStrategy) {
        this.loadBalanceStrategy = loadBalanceStrategy;
    }

    public List<RegisterMeta> getRegisterMeta() {
        return registerMeta;
    }

    public void setRegisterMeta(List<RegisterMeta> registerMeta) {
        this.registerMeta = registerMeta;
    }
}
