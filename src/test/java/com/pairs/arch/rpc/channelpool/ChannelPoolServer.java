package com.pairs.arch.rpc.channelpool;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * Created by hupeng on 2017/3/27.
 */
public class ChannelPoolServer {

    private void createServer(){

        ServerBootstrap bt=new ServerBootstrap();
        EventLoopGroup boss=new NioEventLoopGroup(1);
        EventLoopGroup worker=new NioEventLoopGroup(4);

        bt.group(boss,worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new ChannelServerHanlder());
                    }
                });
        ChannelFuture channelFuture=null;
        try {
            channelFuture=bt.bind(8081).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(channelFuture.isSuccess()){
                        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181", 5000, 5000, new ExponentialBackoffRetry(1000, 3));
                        client.start();

                        String path="/hrpc";
                        try {
                            client.delete().deletingChildrenIfNeeded().forPath(path);
                            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path+"/com.rpc.nameServer/server"
                                    ,"127.0.0.1:8081".getBytes());
                            Thread.sleep(5000);

                            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path+"/com.rpc.ageServer/server"
                                    ,"127.0.0.1:8081".getBytes());
                            Thread.sleep(20000);

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        new ChannelPoolServer().createServer();
    }

}
