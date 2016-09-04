package com.lwy.myserver.server;

import com.lwy.myserver.config.Configuration;
import com.lwy.myserver.connector.HttpConnector;
import com.lwy.myserver.container.Container;
import com.lwy.myserver.container.Host;
import com.lwy.myserver.container.SimpleHost;
import com.lwy.myserver.lifecycle.Lifecycle;
import com.lwy.myserver.session.Manager;
import com.lwy.myserver.session.SessionManager;

import java.util.EventListener;

/**
 * server
 * this class's loader class should be CommonClassLoader,then all class will be loaded by CommonClassLoader
 * 这个类必须被CommonClassLoader加载，这样除了bootstrap，所有相关的server的类都是被CommonClassLoader加载，
 * 不会出现类无法转换、无法传值的问题
 * Created by frank lee on 7/11/2016.
 */
public class SimpleServer implements Server,Lifecycle{
    private boolean init = false;

    /**
     * set thread context class loader to CommonClassLoader
     * 设置线程上下文的类加载器为CommonClassLoader
     */
    private void init(){
        Thread.currentThread().setContextClassLoader(SimpleServer.class.getClassLoader());
        Configuration.config();
        init = true;
    }

    @Override
    public void start() {
        if(!init)
            init();
        HttpConnector connector = new HttpConnector();
        Container container = new SimpleHost();
        Manager manager = new SessionManager();
        ((Host)container).setManager(manager);// set session manager
        connector.setContainer(container);
        ((Lifecycle)connector).start();
        ((Lifecycle)container).start();
    }

    @Override
    public void stop() {

    }

    @Override
    public void addListeners(Class<? extends EventListener> clazz, EventListener listener) {

    }

    @Override
    public void removeListener(Class<? extends EventListener> clazz, EventListener listener) {

    }

    @Override
    public void executeListener(String status, Object data) {

    }
}
