package com.lwy.myserver.container;

import java.io.IOException;
import java.util.EventListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.JspPage;

import com.lwy.myserver.config.Configuration;
import com.lwy.myserver.config.FilterFactory;
import com.lwy.myserver.connector.HttpExecutor;
import com.lwy.myserver.http.HttpRequest;
import com.lwy.myserver.http.HttpResponse;
import com.lwy.myserver.jsp.JspParser;
import com.lwy.myserver.jsp.SimpleJspFactory;
import com.lwy.myserver.lifecycle.Lifecycle;
import com.lwy.myserver.listener.SimpleListenerExecutor;
import com.lwy.myserver.loader.SimpleClassLoader;
import com.lwy.myserver.servlet.ApplicationContext;
import com.lwy.myserver.session.Manager;
import com.lwy.myserver.util.Constants;
import com.lwy.myserver.valve.*;

/**
 * only one wrapper, so be care of concurrency
 * @author frank lee	
 *
 */
public class SimpleContext implements Context,Lifecycle {

	private Host host; //@Host
	private ServletContext context;//ServletContext
	private JspApplicationContext jspApplicationContext;
	private JspFactory jspFactory;
	private JspParser parser;
	private SimpleListenerExecutor listenerExecutor = null;
	private ClassLoader classLoader;
	private ValveContext valves;
	private HttpExecutor executor;
	private String name; // name of this context    each web app has an unique context

	// wrapper map
	private final static Map<String,Wrapper> wrappers = new ConcurrentHashMap<>(); 
	// wrapper ValveContext map
	private final static Map<String,ValveContext> wrapperValveContext = new ConcurrentHashMap<>();
	private FilterFactory filfactory = null;

	/**
	 * get WebAppClassLoader first
	 * @param name
	 * @param host
     */
	public SimpleContext(String name,Host host){
		this.name = name;
		this.host = host;
		getClassLoader();
		Thread.currentThread().setContextClassLoader(classLoader);
		listenerExecutor = new SimpleListenerExecutor.Builder().build(this);
	}
	
	public HttpExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(HttpExecutor executor) {
		this.executor = executor;
	}
	
	public void activateAsync(){
		executor.activateAsync();
	}
	
	/**
	 * thread-safe
	 */
	@Override
	public void invoke(HttpRequest request, HttpResponse response) {
		classLoader = getClassLoader();
		String pattern = request.getRequestURI();
		if(pattern.endsWith(".jsp")){ //request for jsp 
			int len = request.getContextPath().length()+1;
			String jspPath = pattern.substring(len); //e.g: before:/bbs/user/login.jsp   after:/user/login.jsp
			JspPage jsp = parser.getJspServlet(name,jspPath,classLoader);
			try {
				jsp.service(request,response);
			} catch (ServletException | IOException e) {
				e.printStackTrace();
			}
		}
		else { //request for servlet
			if (!pattern.endsWith(".do")) {
				int index = pattern.indexOf(".do");
				pattern = pattern.substring(0, index + 3);
			}
			String specifiedName = Configuration.getServletName(name, pattern);
			String className = Configuration.getServletClassName(name, specifiedName);
			Wrapper wrapper = getWrapper(className);
			wrapper.setName(specifiedName);
			valves.invoke(request, response, valves);
			wrapper.invoke(request, response);
		}
	}
	
	/**
	 * thread-safe 
	 * @see SimpleClassLoader-getLoader()
	 * @return
	 */
	@Override
	public ClassLoader getClassLoader(){
		if(classLoader == null){
			classLoader = SimpleClassLoader.getInstance().getLoader(name);
		}
		return classLoader;
	}
	
	private Wrapper createWrapper(String servletName){
		Wrapper wrapper = null;
		try {
			Class<?> clazz = classLoader.loadClass(servletName);
			HttpServlet servlet = (HttpServlet) clazz.newInstance();
			wrapper = new SimpleWrapper(servletName,servlet,this);
			ValveContext context = getWrapperValveContext(wrapper);
			wrapper.setValveContext(context);
			wrappers.put(servletName, wrapper);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			System.err.println("can't find servlet named:"+servletName);
			e.printStackTrace();
		}	
		return wrapper;
	}
	
	@Override
	public Wrapper getWrapper(String servletName){
		Wrapper wrapper = wrappers.get(servletName);
		if(wrapper == null){
			synchronized(wrappers){
				if(wrapper == null){
					wrapper = createWrapper(servletName);
				}
			}
		}
		return wrapper;
	}

	private ValveContext createWrapperValveContext(Wrapper wrapper){
		ValveContext context = new CommonValveContext(wrapper);//SimpleWrapper
		Valve first = new FirstWrapperValve(wrapper);
		Valve second = new SecondWrapperValve(wrapper);
		Valve third = new ThirdWrapperValve(wrapper);
		context.addValve(first);
		context.addValve(second);
		context.addValve(third);
		wrapperValveContext.put(wrapper.getServletName(), context);
		return context;
	}
	
	private ValveContext getWrapperValveContext(Wrapper wrapper){
		ValveContext context = wrapperValveContext.get(wrapper.getServletName());
		if(context == null){
			synchronized(wrapperValveContext){
				if(context == null){
					context = createWrapperValveContext(wrapper);
				}
			}
		}
		return context;
	}
	
	public void setValveContext(ValveContext valves){
		this.valves = valves;
	}
	
	@Override
	public String getName() {
		return name;
	}

	
	@Override
	public ServletContext getServletContext(){
		return context;
	}

	@Override
	public Manager getManager() {
		return host.getManager();
	}

	/**
	 * start context, activate listeners
	 * create servlet context, activate listeners
	 * create JspFactory
	 * create JspApplicationContext and JspParser
	 * create filters, invoke init(FilterConfig)
	 */
	@Override
	public void start() {
		executeListener(Constants.INTERNAL_BEFORE_START,null);
		context = new ApplicationContext(this);
		jspFactory = new SimpleJspFactory();
		jspApplicationContext = jspFactory.getJspApplicationContext(context);
		parser = new JspParser(jspFactory,jspApplicationContext);
		((Lifecycle) context).start();
		filfactory = FilterFactory.getInstance();
		filfactory.fireFilterInstance(this);
		executeListener(Constants.INTERNAL_AFTER_START,null);
	}

	@Override
	public void stop() {
		executeListener(Constants.INTERNAL_BEFORE_STOP,this);
		executeListener(Constants.INTERNAL_AFTER_STOP,this);
	}

	@Override
	public void addListeners(Class<? extends EventListener> clazz, EventListener listener) {
		listenerExecutor.addListener(clazz,listener);
	}

	@Override
	public void removeListener(Class<? extends EventListener> clazz, EventListener listener) {
		listenerExecutor.removeListener(clazz, listener);
	}

	@Override
	public void executeListener(String status,Object data) {
		listenerExecutor.executeListener(status, data);
	}


	@Override
	public Container getSuperContainer() {
		return host;
	}
	
}
