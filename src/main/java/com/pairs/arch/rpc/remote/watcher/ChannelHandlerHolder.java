package com.pairs.arch.rpc.remote.watcher;

import io.netty.channel.ChannelHandler;

/**
 * Created on 2017年08月11日17:38
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
public interface ChannelHandlerHolder {

    ChannelHandler[] handlers();

}
