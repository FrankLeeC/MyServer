package com.lwy.myserver.container;

import com.lwy.myserver.session.Manager;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;

/**
 * host hosts lots of @Context
 * @author frank lee
 *
 */
public interface Host extends Container {

	void addContext(Context context);
	Context getContext(String name);
	void setManager(Manager manager);
}
