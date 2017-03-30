package com.pairs.arch.rpc.server.config;

import com.pairs.arch.rpc.server.annotation.HrpcServer;
import com.pairs.arch.rpc.server.helper.BootstrapCreaterHelper;
import com.pairs.arch.rpc.server.util.ClassScaner;
import com.pairs.arch.rpc.server.util.ServerWrap;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hupeng on 2017/3/28.
 */
public class HrpcServerConfig  {

    private Integer serverPort = 8010;
    private List<String> serverPackage = new ArrayList<String>();
    private String zkAddress = "127.0.0.1:2181";
    private String rootPath = "/hrpc";
    private ExecutorService executorService= Executors.newFixedThreadPool(1);
    private static Logger logger=Logger.getLogger(HrpcServerConfig.class);

    private static HrpcServerConfig instance = new HrpcServerConfig();



    private HrpcServerConfig() {

    }

    private void serverRun() {
        //将服务注册到zookeeper上
        serverRegister();
        //启动netty服务
        executorService.execute(new BootstrapCreaterHelper(instance.serverPort));
    }



    /**
     * 服务启动注册
     * 服务初始化的时候需要执行
     */
    private void serverRegister() {
        CuratorFramework client = CuratorFrameworkFactory
                .newClient(zkAddress, 3000, 3000, new ExponentialBackoffRetry(500, 3));
        client.start();

        if(logger.isDebugEnabled()){
            logger.debug(String.format("---connect zookeeper starts。address is :%s---",zkAddress));
        }

        Set<Class<?>> classSet = ClassScaner.scanerAnnotation(serverPackage);

        String ip = getIp();//获得本机IP
        String address = ip + ":" + serverPort.toString();

        if(logger.isDebugEnabled()){
            logger.debug(String.format("---server register on zookeeper 。address is :%s---",address));
        }

        try {
            for (Class<?> entity : classSet) {
                String interfaceName = entity.getAnnotation(HrpcServer.class).value().getName();

                client.create().creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                        .forPath(rootPath + "/" + interfaceName + "/server", address.getBytes());
                ServerWrap.addServer(interfaceName, entity);
            }

        } catch (KeeperException.ConnectionLossException lossException) {
            logger.error("--------not zookeeper server to connect-------");
            System.exit(1);//连接不上zookeeper,项目就结束
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String getIp() {
        InetAddress addr = null;
        String ip="";
        try {
            addr = InetAddress.getLocalHost();
            ip = addr.getHostAddress();//获得本机IP
        } catch (UnknownHostException e) {
            logger.error(e);
        }

        return ip;
    }


    public static HrpcServerConfig getInstance(Integer serverPort, String zkAddress, List<String> packages) {
        instance.setServerPort(serverPort);
        instance.setZkAddress(zkAddress);
        getInstance(packages);
        return instance;
    }

    public static HrpcServerConfig getInstance(List<String> packages) {
        instance.setServerPackage(packages);
        instance.serverRun();
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
