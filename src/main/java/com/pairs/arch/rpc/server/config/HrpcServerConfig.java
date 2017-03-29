package com.pairs.arch.rpc.server.config;

import com.pairs.arch.rpc.common.bean.HrpcRequest;
import com.pairs.arch.rpc.common.bean.HrpcResponse;
import com.pairs.arch.rpc.common.codec.HrpcDecoder;
import com.pairs.arch.rpc.common.codec.HrpcEncoder;
import com.pairs.arch.rpc.server.annotation.HrpcServer;
import com.pairs.arch.rpc.server.handler.HrpcHandler;
import com.pairs.arch.rpc.server.util.ClassScaner;
import com.pairs.arch.rpc.server.util.ServerWrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
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
import org.apache.zookeeper.KeeperException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by hupeng on 2017/3/28.
 */
public class HrpcServerConfig {

    private Integer serverPort = 8010;
    private List<String> serverPackage = new ArrayList<String>();
    private String zkAddress = "127.0.0.1:2181";
    private String rootPath = "/hrpc";

    private static HrpcServerConfig instance=new HrpcServerConfig();

    private HrpcServerConfig(){

    }


    private void run(){
        serverRegister();
        createBootstrap();
    }

    /**
     * 启动netty 服务
     */
    private void createBootstrap(){
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(4);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(new HrpcDecoder(HrpcRequest.class));
                            channel.pipeline().addLast(new HrpcEncoder(HrpcResponse.class));
                            channel.pipeline().addLast(new HrpcHandler());
                        }
                    });

            ChannelFuture future = null;
            try {
                future = bootstrap.bind(serverPort).sync();
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }


    /**
     * 服务启动注册
     * 服务初始化的时候需要执行
     */
    private void serverRegister() {
        CuratorFramework client = CuratorFrameworkFactory
                .newClient(zkAddress, 3000, 3000, new ExponentialBackoffRetry(500, 3));
        client.start();

        Set<Class<?>> classSet = ClassScaner.scanerAnnotation(serverPackage);

        String ip = getIp();//获得本机IP
        String address = ip + ":" + serverPort.toString();
        try {
            for (Class<?> entity : classSet) {
                String interfaceName=entity.getAnnotation(HrpcServer.class).value().getName();
                client.create().creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                        .forPath(rootPath + "/" + interfaceName + "/server", address.getBytes());
                ServerWrap.addServer(interfaceName,entity);
            }

        } catch (KeeperException.ConnectionLossException lossException) {
            System.out.println("not zookeeper server to connect");
            System.exit(1);//连接不上zookeeper,项目就结束
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String getIp() {
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String ip = addr.getHostAddress();//获得本机IP

        return ip;
    }



    public static HrpcServerConfig getInstance(Integer serverPort,String zkAddress,List<String> packages){
        instance.setServerPort(serverPort);
        instance.setZkAddress(zkAddress);
        getInstance(packages);
        return instance;
    }

    public static HrpcServerConfig getInstance(List<String> packages){
        instance.setServerPackage(packages);
        instance.run();
        return instance;
    }



    private void setServerPackage(List<String> packages) {
        serverPackage.addAll(packages);
    }


    private void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    private void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }
}
