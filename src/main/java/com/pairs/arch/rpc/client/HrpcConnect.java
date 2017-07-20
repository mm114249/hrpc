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
    private Bootstrap bootstrap;

    public HrpcConnect(String address, Channel channel,Bootstrap bootstrap) {
        this.address = address;
        this.channel = channel;
        this.bootstrap=bootstrap;
    }

    public void writeAndFlush(Object obj){
        channel.writeAndFlush(obj);
    }

    public Channel getChannel() {
        return channel;
    }



    public Bootstrap getBootstrap() {
        return bootstrap;
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
