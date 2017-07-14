package com.pairs.arch.rpc.server.container;

import com.pairs.arch.rpc.server.config.HrpcServerConfig;
import com.pairs.arch.rpc.server.helper.BootstrapCreaterHelper;
import com.pairs.arch.rpc.server.helper.RegisterHelper;

/**
 * Created on 2017年07月12日 16:38
 * <P>
 * Title:[]
 * </p>
 * <p>
 * Description :[]
 * </p>
 * Company:
 *
 * @author []
 * @version 1.0
 **/
public class HrpcServerContainer implements Container {

    private HrpcServerConfig hrpcServerConfig;

    public HrpcServerContainer(HrpcServerConfig hrpcServerConfig) {
        this.hrpcServerConfig = hrpcServerConfig;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {

    }
}
