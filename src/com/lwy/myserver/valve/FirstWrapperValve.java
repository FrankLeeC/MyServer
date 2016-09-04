package com.lwy.myserver.valve;

import com.lwy.myserver.container.Container;
import com.lwy.myserver.http.HttpRequest;
import com.lwy.myserver.http.HttpResponse;

public class FirstWrapperValve implements Valve {

	private Container container;
	
	public FirstWrapperValve(Container container) {
		this.container = container;
	}

	@Override
	public Container getContainer() {
		return container;
	}

	@Override
	public void invoke(HttpRequest request, HttpResponse response,
			ValveContext context) {
		System.out.println("from first wrapper vavle");
		context.invoke(request, response, context);
		System.out.println("out of first wrapper valve");
	}

}
