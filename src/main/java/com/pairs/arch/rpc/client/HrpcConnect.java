package com.pairs.arch.rpc.client;

import io.netty.channel.Channel;

import java.util.Date;

/**
 * Created by hupeng on 2017/3/29.
 */
public class HrpcConnect {

    private String address;
    private Channel channel;
    private Long lastActive;

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
        if(new Date().getTime()-lastActive>10*1000){
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

    @Override
    public String toString() {
        return "HrpcConnect{" +
                "address='" + address + '\'' +
                ", channel=" + channel.id().asShortText() +
                ", lastActive=" + lastActive +
                '}';
    }
}
