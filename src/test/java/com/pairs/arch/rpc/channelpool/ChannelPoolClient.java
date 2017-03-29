package com.pairs.arch.rpc.channelpool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * Created by hupeng on 2017/3/27.
 */
public class ChannelPoolClient {

    public void createClient(){
        final Bootstrap bootstrap=new Bootstrap();
        EventLoopGroup eventLoopGroup=new NioEventLoopGroup();

        InetSocketAddress addr1 = new InetSocketAddress("localhost", 8081);
        InetSocketAddress addr2 = new InetSocketAddress("localhost", 8081);

        bootstrap.remoteAddress(addr1);
        bootstrap.remoteAddress(addr2);
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class);
        //   .handler(new ChannelServerHanlder());

//        ChannelPoolMap<InetSocketAddress, SimpleChannelPool> poolMap = new AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool>() {
//            @Override
//            protected SimpleChannelPool newPool(InetSocketAddress key) {
//                System.out.println(1111);
//                return new SimpleChannelPool(bootstrap.remoteAddress(key), new SimChannelPoolHanlder());
//            }
//        };

        ChannelPool pool = new FixedChannelPool(bootstrap, new SimChannelPoolHanlder(), 2, Integer.MAX_VALUE);

        Channel channel = pool.acquire().syncUninterruptibly().getNow();
        System.out.println(channel.id());



        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        ChannelPoolClient client=new ChannelPoolClient();
        client.createClient();
    }
}
