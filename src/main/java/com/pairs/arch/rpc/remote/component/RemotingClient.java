package com.pairs.arch.rpc.remote.component;

import com.pairs.arch.rpc.remote.model.NettyChannelnactiveProcessor;
import com.pairs.arch.rpc.remote.model.NettyRequestProcessor;
import com.pairs.arch.rpc.remote.model.RemotingTransporter;

import java.util.concurrent.ExecutorService;

/**
 * Created on 2017年08月11日14:33
 * <p>
 * Title:[]
 * </p >
 * <p>
 * Description :[]
 * </p >
 * Company:
 *
 * @author [hupeng]
 * @version 1.0
 **/
public interface RemotingClient extends BaseRemotingService {

    /**
     * 向远端发送请求
     * @param address
     * @param request
     * @param timeout
     * @return
     */
    RemotingTransporter invokeSync(final String address,final RemotingTransporter request,final long timeout);

    /**
     * 注册处理器,不同的requestCode 使用不同的处理器来处理。便于程序扩展，修改业务逻辑只需要修改处理器里的代码即可
     * 不需要影响handler的代码。
     * @param requestCode
     * @param processor
     * @param executor
     */
    void registerProcess(final byte requestCode, final NettyRequestProcessor processor, final ExecutorService executor);
    /**注册通道失效处理器*/
    void registerChannelInactiveProcess(final NettyChannelnactiveProcessor processor,final ExecutorService executor);
    /**某个地址的channel是否可写*/
    boolean isChannelWriteable(final String address);
    /**是否重连设置*/
    void setReconnect(final boolean isReconnect);

}
