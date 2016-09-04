package com.lwy.myserver.loader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bootstrap--->ext loader--->common class loader(my myserver's jar,including javaee-api)--->web app class loader
 * @author frank lee
 *
 */
public class WebAppClassLoader extends ClassLoader {

	private ClassLoader parent; //  /*ext loader*/   common loader
	private Map<String,Class<?>> classes = new ConcurrentHashMap<>();
	private URL[] urls;
	/**
	 * urls' order is important
	 * Bootstrap classes of your JVM
	 * /WEB-INF/classes of your web application
	 * /WEB-INF/lib/*.jar of your web application
	 * System class loader classes
	 * Common class loader classes
	 * @param urls
	 * @param parent
	 */
	public WebAppClassLoader(URL[] urls, ClassLoader parent) {
		this.urls = urls;
		this.parent = parent;
	}


	/**
	 * if this class is in javaee-api(JavaEEClassSets.containsClass(name) returns true)
	 * then, use common class loader to load
	 *
	 * search resource as i defined,which is also recommended by javaee specification
	 * Bootstrap classes of your JVM
	 * /WEB-INF/classes of your web application
	 * /WEB-INF/lib/*.jar of your web application
	 * Common class loader classes
	 */
	@Override
	protected Class<?> loadClass(String name, boolean resolve) {
		if("".equals(name) ||  name == null)
			return null;
		Class<?> clazz = classes.get(name);
		if(clazz == null)
			try {
				if(JavaEEClassSets.containsClass(name)){
					return parent.loadClass(name);
				}
				clazz = parent.getParent().getParent().loadClass(name);//use ext class loader
			} catch (ClassNotFoundException e) {
				clazz = findClass(name);         //use my class loader to findClass
			}
		if(clazz == null){
			try {
				clazz = parent.loadClass(name); //this class is this server's class
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return clazz;
	}
	
	@Override
	protected Class<?> findClass(String name) {
		Class<?> clazz = null;
		try {
			clazz = readClass(name);
		} catch (ClassNotFoundException e) {
			System.err.println("can not find class:"+name);
			e.printStackTrace();
		}
		if(clazz != null){
			classes.put(name,clazz);
			return clazz;
		}
		return null;
	}

	/**
	 * findClass must throws @ClassNotFoundException, but it won't,
	 * so add this method and throws @ClassNotFoundException,
	 * then, findClass will throws @ClassNotFoundException.
	 * @param name class name
	 * @return clazz
	 * @throws ClassNotFoundException
     */
	private Class<?> readClass(String name) throws ClassNotFoundException{
		Class<?> clazz = null;
		FileInputStream in = null;
		ByteArrayOutputStream out = null;
		byte[] classBytes;
		byte[] read = new byte[1024];
		try {
			for (URL url : urls) {
				String root = url.getPath();
				String path = root + name.replace(".", "/").concat(".class");
				File file = new File(path);
				if (file.exists()) {
					in = new FileInputStream(file);
					out = new ByteArrayOutputStream();
					int len;
					while ((len = in.read(read)) > 0) {
						out.write(read, 0, len);
					}
					break;
				}
			}
			if(out != null) {
				classBytes = out.toByteArray();
				out.close();
				in.close();
				clazz = defineClass(name,classBytes,0,classBytes.length);
			}
		} catch (IOException e) {
			System.err.println("fail to load class:"+name);
			e.printStackTrace();
		}
		return clazz;
	}

}
