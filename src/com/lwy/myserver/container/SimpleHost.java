package com.lwy.myserver.container;

import com.lwy.myserver.config.Configuration;
import com.lwy.myserver.http.HttpRequest;
import com.lwy.myserver.http.HttpResponse;
import com.lwy.myserver.jsp.SimpleJspFactory;
import com.lwy.myserver.lifecycle.Lifecycle;
import com.lwy.myserver.listener.InternalListenerExecutor;
import com.lwy.myserver.listener.ListenerExecutor;
import com.lwy.myserver.loader.SimpleClassLoader;
import com.lwy.myserver.session.Manager;
import com.lwy.myserver.valve.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.JspPage;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleHost implements Host, Lifecycle{
	
	private ConcurrentHashMap<String,Context> contexts = new ConcurrentHashMap<>();
	private Manager manager;
	private ValveContext valveContext;
	private ListenerExecutor listenerExecutor = InternalListenerExecutor.getInstance();

	@Override
	public void invoke(HttpRequest request, HttpResponse response) {
		valveContext.invoke(request, response, valveContext);
		Container context = getContext(request.getContextPath());
		context.invoke(request,response);
	}

	/**
	 * first valve can bound or create session for request
	 * second valve can separate requests into two types:
	 * 		1.servlet request
	 * 		2.jsp request
	 */
	private void createValveContext() {
		valveContext = new CommonValveContext(this);
		Valve valve = new SessionValve(this);
		valveContext.addValve(valve);
	}

	@Override
	public Manager getManager() {
		return manager;
	}

	@Override
	public void addContext(Context context) {
		contexts.put(context.getName(), context);
	}

	/**
	 * create context and bound valves to it
	 * @param name
	 * @return
     */
	@Override
	public Context getContext(String name) {
		Context context = contexts.get(name);
		if(context != null){
			ValveContext valveContext = new CommonValveContext(context); //SimpleContext
			Valve first = new FirstContextValve(context);
			Valve second = new SecondContextValve(context);
			Valve third = new ThirdContextValve(context);
			valveContext.addValve(first);
			valveContext.addValve(second);
			valveContext.addValve(third);
			context.setValveContext(valveContext);
			((Lifecycle)context).start();
			return contexts.get(name);
		}
		return null;
	}

	/**
	 * 1.create valve context
	 * 2.start up session manager
	 * 3.set up all context
	 * 4.set up JspFactory
	 * later,change to this mode:each context is a thread，let context implements Runnable
	 * 以后改成这种模式：一个context为一个线程 let context implements Runnable
	 */
	@Override
	public void start() {
		createValveContext();
		((Lifecycle) manager).start();
		List<String> contextNames = Configuration.getApps();
		for(String name:contextNames){
			ClassLoader webAppLoader = SimpleClassLoader.getInstance().getLoader(name);
			try {
				Class<?> clazz = webAppLoader.loadClass("com.lwy.myserver.container.SimpleContext");
				Constructor<?> constructor = clazz.getDeclaredConstructor(String.class,Host.class);
				constructor.setAccessible(true);
				Container context = (Container) constructor.newInstance(name,this);
				((Lifecycle)context).start();
				addContext((Context) context);
			} catch (ClassNotFoundException | InstantiationException | NoSuchMethodException | IllegalAccessException | 					 InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stop() {
		((Lifecycle) manager).start();
	}

	@Override
	public void addListeners(Class<? extends EventListener> clazz,
			EventListener listener) {
		listenerExecutor.addListener(clazz, listener);
	}

	@Override
	public void removeListener(Class<? extends EventListener> clazz,
			EventListener listener) {
		listenerExecutor.removeListener(clazz, listener);
	}

	@Override
	public void executeListener(String status, Object data) {
		listenerExecutor.executeListener(status, data);
	}

	@Override
	public void setManager(Manager manager) {
		this.manager = manager;
	}

	@Override
	public Container getSuperContainer() {
		// TODO Auto-generated method stub
		return null;
	}

}
