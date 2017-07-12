package com.pairs.arch.rpc.server.container;


public class ApplicationContextContainer implements Container {

    @Override
    public void start() {
    	ApplicationContextBuilder.getInstance();
    }

    @Override
    public void stop() {
    }

}
