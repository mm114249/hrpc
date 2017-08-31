package com.pairs.arch.rpc.common.rpc.registry;


import java.util.ArrayList;
import java.util.List;

/**
 * @description 注册中心的记录的数据去持久化的数据
 */
public class RegistryPersistRecord {
	
	private String serviceName;
	//负载均衡策略
	private LoadBalanceStrategy balanceStrategy;
	
	private List<PersistProviderInfo> providerInfos = new ArrayList<PersistProviderInfo>();
	
	public RegistryPersistRecord() {
	}
	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public LoadBalanceStrategy getBalanceStrategy() {
		return balanceStrategy;
	}

	public void setBalanceStrategy(LoadBalanceStrategy balanceStrategy) {
		this.balanceStrategy = balanceStrategy;
	}

	public List<PersistProviderInfo> getProviderInfos() {
		return providerInfos;
	}

	public void setProviderInfos(List<PersistProviderInfo> providerInfos) {
		this.providerInfos = providerInfos;
	}


	public static class PersistProviderInfo {
		
		private RegisterMeta.Address address;
		
		private ServiceReviewState isReviewed = ServiceReviewState.PASS_REVIEW;
		
		public PersistProviderInfo() {
		}
		
		public RegisterMeta.Address getAddress() {
			return address;
		}

		public void setAddress(RegisterMeta.Address address) {
			this.address = address;
		}

		public ServiceReviewState getIsReviewed() {
			return isReviewed;
		}

		public void setIsReviewed(ServiceReviewState isReviewed) {
			this.isReviewed = isReviewed;
		}

		
	}

}
