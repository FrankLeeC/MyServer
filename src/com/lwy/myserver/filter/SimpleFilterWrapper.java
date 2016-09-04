package com.lwy.myserver.filter;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.lwy.myserver.container.Container;
import com.lwy.myserver.container.Context;

public class SimpleFilterWrapper implements FilterConfig, Filter {
	private Container container; //@SimpleContext
	private Filter filter;
	private String name;
	private String className;

	public SimpleFilterWrapper(Filter filter,Container container) {
		this.filter = filter;
		this.container = container;
	}
	
	/**
	 * not in the same package with programmer's custom filter, so they won't access this method
	 * 与程序员写的程序不在一个包，所有他们获取不到
	 * @return
	 */
	protected Filter getFilter(){
		return filter;
	}

	@Override
	public String getFilterName() {
		if(name != null)
			return name;
		return className;
	}

	@Override
	public ServletContext getServletContext() {
		return ((Context)container).getServletContext();
	}

	@Override
	public String getInitParameter(String name) {
		return null;
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return null;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		filter.init(this);
	}

	@Override
	public void doFilter(HttpServletRequest request, ServletResponse response,
						 FilterChain chain) throws IOException, ServletException {
		filter.doFilter(request, response, chain);
	}

	@Override
	public void destroy() {
		filter.destroy();
	}

}
