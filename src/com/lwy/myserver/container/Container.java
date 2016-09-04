package com.lwy.myserver.container;

import com.lwy.myserver.http.HttpRequest;
import com.lwy.myserver.http.HttpResponse;
import com.lwy.myserver.session.Manager;
import com.lwy.myserver.valve.ValveContext;

public interface Container{
	public void invoke(HttpRequest request, HttpResponse response);
	public Manager getManager();
	public Container getSuperContainer();
}
