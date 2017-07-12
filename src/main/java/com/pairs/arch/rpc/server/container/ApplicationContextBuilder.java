package com.pairs.arch.rpc.server.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationContextBuilder {

    private static ApplicationContextBuilder instance;

    private AbstractApplicationContext appContext;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    public synchronized static ApplicationContextBuilder getInstance() {
        if (instance == null) {
            instance = new ApplicationContextBuilder();
        }
        return instance;
    }

    private ApplicationContextBuilder() {
        try {
            appContext = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        } catch (BeansException ex) {
            logger.error("无法从xml路径中获得ApplicationContext",ex);
        }
    }

    public Object getBean(String name) {
        return appContext.getBeanFactory().getBean(name);
    }

    public <T> T getBean(Class<T> clazz) {
        return appContext.getBeanFactory().getBean(clazz);
    }

    public void close() {
        appContext.close();
    }

    public AbstractApplicationContext getAppContext() {
        return appContext;
    }
}
