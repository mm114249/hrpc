package com.pairs.arch.rpc.common.serializer;

/**
 * Created on 2017年08月10日16:40
 * <p>
 * Title:[]
 * </p >
 * <p>
 * Description :[]
 * </p >
 * Company:武汉灵达科技有限公司
 *
 * @author [hupeng]
 * @version 1.0
 **/
public interface Serializer {

    /**序列化消息*/
    <T> byte[] writeObject(T obj);
    /**反序列化消息*/
    <T> T readObject(byte[] bytes,Class<T> clazz);


}
