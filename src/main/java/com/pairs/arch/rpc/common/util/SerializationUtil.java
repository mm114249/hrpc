package com.pairs.arch.rpc.common.util;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hupeng on 2017/3/24.
 */
public class SerializationUtil {

    private static Map<Class<?>, Schema<?>> cacheSchema = new ConcurrentHashMap<Class<?>, Schema<?>>();

    private static <T> Schema<T> getSchema(Class<T> clazz) {
        if (cacheSchema.containsKey(clazz)) {
            return (Schema<T>) cacheSchema.get(clazz);
        }
        RuntimeSchema<T> schema = RuntimeSchema.createFrom(clazz);
        cacheSchema.put(clazz, schema);
        return schema;
    }

    public static <T> byte[] serialize(T obj) {
        Class<?> clazz = obj.getClass();
        Schema<T> schema = (Schema<T>) getSchema(clazz);
        LinkedBuffer buffer=LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            byte[] bytes = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
            return bytes;
        }catch (Exception e){
            throw new IllegalStateException("error param",e);
        }finally {
            buffer.clear();
        }
    }

    public static <T> T deserialize(byte[] data,Class<T> clazz){
        Schema<T> schema=getSchema(clazz);
        T t=schema.newMessage();
        ProtostuffIOUtil.mergeFrom(data,t,schema);
        return t;
    }


}
