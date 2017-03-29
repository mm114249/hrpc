package com.pairs.arch.rpc.channelpool;

import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPoolHandler;

/**
 * Created by hupeng on 2017/3/27.
 */
public class SimChannelPoolHanlder implements ChannelPoolHandler {

    public void channelReleased(Channel channel) throws Exception {
        System.out.println("channel pool Released");
    }

    public void channelAcquired(Channel channel) throws Exception {
        System.out.println("channel pool Acquired");
    }

    public void channelCreated(Channel channel) throws Exception {
        System.out.println("channel pool Created");
    }
}
