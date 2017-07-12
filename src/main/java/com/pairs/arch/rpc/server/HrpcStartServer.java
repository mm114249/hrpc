package com.pairs.arch.rpc.server;

import com.pairs.arch.rpc.server.config.HrpcServerConfig;
import com.pairs.arch.rpc.server.container.ApplicationContextContainer;
import com.pairs.arch.rpc.server.container.Container;
import com.pairs.arch.rpc.server.container.ContainerHelper;
import com.pairs.arch.rpc.server.container.HrpcServerContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2017年07月12日 16:10
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
public class HrpcStartServer {

    public static void start(HrpcServerConfig hrpcServerConfig){
        List<Container> containerList = new ArrayList<Container>();
        containerList.add(new ApplicationContextContainer());// 启动spring容器
        containerList.add(new HrpcServerContainer(hrpcServerConfig));
        ContainerHelper.start(containerList);
    }

    public static void main(String[] args) {
        HrpcServerConfig hrpcServerConfig=new HrpcServerConfig();
        hrpcServerConfig.setZkAddress("127.0.0.1:2181");
        hrpcServerConfig.setServerPort(7070);
        start(hrpcServerConfig);
    }

}
