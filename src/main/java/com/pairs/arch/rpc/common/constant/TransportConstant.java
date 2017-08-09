package com.pairs.arch.rpc.common.constant;

/**
 * Created on 2017年08月09日11:07
 * <p>
 * Title:[]
 * </p >
 * <p>
 * Description :[]
 * </p >
 * Company:
 *
 * @author [hupeng]
 * @version 1.0
 **/
public class TransportConstant {
    /** 协议头长度 */
    public static final int HEAD_LENGTH = 16;
    /** Magic */
    public static final short MAGIC = (short) 0xbabe;
    /** 发送的是请求信息*/
    public static final byte REQUEST_REMOTING = 1;
    /** 发送的是响应信息*/
    public static final byte RESPONSE_REMOTING = 2;

}
