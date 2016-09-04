package com.lwy.myserver.util;

import java.io.File;

public class Constants {

	public static final String SYSTEM = System.getProperty("user.dir");
	public static final String INTERNALJAR = SYSTEM + "\\lib";
	public static final String SERVER_JAVAEE = SYSTEM + "\\out\\production\\MyServer";
	public static final String JSP_ROOT = SYSTEM + File.separator + "WebContent" + File.separator + "jsp";
	public static final String WEBROOT	= SYSTEM + File.separator + "WebContent" + File.separator + "webapps";
	public static final String INTERNAL_BEFORE_START = "INTERNAL_BEFORE_START"; ////before lifecycle start
	public static final String INTERNAL_PROCESS_START = "INTERNAL_PROCESS_START"; //during lifecycle start
	public static final String INTERNAL_AFTER_START = "INTERNAL_AFTER_START"; //after lifecycle start
	public static final String INTERNAL_BEFORE_STOP = "INTERNAL_BEFORE_STOP"; //before lifecycle stop
	public static final String INTERNAL_PROCESS_STOP = "INTERNAL_PROCESS_STOP"; //during lifecycle stop
	public static final String INTERNAL_AFTER_STOP = "INTERNAL_AFTER_STOP"; //after lifecycle stop
	
	public static final int SESSION_INTERVAL = 2*60; //session interval(in second)
	public static final String SSID = "SSID"; // session id 
	public static final String SESSION_CREATION = "SESSION_CREATION"; //@HttpSession creation
	public static final String SESSION_DESTROY = "SESSION_DESTROY"; //@HttpSession destroy
	public static final String SERVLETCONTEXT_INITIALIZATION = "SERVLETCONTEXT_INITIALIZATION"; //@ServletContext initialization 
	public static final String SERVLETCONTEXT_DESTROY = "SERVLETCONTEXT_DESTROY"; //@ServletContext destroy
	public static final String SERVLET_CONTEXT_ATTRIBUTE_ADD = "SERVLET_CONTEXT_ATTRIBUTE_ADD"; //@ServletContext add attributes
	public static final String SERVLET_CONTEXT_ATTRIBUTE_REPLACE = "SERVLET_CONTEXT_ATTRIBUTE_REPLACE";//@ServletContext change attributes
	public static final String SERVLET_CONTEXT_ATTRIBUTE_REMOVE = "SERVLET_CONTEXT_ATTRIBUTE_REMOVE";//@ServletContext remove attributes
	public static final String REQUEST_ATTRIBUTE_ADD = "REQUEST_ATTRIBUTE_ADD"; //@HttpRequest attribute add
	public static final String REQUEST_ATTRIBUTE_REMOVE = "REQUEST_ATTRIBUTE_REMOVE"; //@HttpRequest attribute remove
	public static final String REQUEST_ATTRIBUTE_REPLACE = "REQUEST_ATTRIBUTE_REPLACE"; //@HttpRequest attribute replace
	public static final String REQUEST_CREATE = "REQUEST_CREATE"; // a @HttpRequest initialized
	public static final String REQUEST_DESTROY = "REQUEST_DESTROY"; // a @HttpRequest destroyed
	public static final String SESSION_VALUE_BOUND = "SESSION_VALUE_BOUND"; //session.put
	public static final String SESSION_VALUE_UNBOUND = "SESSION_VALUE_UNBOUND"; //session.remove
}
