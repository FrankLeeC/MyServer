package com.lwy.myserver.loader;


/**
 * 
 * @author frank lee
 *
 */
public interface Loader {

	/**
	 * 
	 * @param name context path
	 * @return
	 */
	public ClassLoader getLoader(String name);
}
