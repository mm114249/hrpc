package com.pairs.arch.rpc.common.spi;

import java.util.ServiceLoader;

/**
 * Created on 2017年08月10日16:37
 * SPI 加载器
 * 加载序列化框架
 * @author [hupeng]
 * @version 1.0
 **/
public class BaseServerLoader {

    public static <S> S load(Class<S> clazz){
        return ServiceLoader.load(clazz).iterator().next();
    }
}
