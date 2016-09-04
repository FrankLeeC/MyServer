package com.lwy.myserver.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionListener;

import com.lwy.myserver.container.Context;
import com.lwy.myserver.listener.LifecycleListener;

/**
 * singleton
 * all listeners are singleton
 * 所有的listener都是单例
 * @author frank lee
 *
 */
public class ListenerFactory {
	
	/**
	 * listeners' key is context path
	 * listener的key是context path 也就是web app 的名字
	 */
	private static Map<String,Map<Class<? extends EventListener>,List<EventListener>>> listeners = new ConcurrentHashMap<>();
	private static ListenerFactory instance = create();

	
	private ListenerFactory(){}
	
	private static ListenerFactory create(){
		return new ListenerFactory();
	}
	
	public static ListenerFactory getInstance(){
		return instance;
	}
	
	/**
	 * create listeners' instances of specified context
	 * @param context 
	 * @return
	 */
	public void createListenerInstance(Context context){
		String name = context.getName();
		Map<Class<? extends EventListener>,List<EventListener>> listenerMap = listeners.get(name);
		if(listenerMap == null){
			listenerMap = new ConcurrentHashMap<>();
			ClassLoader loader = context.getClassLoader();
			Collection<Map<String,String>> listenerClass = Configuration.getListeners(name).values();
			Iterator<Map<String,String>> iterator = listenerClass.iterator();
			List<EventListener> lifecycleListeners = new ArrayList<>();
			List<EventListener> servletContextAttributeListeners = new ArrayList<>();
			List<EventListener> servletContextListeners = new ArrayList<>();
			List<EventListener> servletRequestAttributeListeners = new ArrayList<>();
			List<EventListener> servletRequestListeners = new ArrayList<>();
			List<EventListener> httpSessionBindingListeners = new ArrayList<>();
			List<EventListener> httpSessionListeners = new ArrayList<>();
			while(iterator.hasNext()){
				Map<String,String> map = iterator.next();
				String className = map.get(Keys.CLASS);
				//this check is important,web.xml may contain redundant empty configuration
				if(className != null && !"".equals(className)){ 
					try {
						EventListener listener = (EventListener) loader.loadClass(className).newInstance();
//						System.out.println(ServletContextListener.class.getClassLoader());
//						System.out.println(listener+" "+listener.getClass().getClassLoader()+" "+EventListener.class.getClassLoader());
//						System.out.println(listener instanceof ServletContextListener);
						if(listener instanceof LifecycleListener)                         //1
							lifecycleListeners.add(listener);
						else if(listener instanceof ServletContextAttributeListener)       //2
							servletContextAttributeListeners.add(listener);
						else if(listener instanceof ServletContextListener)            //3
							servletContextListeners.add(listener);
						else if(listener instanceof ServletRequestAttributeListener)       //4
							servletRequestAttributeListeners.add(listener);
						else if(listener instanceof ServletRequestListener)             //5
							servletRequestListeners.add(listener);
						else if(listener instanceof HttpSessionBindingListener)          //6
							httpSessionBindingListeners.add(listener);
						else if(listener instanceof HttpSessionListener)                       //7
							httpSessionListeners.add(listener);
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
			listenerMap.put(LifecycleListener.class, lifecycleListeners);                      //1
			listenerMap.put(ServletContextAttributeListener.class, servletContextAttributeListeners);       //2
			listenerMap.put(ServletContextListener.class, servletContextListeners);                //3
			listenerMap.put(ServletRequestAttributeListener.class, servletRequestAttributeListeners);            //4
			listenerMap.put(ServletRequestListener.class, servletRequestListeners);                  //5
			listenerMap.put(HttpSessionBindingListener.class, httpSessionBindingListeners);          //6
			listenerMap.put(HttpSessionListener.class, httpSessionListeners);                         //7
			listeners.put(name, listenerMap);
		}
	}
	
	public Map<Class<? extends EventListener>,List<EventListener>> getListenerInstance(Context context){
		return listeners.get(context.getName());
	}

}
