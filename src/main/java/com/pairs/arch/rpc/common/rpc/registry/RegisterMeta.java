package com.pairs.arch.rpc.common.rpc.registry;

import com.pairs.arch.rpc.common.transport.body.PublishServiceCustomBody;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created on 2017年08月17日14:55
 * <p>
 * Title:[]
 * </p >
 * <p>
 * Description :[provider在注册中心 注册服务的信息]
 * </p >
 * Company:
 *
 * @author [hupeng]
 * @version 1.0
 **/
public class RegisterMeta {

    private Address address = new Address();
    private String serviceName;

    // 是否该服务是VIP服务，如果该服务是VIP服务，走特定的channel，也可以有降级的服务
    private boolean isVIPService;
    // 是否支持服务降级
    private boolean isSupportDegradeService;
    // 降级服务的mock方法的路径
    private String degradeServicePath;
    // 降级服务的描述
    private String degradeServiceDesc;
    // 服务的权重
    private volatile int weight;
    // 建议连接数 hashCode()与equals()不把connCount计算在内
    private volatile int connCount;
    //审核状态
    private ServiceReviewState isReviewed=ServiceReviewState.HAS_NOT_REVIEWED;
    private boolean hasDegradeService = false;


    public RegisterMeta(Address address, String serviceName,boolean isVIPService, boolean isSupportDegradeService, String degradeServicePath,
                        String degradeServiceDesc, int weight, int connCount) {
        this.address = address;
        this.serviceName = serviceName;
        this.isVIPService = isVIPService;
        this.isSupportDegradeService = isSupportDegradeService;
        this.degradeServicePath = degradeServicePath;
        this.degradeServiceDesc = degradeServiceDesc;
        this.weight = weight;
        this.connCount = connCount;
    }


    public static RegisterMeta createRegiserMeta(PublishServiceCustomBody customBody, Channel channel){
        if(StringUtils.isBlank(customBody.getHost())){
            SocketAddress socketAddress = channel.remoteAddress();
            if(socketAddress instanceof InetSocketAddress){
                InetSocketAddress inetSocketAddress= (InetSocketAddress) socketAddress;
                customBody.setHost(inetSocketAddress.getAddress().getHostAddress());
            }
        }

        Address address=new Address(customBody.getHost(),customBody.getProt());

        RegisterMeta registerMeta=new RegisterMeta(address, customBody.getServiceProviderName(),customBody.isVIP(),
                customBody.isSupporDegrade(),customBody.getDegradeServicePath(),customBody.getDegradeServiceDesc(),
                customBody.getWeight(),customBody.getConnectCount());

        return registerMeta;

    }



    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public boolean isVIPService() {
        return isVIPService;
    }

    public void setVIPService(boolean VIPService) {
        isVIPService = VIPService;
    }

    public boolean isSupportDegradeService() {
        return isSupportDegradeService;
    }

    public void setSupportDegradeService(boolean supportDegradeService) {
        isSupportDegradeService = supportDegradeService;
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

    public int getConnCount() {
        return connCount;
    }

    public void setConnCount(int connCount) {
        this.connCount = connCount;
    }

    public ServiceReviewState getIsReviewed() {
        return isReviewed;
    }

    public void setIsReviewed(ServiceReviewState isReviewed) {
        this.isReviewed = isReviewed;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        RegisterMeta that = (RegisterMeta) obj;

        return !(address != null ? !address.equals(that.address) : that.address != null)
                && !(serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null);

    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + (serviceName != null ? serviceName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RegisterMeta [address=" + address + ", serviceName=" + serviceName + ", isVIPService=" + isVIPService + ", isSupportDegradeService="
                + isSupportDegradeService + ", degradeServicePath=" + degradeServicePath + ", degradeServiceDesc=" + degradeServiceDesc + ", weight=" + weight
                + ", connCount=" + connCount + ", isReviewed=" + isReviewed + ", hasDegradeService=" + hasDegradeService + "]";
    }



    public static class Address{
        private String host;
        private int port;

        public Address(){

        }

        public Address(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Address address = (Address) o;

            if (port != address.port) return false;
            return host.equals(address.host);

        }

        @Override
        public int hashCode() {
            int result = host.hashCode();
            result = 31 * result + port;
            return result;
        }

        @Override
        public String toString() {
            return "Address{" + "host='" + host + '\'' + ", port=" + port + '}';
        }
    }
}
