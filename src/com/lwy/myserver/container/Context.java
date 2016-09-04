package com.lwy.myserver.container;

import javax.servlet.ServletContext;

import com.lwy.myserver.session.Manager;
import com.lwy.myserver.valve.ValveContext;

/**
 * context coresponds to a web app, @Host containes many contexts 
 * @author frank lee
 *
 */
public interface Context extends Container {

	public Wrapper getWrapper(String servletName);
	public Manager getManager();
	public void setValveContext(ValveContext valveContext);
	public ServletContext getServletContext();
	public String getName();
	public ClassLoader getClassLoader();
}
