package com.lwy.myserver.container;

import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import com.lwy.myserver.http.HttpRequest;
import com.lwy.myserver.http.HttpResponse;
import com.lwy.myserver.session.Manager;
import com.lwy.myserver.valve.ValveContext;

public class SimpleWrapper implements Wrapper,ServletConfig {
	private ValveContext valveContext;
	private String className;
	private String name;
	private HttpServlet servlet;
	private Container supContainer;
	private Map<String,String> configParameters = new ConcurrentHashMap<>(); //save ServletConfig name/value

	public SimpleWrapper(String name, HttpServlet servlet, Container supContainer) {
		this.className = name;
		this.servlet = servlet;
		this.supContainer = supContainer;
	}
	

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public void invoke(HttpRequest request, HttpResponse response) {
		valveContext.invoke(request, response, valveContext);
	}

	@Override
	public HttpServlet getServlet() {
		return servlet;
	}

	@Override
	public void setValveContext(ValveContext valveContext) {
		this.valveContext =valveContext;
	}

	@Override
	public Manager getManager() {
		return supContainer.getManager();
	}
	
	/**
     * Returns the name of this servlet instance.
     * The name may be provided via server administration, assigned in the
     * web application deployment descriptor, or for an unregistered (and thus
     * unnamed) servlet instance it will be the servlet's class name.
     *
     * @return	the name of the servlet instance
     */
	@Override
	public String getServletName(){
		if(name != null)
			return name;
		return className;
	}


	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return ((Context)supContainer).getServletContext();
	}
	
	public void setInitParameter(String name,String value){
		configParameters.put(name, value);
	}

	@Override
	public String getInitParameter(String name) {
		return configParameters.get(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		Set<String> set = configParameters.keySet();
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
	public Container getSuperContainer() {
		return supContainer;
	}


}
