package com.lwy.myserver.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class SimpleFilterChain implements FilterChain {
	private List<Filter> filters = null;
	private static ThreadLocal<Integer> locals = new ThreadLocal<>();
	public SimpleFilterChain(List<Filter> filters) {
		this.filters = filters;
	}

	@Override
	public void doFilter(HttpServletRequest request, ServletResponse response)
			throws IOException, ServletException {
		Integer index = locals.get();
		if(index == null)
			index = new Integer(0);
		Filter filter = filters.get(index++);
		 // this line must be above next line, otherwise the next filter will preform and index hasn't been set
		locals.set(index);
		filter.doFilter(request, response, this);
	}

}
