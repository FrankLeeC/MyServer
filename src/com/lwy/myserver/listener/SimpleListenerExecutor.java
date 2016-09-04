package com.lwy.myserver.listener;

import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.lwy.myserver.config.ListenerFactory;
import com.lwy.myserver.container.Context;
import com.lwy.myserver.exceptions.InvalidListenerException;
import com.lwy.myserver.util.Constants;

/**
 * all listener are invoked here
 * every context has one and only one listener executor 
 * simple listeners are listeners defined in javaee-api e.g.: @ServletContextListener
 * @author frank lee
 */
public class SimpleListenerExecutor implements ListenerExecutor{
	/**
	 * @SimpleContext
	 */
	private Context context;
	private static Map<Class<? extends EventListener>,List<EventListener>> listeners = null;
	private static List<EventListener> lifecycleListeners;
	private static List<EventListener> servletContextAttributeListeners;
	private static List<EventListener> servletContextListeners;
	private static List<EventListener> servletRequestAttributeListeners;
	private static List<EventListener> servletRequestListeners;
	private static List<EventListener> httpSessionBindingListeners;
	private static List<EventListener> httpSessionListeners;
	
	/**
	 * every context contains one and only one @SimpleListenerExecutor
	 * @author frank lee
	 *
	 */
	public static class Builder{
		private final static Map<String,SimpleListenerExecutor> instances = new ConcurrentHashMap<>();
		public Builder(){}
		public SimpleListenerExecutor build(Context context){
			ListenerFactory factory = ListenerFactory.getInstance();
			SimpleListenerExecutor instance = instances.get(context.getName());
			if(instance == null){
				synchronized(instances){
					if(instance == null){
						instance = new SimpleListenerExecutor(context,factory);
					}
					instances.put(context.getName(), instance);
				}
			}
			return instance;	
		}
	}
	
	/**
	 * first, we create instance of listeners
	 * second, get instance of context
	 * third, separate listeners to lists order by type
	 * @param context this context
	 * @param factory factory
	 */
	private SimpleListenerExecutor(Context context,ListenerFactory factory){
		this.context = context;
		factory.createListenerInstance(context);
		listeners = factory.getListenerInstance(context);
		lifecycleListeners = listeners.get(LifecycleListener.class);
		servletContextAttributeListeners = listeners.get(ServletContextAttributeListener.class);
		servletContextListeners = listeners.get(ServletContextListener.class);
		servletRequestAttributeListeners = listeners.get(ServletRequestAttributeListener.class);
		servletRequestListeners = listeners.get(ServletRequestListener.class);
		httpSessionBindingListeners = listeners.get(HttpSessionBindingListener.class);
		httpSessionListeners = listeners.get(HttpSessionListener.class);
	}
	
	public Context getContext(){
		return context;
	}
	
	public synchronized void addListener(Class<? extends EventListener> clazz,EventListener listener){
		List<EventListener> list = listeners.get(clazz);
		if(list == null){
			try {
				throw new InvalidListenerException("no such listener named:"+clazz.getName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else
			list.add(listener);
	}  
	
	public synchronized void removeListener(Class<? extends EventListener> clazz,EventListener listener){
		List<EventListener> list = listeners.get(clazz);
		if(list != null)
			list.remove(listener);
	}
	
	public void executeListener(String status,Object data){
		switch(status){
			case Constants.INTERNAL_BEFORE_START:
				for(EventListener listener:lifecycleListeners)
					((LifecycleListener) listener).beforeStartEvent(new LifecycleEvent(data));
				break;
			case Constants.INTERNAL_PROCESS_START:
				for(EventListener listener:lifecycleListeners)
					((LifecycleListener) listener).processStartEvent(new LifecycleEvent(data));
				break;
			case Constants.INTERNAL_AFTER_START:
				for(EventListener listener:lifecycleListeners)
					((LifecycleListener) listener).afterStartEvent(new LifecycleEvent(data));
				break;
			case Constants.INTERNAL_BEFORE_STOP:
				for(EventListener listener:lifecycleListeners)
					((LifecycleListener) listener).beforeStopEvent(new LifecycleEvent(data));
				break;
			case Constants.INTERNAL_PROCESS_STOP:
				for(EventListener listener:lifecycleListeners)
					((LifecycleListener) listener).processStopEvent(new LifecycleEvent(data));
				break;
			case Constants.INTERNAL_AFTER_STOP:
				for(EventListener listener:lifecycleListeners)
					((LifecycleListener) listener).afterStopEvent(new LifecycleEvent(data));
				break;
			case Constants.SESSION_CREATION:
				for(EventListener listener:httpSessionListeners)
					((HttpSessionListener) listener).sessionCreated(new HttpSessionEvent((HttpSession)data));
				break;
			case Constants.SESSION_DESTROY:
				for(EventListener listener:httpSessionListeners)
					((HttpSessionListener) listener).sessionDestroyed(new HttpSessionEvent((HttpSession)data));
				break;
			case Constants.SERVLETCONTEXT_INITIALIZATION:
				for(EventListener listener:servletContextListeners)
					((ServletContextListener) listener).contextInitialized(new ServletContextEvent((ServletContext) data));
				break;
			case Constants.SERVLETCONTEXT_DESTROY:
				for(EventListener listener:servletContextListeners)
					((ServletContextListener) listener).contextDestroyed(new ServletContextEvent((ServletContext)data));
				break;
			case Constants.SERVLET_CONTEXT_ATTRIBUTE_ADD:
				for(EventListener listener:servletContextAttributeListeners)
					((ServletContextAttributeListener) listener).attributeAdded((ServletContextAttributeEvent)(data));
				break;
			case Constants.SERVLET_CONTEXT_ATTRIBUTE_REMOVE:
				for(EventListener listener:servletContextAttributeListeners)
					((ServletContextAttributeListener) listener).attributeRemoved((ServletContextAttributeEvent) data);
				break;
			case Constants.SERVLET_CONTEXT_ATTRIBUTE_REPLACE:
				for(EventListener listener:servletContextAttributeListeners)
					((ServletContextAttributeListener) listener).attributeReplaced((ServletContextAttributeEvent) data);
				break;
			case Constants.REQUEST_ATTRIBUTE_ADD:
				for(EventListener listener:servletRequestAttributeListeners)
					((ServletRequestAttributeListener) listener).attributeAdded((ServletRequestAttributeEvent)data);
				break;
			case Constants.REQUEST_ATTRIBUTE_REMOVE:
				for(EventListener listener:servletRequestAttributeListeners)
					((ServletRequestAttributeListener) listener).attributeRemoved((ServletRequestAttributeEvent)data);
				break;
			case Constants.REQUEST_ATTRIBUTE_REPLACE:
				for(EventListener listener:servletRequestAttributeListeners)
					((ServletRequestAttributeListener) listener).attributeReplaced((ServletRequestAttributeEvent)data);
				break;
			case Constants.REQUEST_CREATE:
				for(EventListener listener:servletRequestListeners)
					((ServletRequestListener) listener).requestInitialized((ServletRequestEvent)data);
				break;
			case Constants.REQUEST_DESTROY:
				for(EventListener listener:servletRequestListeners)
					((ServletRequestListener) listener).requestDestroyed((ServletRequestEvent)data);
				break;
			case Constants.SESSION_VALUE_BOUND:
				for(EventListener listener:httpSessionBindingListeners)
					((HttpSessionBindingListener) listener).valueBound((HttpSessionBindingEvent)data);
				break;
			case Constants.SESSION_VALUE_UNBOUND:
				for(EventListener listener:httpSessionBindingListeners)
					((HttpSessionBindingListener) listener).valueUnbound((HttpSessionBindingEvent)data);
				break;
			default:
				break;	
		}
	}
}
