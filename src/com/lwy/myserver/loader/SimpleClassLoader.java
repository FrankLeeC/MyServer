package com.lwy.myserver.loader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLStreamHandler;

import com.lwy.myserver.util.Constants;

/**
 * use synchronized block
 * hold an instance of @WebAppClassLoader
 * each servlet context contains one and only one instance of this class
 * instance of this class belongs to one and only one servlet context
 * @author frank lee
 *
 */
public class SimpleClassLoader implements Loader {

	private SimpleClassLoader(){
	}
	private static SimpleClassLoader instance = new SimpleClassLoader();
	public static SimpleClassLoader getInstance(){
		return instance;
	}
	
	/**
	 * synchronized here, and it's important
	 * @param name context name
	 */
	@Override
	public ClassLoader getLoader(String name) {
		synchronized(instance){
			try {
				ClassLoader commonLoader = Thread.currentThread().getContextClassLoader();
				URL[] urls = new URL[2];
				File appFile = new File(Constants.WEBROOT+File.separator+name);
				String appRepository = (new URL("file",null,appFile.getCanonicalPath()+File.separator)).toString();
				urls[0] = new URL(null,appRepository,(URLStreamHandler)null);
				File jspFile = new File(Constants.JSP_ROOT+File.separator+name);
				String jspRepository = (new URL("file",null,jspFile.getCanonicalPath()+File.separator)).toExternalForm();
				urls[1] = new URL(null,jspRepository,(URLStreamHandler)null);
				ClassLoader loader = new WebAppClassLoader(urls,commonLoader);
				return loader;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

}
