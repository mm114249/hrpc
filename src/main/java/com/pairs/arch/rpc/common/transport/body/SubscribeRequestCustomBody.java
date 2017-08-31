package com.pairs.arch.rpc.common.transport.body;


/**
 * 
 * @description 消费者订阅服务的主题消息，这边做的相对简单，只要有唯一的名字控制就好
 * @modifytime
 */
public class SubscribeRequestCustomBody   {
	
	private String serviceName;
	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}


	

}
