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

    public static void start(){
        List<Container> containerList = new ArrayList<Container>();
        containerList.add(new ApplicationContextContainer());// 启动spring容器
        ContainerHelper.start(containerList);
    }

    public static void main(String[] args) {
        start();
    }

}
