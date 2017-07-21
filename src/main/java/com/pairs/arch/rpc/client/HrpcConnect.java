package com.pairs.arch.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hupeng on 2017/3/29.
 */
public class HrpcConnect {

    private String address;
    private Channel channel;

    public HrpcConnect(String address, Channel channel) {
        this.address = address;
        this.channel = channel;
    }

    public void writeAndFlush(Object obj){
        channel.writeAndFlush(obj);
    }

    public Channel getChannel() {
        return channel;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "HrpcConnect{" +
                "address='" + address + '\'' +
                ", channel=" + channel +
                '}';
    }
}
