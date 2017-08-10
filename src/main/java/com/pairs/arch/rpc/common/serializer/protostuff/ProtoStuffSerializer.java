package com.pairs.arch.rpc.common.serializer.protostuff;

import com.pairs.arch.rpc.common.serializer.Serializer;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.springframework.objenesis.Objenesis;
import org.springframework.objenesis.ObjenesisStd;

import java.util.Map;

/**
 * Created on 2017年08月10日16:45
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
public class ProtoStuffSerializer implements Serializer {

    private Map<Class<?>,Schema<?>> schemaCache=new ConcurrentHashMap<Class<?>,Schema<?>>();
    private Objenesis objenesis = new ObjenesisStd(true);

    @Override
    public <T> byte[] writeObject(T obj) {
        Class<T> clz= (Class<T>) obj.getClass();
        LinkedBuffer buffer=LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        Schema<T> schema=getSchema(clz);
        try {
            byte[] bytes=ProtostuffIOUtil.toByteArray(obj,schema,buffer);
            return bytes;
        }finally {
            buffer.clear();
        }
    }

    @Override
    public <T> T readObject(byte[] bytes, Class<T> clazz) {
        T t=objenesis.newInstance(clazz);
        Schema<T> schema=getSchema(clazz);
        ProtostuffIOUtil.mergeFrom(bytes,t,schema);
        return t;
    }

    private <T> Schema<T> getSchema(Class<T> clazz){
        Schema<T> schema = (Schema<T>) schemaCache.get(clazz);
        if(schema==null){
            schema = RuntimeSchema.createFrom(clazz);
            schemaCache.put(clazz,schema);
        }
        return schema;
    }

}
