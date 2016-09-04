package com.lwy.myserver.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import com.lwy.myserver.config.Configuration;
import com.lwy.myserver.container.Context;
import com.lwy.myserver.container.Wrapper;

/**
 * singleton
 * this filter is the last filter in filter chain, add it automatically
 * after the last filter which defined by programmer processes chain.doFilter(...),this filter will process
 * after process, it will process servlet.service method
 * 单例
 * filter chain中最后一个filter，自动添加上去，当程序员的最后要给filter执行了chain.foFilter()之后，这个filter会工作
 * 工作之后，就回促发servlet的service方法
 * @author frank lee
 *
 */
public class StandardFilter implements Filter {
	
	private Context context;
	private static StandardFilter instance = null;

	public static StandardFilter getInstance(Context context){
		if(instance == null){
			synchronized(context){
				if(instance == null){
					instance = new StandardFilter(context);
				}
			}
		}
		return instance;
	}
	
	private StandardFilter(Context context) {
		this.context = context;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}

	@Override
	public void doFilter(HttpServletRequest request, ServletResponse response,
						 FilterChain chain) throws IOException, ServletException {
		String pattern = ((HttpServletRequest)request).getRequestURI();
		if(!pattern.endsWith(".do")){
			int index = pattern.indexOf(".do");
			pattern = pattern.substring(0,index+3);
		}
		String specifiedName = Configuration.getServletName(context.getName(),pattern);
		String className = Configuration.getServletClassName(context.getName(),specifiedName);
		Wrapper wrapper = context.getWrapper(className);
		HttpServlet servlet = wrapper.getServlet();
		ServletConfig config = (ServletConfig) wrapper;
		servlet.init(config);
		servlet.service(request, response);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
