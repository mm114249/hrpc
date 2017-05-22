package com.pairs.arch.rpc.client;

import io.netty.channel.Channel;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hupeng on 2017/3/29.
 */
public class HrpcConnect {

    private String address;
    private Channel channel;
    private Long lastActive;
    private AtomicInteger idelTotal=new AtomicInteger(0);

    public static final Integer IDEL_TIME=5;//空闲检查一次的时间

    public HrpcConnect(String address, Channel channel, Long lastActive) {
        this.address = address;
        this.channel = channel;
        this.lastActive = lastActive;
    }


    public void writeAndFlush(Object obj){
        this.lastActive=new Date().getTime();
        channel.writeAndFlush(obj);
    }

    /**
     * 空闲连接检测
     * @return
     */
    public boolean isIdle(){
        if(new Date().getTime()-lastActive>100*1000){
            return true;
        }else {
            return false;
        }
    }

    public void close(){
        this.channel.close();
    }

    public Channel getChannel() {
        return channel;
    }


    public AtomicInteger getIdelTotal() {
        return idelTotal;
    }

    public void setIdelTotal(AtomicInteger idelTotal) {
        this.idelTotal = idelTotal;
    }


    @Override
    public String toString() {
        return "HrpcConnect{" +
                "address='" + address + '\'' +
                ", channel=" + channel +
                ", lastActive=" + lastActive +
                ", idelTotal=" + idelTotal +
                '}';
    }
}
