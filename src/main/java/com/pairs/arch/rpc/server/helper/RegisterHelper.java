package com.pairs.arch.rpc.server.helper;

import com.google.common.base.Joiner;
import com.pairs.arch.rpc.server.annotation.HrpcServer;
import com.pairs.arch.rpc.server.config.HrpcServerConfig;
import com.pairs.arch.rpc.server.container.ApplicationContextBuilder;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.annotation.Annotation;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Created on 2017年07月11日 18:15
 * <P>
 * Title:[]
 * </p>
 * <p>
 * Description :[]
 * </p>
 * Company:
 *
 * @author [hupeng]
 * @version 1.0
 **/
public class RegisterHelper implements InitializingBean,ApplicationContextAware  {

    private static Logger logger=Logger.getLogger(RegisterHelper.class);
    private HrpcServerConfig hrpcServerConfig;
    private ApplicationContext applicationContext;


    public RegisterHelper(){

    }

    public RegisterHelper(HrpcServerConfig hrpcServerConfig) {
        this.hrpcServerConfig = hrpcServerConfig;
    }

    /**
     * 服务启动注册
     * 服务初始化的时候需要执行
     */
    public void serverRegister() {
        CuratorFramework client = CuratorFrameworkFactory.newClient(hrpcServerConfig.getZkAddress(), 3000, 3000, new ExponentialBackoffRetry(500, 3));
        client.start();
        Joiner joiner=Joiner.on("/").skipNulls();
        logger.debug(String.format("---connect zookeeper starts。address is :%s---",hrpcServerConfig.getZkAddress()));

        Map<String, Object> beanMap =applicationContext.getBeansWithAnnotation(HrpcServer.class);

        String ip = getIp();//获得本机IP
        String address = ip + ":" + hrpcServerConfig.getServerPort().toString();

        logger.debug(String.format("---server register on zookeeper 。address is :%s---",address));

        try {
            if(beanMap!=null){
                for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
                    HrpcServer s=entry.getValue().getClass().getAnnotation(HrpcServer.class);
                    Class clzz=s.value();
                    String interfaceName=clzz.getName();
                    client.create().creatingParentsIfNeeded()
                            .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                            .forPath(joiner.join(hrpcServerConfig.getRootPath(),interfaceName,"server"), address.getBytes());
                }
            }
        } catch (KeeperException.ConnectionLossException lossException) {
            logger.error("--------not zookeeper server to connect-------");
            System.exit(1);//连接不上zookeeper,项目就结束
        } catch (Exception e) {
            logger.error(e);
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

    public void setHrpcServerConfig(HrpcServerConfig hrpcServerConfig) {
        this.hrpcServerConfig = hrpcServerConfig;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        serverRegister();
    }
}
