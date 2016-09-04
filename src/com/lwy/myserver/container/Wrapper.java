package com.lwy.myserver.container;

import com.lwy.myserver.valve.ValveContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

/**
 * hold a servlet, @Context contains many wrappers
 * @author frank lee
 *
 */
public interface Wrapper extends Container {

	public HttpServlet getServlet();
	public String getServletName();
	public void setValveContext(ValveContext valveContext);
	public void setName(String name); //set specified name in web application configuration
	public ServletContext getServletContext();
}
