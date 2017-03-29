package com.pairs.arch.rpc.client.registry;

import com.pairs.arch.rpc.client.hanler.HrpcClientHandler;
import com.pairs.arch.rpc.common.bean.HrpcRequest;
import com.pairs.arch.rpc.common.bean.HrpcResponse;
import com.pairs.arch.rpc.common.codec.HrpcDecoder;
import com.pairs.arch.rpc.common.codec.HrpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hupeng on 2017/3/27.
 */
public class ServerDiscovery {

    private String zkAddress;
    private String path = "/hrpc";
    private CuratorFramework client;
    private static ServerDiscovery instance = new ServerDiscovery();
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);

    private ServerDiscovery() {
        zkAddress = "localhost:2181";
    }

    private ServerDiscovery(String zkAddress) {
        this.zkAddress = zkAddress;
    }


    /**
     * 服务列表
     * key:classname value:ip列表
     */
    private Map<String, List<String>> serverMap = new HashMap<String, List<String>>();
    /**
     * 远程地址列表
     * key:ip value:Channel
     */
    private Map<String, Channel> channelMap = new HashMap<String, Channel>();

    public Channel discoverServer(HrpcRequest hrpcRequest) {
        String className = hrpcRequest.getClassName();
        if (!serverMap.containsKey(className)) {
            boolean hasServer = false;
            try {
                //本地缓存中没有服务,就去zookeeper上主动发现一次
                if (client.checkExists().forPath(path + "/" + className)!= null) {
                    List<String> childrens=client.getChildren().forPath(path + "/" + className);
                    if(CollectionUtils.isNotEmpty(childrens)){
                        for(String c:childrens){
                            String ip=new String(client.getData().forPath(path+"/"+className+"/"+c));
                            serverRegister(className,ip);
                            hasServer=true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(!hasServer){
                throw new RuntimeException("not server provide");
            }

        }

        List<String> addressList = serverMap.get(className);
        int index = (int) (Math.random() * addressList.size());
        String address = addressList.get(index);

        if (!channelMap.containsKey(address)) {
            serverRegister(className, address);
        }

        return channelMap.get(address);
    }


    private void registerZookeeper() {
        client = CuratorFrameworkFactory.newClient(zkAddress, 5000, 5000, new ExponentialBackoffRetry(1000, 3));
        client.start();

        TreeCache treeCache = null;//递归的监听根节点下的子节点,包括孙子节点
        try {
            if (client.checkExists().forPath(path) == null) {
                client.create().forPath(path);
            }
            treeCache = new TreeCache(client, path);
            treeCache.start();

            treeCache.getListenable().addListener(new TreeCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
                    if (TreeCacheEvent.Type.NODE_ADDED == event.getType()) {
                        if (event.getData().getPath().split("/").length == 4) {
                            String className = event.getData().getPath().split("/")[2];
                            String ip = new String(event.getData().getData());
                            serverRegister(className, ip);
                        }
                    } else if (TreeCacheEvent.Type.NODE_REMOVED == event.getType()) {
                        if (event.getData().getPath().split("/").length == 4) {
                            String className = event.getData().getPath().split("/")[3];
                            String ip = new String(event.getData().getData());
                            unRegister(className, ip);
                        }
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private void serverRegister(String className, String ip) {
        if (serverMap.containsKey(className)
                && serverMap.get(className).contains(ip)
                && channelMap.containsKey(ip)) {
            //已经注册了,不需要在创建channel了
            return;
        }

        if (!serverMap.containsKey(className)) {
            List<String> ips = new ArrayList<String>();
            ips.add(ip);
            serverMap.put(className, ips);
        } else {
            List<String> ips = serverMap.get(className);
            if (!ips.contains(ip)) {
                ips.add(ip);
            }
        }
        createConnect(ip);

    }

    private void unRegister(String className, String address) {
        if (!serverMap.containsKey(className)) {
            return;
        }

        serverMap.get(className).remove(address);
    }


    /**
     * 创建 链接
     *
     * @param address
     */
    private void createConnect(final String address) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .remoteAddress(address.split(":")[0], Integer.valueOf(address.split(":")[1]))
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new HrpcDecoder(HrpcResponse.class));
                        socketChannel.pipeline().addLast(new HrpcEncoder(HrpcRequest.class));
                        socketChannel.pipeline().addLast(new HrpcClientHandler());
                    }
                });

        try {
            ChannelFuture channelFuture = bootstrap.connect().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        if (!channelMap.containsKey(address)) {
                            channelMap.put(address, channelFuture.channel());
                        }
                    }
                }
            }).sync();
            channelFuture.channel().closeFuture();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static ServerDiscovery getInstance() {
        instance.registerZookeeper();
        return instance;
    }

    public void consoleMessage() {
        StringBuffer serverBuffer = new StringBuffer();
        for (Map.Entry<String, List<String>> entry : serverMap.entrySet()) {
            serverBuffer.append("{");
            serverBuffer.append(entry.getKey() + ",");
            serverBuffer.append("ips [");
            for (String ip : entry.getValue()) {
                serverBuffer.append(ip + ",");
            }
            serverBuffer.append("]");
            serverBuffer.append("},");
        }
        System.out.println(serverBuffer.toString());


        StringBuffer channelStringBuffer = new StringBuffer();
        for (Map.Entry<String, Channel> entry : channelMap.entrySet()) {
            channelStringBuffer.append("{");
            channelStringBuffer.append("ip:" + entry.getKey() + "--->");
            channelStringBuffer.append("channel Id:" + entry.getValue().id().asShortText());
            channelStringBuffer.append("},");
        }

        System.out.println(channelStringBuffer.toString());

    }


    public static void main(String[] args) throws InterruptedException {
        getInstance();

        while (true) {
            Thread.sleep(4000);
            getInstance().consoleMessage();
        }

    }


}
