package com.pairs.arch.rpc.client.discovery;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.pairs.arch.rpc.client.HrpcConnect;
import com.pairs.arch.rpc.client.config.HrpcClientConfig;
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
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.collections.CollectionUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by hupeng on 2017/3/27.
 */
public class ServerDiscovery {

    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    private boolean isRun = false;//服务是否启动,服务启动了就不在执行zookeeper注册和空闲链路检查注册了
    private Logger logger=Logger.getLogger(ServerDiscovery.class);
    private Joiner joiner=Joiner.on("/").skipNulls();
    private CuratorFramework client;
    private HrpcClientConfig hrpcClientConfig;

    /**
     * 服务列表
     * key:classname value:ip列表
     */
    private static Map<String, List<String>> serverMap = new HashMap<String, List<String>>();
    /**
     * 远程地址列表
     * key:ip value:Channel
     * <p>
     * 维护一个单独的ip列表,是为了方便检查ip时候已经建立channel
     */
    private static Map<String, HrpcConnect> channelMap = new HashMap<String, HrpcConnect>();

    public ServerDiscovery(HrpcClientConfig hrpcClientConfig) {
        this.hrpcClientConfig = hrpcClientConfig;
        ServerDiscoveryWarp.setServerDiscovery(this);
    }

    /**
     * 得到一个rpc服务
     * @param hrpcRequest
     * @return
     */
    public HrpcConnect getServer(HrpcRequest hrpcRequest) {
        String className = hrpcRequest.getClassName();
        if (!serverMap.containsKey(className)) {
            //主动去发现一次服务
            discoveryAndCache(className);
        }

        List<String> addressList = serverMap.get(className);
        int index = (int) (Math.random() * addressList.size());
        String address = addressList.get(index);
        if(!channelMap.containsKey(address)){
            createServer(className,address);
        }
        return channelMap.get(address);
    }


    public static HrpcConnect getConnect(Channel channel){
        for (Map.Entry<String, HrpcConnect> entry : channelMap.entrySet()) {
            if(channel.id().asShortText().equals(entry.getValue().getChannel().id().asShortText())){
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 断线重连的时候,需要先删除当前链接
     * @param channel
     */
    public static void removeConnect(Channel channel){
        String removeKey="";
        for (Map.Entry<String, HrpcConnect> entry : channelMap.entrySet()) {
            if(channel.id().asShortText().equals(entry.getValue().getChannel().id().asShortText())){
                removeKey=entry.getKey();
                break;
            }
        }
        channelMap.remove(removeKey);
    }

    /**
     * 添加一个链路
     * @param hrpcConnect
     */
    public static void addConnect(HrpcConnect hrpcConnect){
        channelMap.put(hrpcConnect.getAddress(),hrpcConnect);
    }


    /**
     * 删除链接
     * @param channel
     */
    public void removeServer(Channel channel){
        String removeKey="";
        for (Map.Entry<String, HrpcConnect> entry : channelMap.entrySet()) {
            if(channel.id().asShortText().equals(entry.getValue().getChannel().id().asShortText())){
                removeKey=entry.getKey();
                break;
            }
        }
        channelMap.remove(removeKey);
    }

    /**
     * 提供给应用来获取服务
     * @param className
     */
    private void discoveryAndCache(String className){
        boolean hasServer = false;
        try {
            //本地缓存中没有服务,就去zookeeper上主动发现一次
            if (getZkClient().checkExists().forPath(joiner.join(hrpcClientConfig.getRootPath(),className)) != null) {
                List<String> childrens = getZkClient().getChildren().forPath(joiner.join(hrpcClientConfig.getRootPath(),className));
                if (CollectionUtils.isNotEmpty(childrens)) {
                    for (String c : childrens) {
                        String ip = new String(getZkClient().getData().forPath(joiner.join(hrpcClientConfig.getRootPath(),className,c)));
                        createServer(className, ip);
                        hasServer = true;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("",e);
        }

        if (!hasServer) {
            throw new RuntimeException("not server provide");
        }
    }


    /**
     * 创建服务
     * @param className
     * @param ip
     */
    private void createServer(String className, String ip) {
        if (serverMap.containsKey(className) && serverMap.get(className).contains(ip) && channelMap.containsKey(ip)) {
            //已经缓存了channel,不需要在创建channel了
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

    /**
     * 监听zookeeper节点，当有节点失效的时候，自动去关闭channel，删除掉维护的节点信息
     * @param className
     * @param address
     */
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
    private  void createConnect(final String address) {
        final Bootstrap bootstrap=new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .remoteAddress(address.split(":")[0], Integer.valueOf(address.split(":")[1]))
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new HrpcDecoder(HrpcResponse.class));
                        socketChannel.pipeline().addLast(new IdleStateHandler(hrpcClientConfig.getIdelTime(), hrpcClientConfig.getIdelTime(), hrpcClientConfig.getIdelTime()));
                        socketChannel.pipeline().addLast(hrpcClientConfig.getEventExecutorGroup(),new HrpcClientHandler(hrpcClientConfig.getIdelTime()));
                        socketChannel.pipeline().addLast(new HrpcEncoder(HrpcRequest.class));
                    }
                });
        try {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            ChannelFuture channelFuture = bootstrap.connect().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        HrpcConnect hrpcConnect = new HrpcConnect(address, channelFuture.channel(),bootstrap);
                        channelMap.put(address, hrpcConnect);
                        countDownLatch.countDown();
                    }
                }
            });
            countDownLatch.await(1, TimeUnit.SECONDS);
            channelFuture.channel().closeFuture();
        } catch (InterruptedException e) {
            logger.error(e);
        }
    }


    public void consoleMessage() {
        Map<String, List<Map<String, String>>> consoleMap = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : serverMap.entrySet()) {
            List<Map<String, String>> list = new ArrayList<>();
            for (String ip : entry.getValue()) {
                Map<String, String> atomMap = new LinkedHashMap<>();
                if (channelMap.containsKey(ip)) {
                    atomMap.put(ip, channelMap.get(ip).getChannel().id().asShortText());
                }else{
                    atomMap.put(ip, "");
                }
                list.add(atomMap);
            }
            consoleMap.put(Iterables.getLast(Splitter.on(".").split(entry.getKey())),list);
        }

        if(logger.isDebugEnabled()){
            logger.debug(JSONObject.toJSONString(consoleMap));
        }
    }


    /**
     * 连接zookeeper
     */
    public void registerZookeeper() {
        String rootPath=hrpcClientConfig.getRootPath();
        TreeCache treeCache = null;//递归的监听根节点下的子节点,包括孙子节点
        try {
            if (getZkClient().checkExists().forPath(rootPath) == null) {
                getZkClient().create().forPath(rootPath);
            }
            treeCache = new TreeCache(getZkClient(), rootPath);
            treeCache.start();

            treeCache.getListenable().addListener(new TreeCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
                    if (TreeCacheEvent.Type.NODE_ADDED == event.getType()) {
                        //自动去发现新注册上来的服务
                        if (event.getData().getPath().split("/").length == 4) {
                            String className = event.getData().getPath().split("/")[2];
                            String ip = new String(event.getData().getData());
                            createServer(className, ip);
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
            logger.error(e);
        }
    }

    private CuratorFramework getZkClient(){
        if(client==null){
            client = CuratorFrameworkFactory.newClient(hrpcClientConfig.getZkAddress(), 5000, 5000, new ExponentialBackoffRetry(1000, 3));
            client.start();
        }
        return client;
    }

}