package com.pairs.arch.rpc.remote.model;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created on 2017年08月09日14:56
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
public interface NettyChannelnactiveProcessor {

    void processChannelInacitve(ChannelHandlerContext ctx) throws InterruptedException;

}
