package com.pairs.arch.rpc.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by hupeng on 2017/3/28.
 */
public class SyncLock {

    private static SyncLock instance = new SyncLock();

    //key:request uuid  value:线程的锁
    //定义一个普通的cache,不需要用到load
    private Cache<String, CountDownLatch> cache = CacheBuilder.newBuilder()
            .maximumSize(1000).expireAfterAccess(5, TimeUnit.SECONDS)
            .build();

    public CountDownLatch get(String uuid) {
        return cache.getIfPresent(uuid);
    }

    public void put(String uuid, CountDownLatch latch) {
        cache.put(uuid, latch);
    }

    private SyncLock() {

    }

    public static SyncLock getInstance() {
        return instance;
    }

}
