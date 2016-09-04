package com.lwy.myserver.valve;

import com.lwy.myserver.container.Container;
import com.lwy.myserver.http.HttpRequest;
import com.lwy.myserver.http.HttpResponse;

public class ThirdContextValve implements Valve {

	private Container container;
	public ThirdContextValve(){}
	public ThirdContextValve(Container container){
		this.container = container;
	}
	@Override
	public Container getContainer() {
		return container;
	}

	@Override
	public void invoke(HttpRequest request, HttpResponse response,
			ValveContext context) {
		System.out.println("=====come in third valve");
		System.out.println("=====come out third valve");
	}

}
