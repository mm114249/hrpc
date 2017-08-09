package com.pairs.arch.rpc.remote.model;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created on 2017年08月09日10:49
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
public interface NettyRequestProcessor {
    RemotingTransporter processRequest(ChannelHandlerContext ctx, RemotingTransporter request)
            throws Exception;
}
