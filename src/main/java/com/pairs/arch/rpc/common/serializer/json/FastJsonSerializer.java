package com.pairs.arch.rpc.common.serializer.json;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.pairs.arch.rpc.common.serializer.Serializer;

/**
 * Created on 2017年08月10日17:30
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
public class FastJsonSerializer implements Serializer {


    @Override
    public <T> byte[] writeObject(T obj) {
        byte[] bytes = JSONObject.toJSONBytes(obj, SerializerFeature.SortField);
        return bytes;
    }

    @Override
    public <T> T readObject(byte[] bytes, Class<T> clazz) {
        return JSONObject.parseObject(bytes,clazz);
    }
}
