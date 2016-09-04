package com.lwy.myserver.valve;

import com.lwy.myserver.container.Container;
import com.lwy.myserver.http.HttpRequest;
import com.lwy.myserver.http.HttpResponse;


public interface Valve{

	public Container getContainer();
	public void invoke(HttpRequest request, HttpResponse response, ValveContext context);
}
