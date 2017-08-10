package com.pairs.arch.rpc.common.serializer;

import com.pairs.arch.rpc.common.spi.BaseServerLoader;

/**
 * Created on 2017年08月10日16:39
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
public class SerializerHolder {

    private static final Serializer serializer= BaseServerLoader.load(Serializer.class);

    public static Serializer serializerImpl(){
        return serializer;
    }

}
