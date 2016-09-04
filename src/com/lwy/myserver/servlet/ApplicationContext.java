package com.lwy.myserver.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import com.lwy.myserver.container.Context;
import com.lwy.myserver.container.SimpleContext;
import com.lwy.myserver.lifecycle.Lifecycle;
import com.lwy.myserver.listener.InternalListener;
import com.lwy.myserver.listener.InternalListenerExecutor;
import com.lwy.myserver.listener.ListenerExecutor;
import com.lwy.myserver.listener.SimpleListenerExecutor;
import com.lwy.myserver.util.Constants;

/**
 * implementation of ServletContext
 * @author frank lee
 *
 */
public class ApplicationContext implements ServletContext, Lifecycle {
	
	private Map<String,Object> attributes;
	private Map<String,String> initParameters;
	private Context context; //@SimpleContext
	private ListenerExecutor internalListenerExecutor = InternalListenerExecutor.getInstance();
	private ListenerExecutor simpleListenerExecutor;
	public ApplicationContext(Context context){
		this.context = context;
		simpleListenerExecutor = new SimpleListenerExecutor.Builder().build(context);
	}
	
	@Override
	public ServletContext getContext(String uripath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContextPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMajorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMinorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getEffectiveMajorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getEffectiveMinorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getMimeType(String file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getResourcePaths(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Servlet getServlet(String name) throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<Servlet> getServlets() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getServletNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void log(String msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void log(Exception exception, String msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void log(String message, Throwable throwable) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getRealPath(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServerInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInitParameter(String name) {
		return  initParameters.get(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		Set<String> set = initParameters.keySet();
		String[] s = new String[set.size()];
		String[] t = set.toArray(s);
		return new Enumeration<String>(){
			private int count = 0;
			@Override
			public boolean hasMoreElements() {
				return count<t.length;
			}
			@Override
			public String nextElement() {
				return t[count++];
			}		
		};
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getAttribute(String name) {
		
		return attributes.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		Set<String> set = attributes.keySet();
		String[] s = new String[set.size()];
		String[] t = set.toArray(s);
		return new Enumeration<String>(){
			private int count = 0;
			@Override
			public boolean hasMoreElements() {
				return count<t.length;
			}
			@Override
			public String nextElement() {
				return t[count++];
			}		
		};
	}

	@Override
	public void setAttribute(String name, Object object) {
		if(attributes.containsKey(name))
			simpleListenerExecutor.executeListener(Constants.SERVLET_CONTEXT_ATTRIBUTE_REPLACE
								, new ServletContextAttributeEvent(this,name,object));
		else
			simpleListenerExecutor.executeListener(Constants.SERVLET_CONTEXT_ATTRIBUTE_ADD
								, new ServletContextAttributeEvent(this,name,object));
		attributes.put(name, object);
	}

	@Override
	public void removeAttribute(String name) {
		if(attributes.containsKey(name)){// must check, or listener will fail and programmers will be confused(mengbi)
			simpleListenerExecutor.executeListener(Constants.SERVLET_CONTEXT_ATTRIBUTE_REMOVE
								, new ServletContextAttributeEvent(this,name,attributes.get(name)));
			attributes.remove(name);
		}
	}

	@Override
	public String getServletContextName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dynamic addServlet(String servletName, String className) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dynamic addServlet(String servletName, Servlet servlet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dynamic addServlet(String servletName,
			Class<? extends Servlet> servletClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> c)
			throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletRegistration getServletRegistration(String servletName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(
			String filterName, String className) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(
			String filterName, Filter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(
			String filterName, Class<? extends Filter> filterClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> c)
			throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FilterRegistration getFilterRegistration(String filterName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSessionTrackingModes(
			Set<SessionTrackingMode> sessionTrackingModes)
			throws IllegalStateException, IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addListener(String className) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends EventListener> void addListener(T t) {
		addListeners(null, t);
	}

	@Override
	public void addListener(Class<? extends EventListener> listenerClass) {
		
	}

	@Override
	public <T extends EventListener> T createListener(Class<T> c)
			throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void declareRoles(String... roleNames) {
		// TODO Auto-generated method stub

	}

	@Override
	public ClassLoader getClassLoader() {
		return ((SimpleContext)context).getClassLoader();
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getVirtualServerName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void start() {
		attributes = new ConcurrentHashMap<>();
		initParameters = new ConcurrentHashMap<>();
		executeListener(Constants.INTERNAL_BEFORE_START,this);
		executeListener(Constants.SERVLETCONTEXT_INITIALIZATION,this);
		executeListener(Constants.INTERNAL_AFTER_START,this);
	}

	@Override
	public void stop() {
		executeListener(Constants.INTERNAL_BEFORE_STOP,this);
		executeListener(Constants.SERVLETCONTEXT_DESTROY,this);
		executeListener(Constants.INTERNAL_AFTER_STOP,this);
	}

	@Override
	public void removeListener(Class<? extends EventListener> clazz, EventListener listener) {
		if(listener instanceof InternalListener)
			internalListenerExecutor.removeListener(clazz, listener);
		else
			simpleListenerExecutor.removeListener(clazz, listener);
	}

	@Override
	public void executeListener(String status, Object data) {
		if(status.startsWith("INTERNAL_"))
			internalListenerExecutor.executeListener(status, data);
		else
			simpleListenerExecutor.executeListener(status, data);
	}

	@Override
	public void addListeners(Class<? extends EventListener> clazz, EventListener listener) {
		if(listener instanceof InternalListener)
			internalListenerExecutor.addListener(clazz, listener);
		else
			simpleListenerExecutor.addListener(clazz, listener);
	}

}
