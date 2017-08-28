package com.pairs.arch.rpc.common.rpc.registry;

/**
 * @description 负载均衡的访问策略
 * @modifytime
 */
public enum LoadBalanceStrategy {
	
	RANDOM, //随机
	WEIGHTINGRANDOM, //加权随机
	ROUNDROBIN, //轮询

}
