package com.pairs.arch.rpc.common.transport.body;

import com.pairs.arch.rpc.remote.model.CommonCustomBody;

/**
 * Created on 2017年08月17日15:13
 * <p>
 * Title:[]
 * </p >
 * <p>
 * Description :[provider 提供向注册中心注册服务的网络传输对象]
 * </p >
 * Company:
 *
 * @author [hupeng]
 * @version 1.0
 **/
public class PublishServiceCustomBody implements CommonCustomBody {

    private String host;//主机地址
    private int prot;//端口号
    private String serviceProviderName;//服务名
    private boolean isVIP;//是否是vip服务,vip服务走特殊通道
    private boolean isSupporDegrade;//是否支持降级
    private String degradeServicePath;//降级后的mock服务的地址
    private String degradeServiceDesc;//mock服务降级
    private int weight;//服务权重
    private int connectCount;//建议连接数
    private long maxCallCountInMinute;//单位时间内最大的连接次数
    private boolean isFlowController;//是否限流

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getProt() {
        return prot;
    }

    public void setProt(int prot) {
        this.prot = prot;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    public boolean isVIP() {
        return isVIP;
    }

    public void setVIP(boolean VIP) {
        isVIP = VIP;
    }

    public boolean isSupporDegrade() {
        return isSupporDegrade;
    }

    public void setSupporDegrade(boolean supporDegrade) {
        isSupporDegrade = supporDegrade;
    }

    public String getDegradeServicePath() {
        return degradeServicePath;
    }

    public void setDegradeServicePath(String degradeServicePath) {
        this.degradeServicePath = degradeServicePath;
    }

    public String getDegradeServiceDesc() {
        return degradeServiceDesc;
    }

    public void setDegradeServiceDesc(String degradeServiceDesc) {
        this.degradeServiceDesc = degradeServiceDesc;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getConnectCount() {
        return connectCount;
    }

    public void setConnectCount(int connectCount) {
        this.connectCount = connectCount;
    }

    public long getMaxCallCountInMinute() {
        return maxCallCountInMinute;
    }

    public void setMaxCallCountInMinute(long maxCallCountInMinute) {
        this.maxCallCountInMinute = maxCallCountInMinute;
    }

    public boolean isFlowController() {
        return isFlowController;
    }

    public void setFlowController(boolean flowController) {
        isFlowController = flowController;
    }
}
