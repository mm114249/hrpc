package com.pairs.arch.rpc.registry.defaultRegistry;

import com.pairs.arch.rpc.remote.model.RemotingTransporter;
import io.netty.channel.Channel;

/**
 * @description 注册中心处理provider的服务接口
 */
public interface RegistryProviderServer {

	/**
	 * 处理provider发送过来的注册信息
	 * @param remotingTransporter 里面的CommonCustomBody 是#PublishServiceCustomBody
	 * @param channel
	 * @return
	 * @throws InterruptedException 
	 */
	RemotingTransporter handlerRegister(RemotingTransporter remotingTransporter, Channel channel) throws InterruptedException;
}
