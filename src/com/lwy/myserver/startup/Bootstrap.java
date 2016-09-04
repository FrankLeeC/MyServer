package com.lwy.myserver.startup;

import com.lwy.myserver.loader.CommonClassLoader;
import com.lwy.myserver.util.Constants;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;

public class Bootstrap {

	public static void main(String[] args) {

//		Configuration.config();
//		Configuration.initTest();

		Bootstrap boot = new Bootstrap();
		ClassLoader commonLoader = boot.createLoader();
		Thread.currentThread().setContextClassLoader(commonLoader);

		try {
			Class<?> server = commonLoader.loadClass("com.lwy.myserver.server.SimpleServer");
			Method start = server.getDeclaredMethod("start");
			start.invoke(server.newInstance());
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
			e.printStackTrace();
		}
//		HttpConnector connector = new HttpConnector();
//		Container container = new SimpleHost();
//		Manager manager = new SessionManager();
//		((Host)container).setManager(manager);// set session manager
//		connector.setContainer(container);
//		((Lifecycle)connector).start();
//		((Lifecycle)container).start();
	}

	private ClassLoader createLoader(){
		ClassLoader commonLoader = null;
		try {
			ClassLoader appLoader = Bootstrap.class.getClassLoader();
			URL[] urls = new URL[2];
			String repository = (new URL("file",null,Constants.SERVER_JAVAEE)).toString();
			String repository2 = (new URL("file",null,Constants.INTERNALJAR + "\\dom4j-1.6.1.jar")).toString();
//			String repository3 = (new URL("file",null,Constants.SYSTEM + "\\out\\production\\MyServer")).toString();
			urls[0] = new URL(null,repository,(URLStreamHandler)null);
			urls[1] = new URL(null,repository2,(URLStreamHandler)null);
//			urls[2] = new URL(null,repository3,(URLStreamHandler)null);
			commonLoader = new CommonClassLoader(appLoader,urls);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return commonLoader;
	}

}
