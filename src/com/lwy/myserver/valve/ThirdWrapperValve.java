package com.lwy.myserver.valve;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.ServletException;

import com.lwy.myserver.config.Configuration;
import com.lwy.myserver.config.FilterFactory;
import com.lwy.myserver.container.Container;
import com.lwy.myserver.container.Context;
import com.lwy.myserver.container.SimpleWrapper;
import com.lwy.myserver.filter.SimpleFilterChain;
import com.lwy.myserver.filter.SimpleFilterWrapper;
import com.lwy.myserver.filter.StandardFilter;
import com.lwy.myserver.http.HttpRequest;
import com.lwy.myserver.http.HttpResponse;

public class ThirdWrapperValve implements Valve {

	private Container container; //SimpleWrapper @Container/@ServletConfig
	
	public ThirdWrapperValve(Container container) {
		this.container = container;
	}

	@Override
	public Container getContainer() {
		return container;
	}

	@Override
	public void invoke(HttpRequest request, HttpResponse response,
			ValveContext context) {
		System.out.println("from third wrapper valve");
		String name = ((SimpleWrapper)container).getServletName();
		Container supContainer = container.getSuperContainer();
		String contextName = ((Context)supContainer).getName();
		String pattern = Configuration.getServletPattern(contextName,name);
		List<Filter> filters = FilterFactory.getFiltersByNames(Configuration.getFilterNamesByPattern(contextName,pattern)
				,(Context) container.getSuperContainer());
		StandardFilter sinstance = StandardFilter.getInstance((Context)supContainer);
		Filter instance = new SimpleFilterWrapper(sinstance,supContainer);
		try {
			instance.init(null);
		} catch (ServletException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		filters.add(instance);
		SimpleFilterChain sfc = new SimpleFilterChain(filters);
		try {
			sfc.doFilter(request, response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("out of third wrapper valve");
	}

}
