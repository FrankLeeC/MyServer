package com.lwy.myserver.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.ServletException;

import com.lwy.myserver.container.Context;
import com.lwy.myserver.filter.SimpleFilterWrapper;

/**
 * singleton
 * all filters are singleton
 * 所有的filter都是单例
 * @author frank lee
 *
 */
public class FilterFactory {

	/**
	 * key:String    context path
	 */
	private static Map<String,List<Filter>> filters = new ConcurrentHashMap<>();

	private static FilterFactory instance = null;
	private static Object lock = new Object();
	
	private FilterFactory(){}
	
	public static FilterFactory getInstance(){
		if(instance == null){
			synchronized(lock){
				if(instance == null){
					instance = new FilterFactory();
				}
			}
		}
	    return instance;
	}
	
	/**
	 * fire all filter in this context(each app)
	 * 启动所有的这个context的filter
	 * @param context
	 */
	public void fireFilterInstance(Context context){
		String name = context.getName();
		List<Filter> filterList = filters.get(name);
		if(filterList == null){
			ClassLoader classLoader = context.getClassLoader();
			Map<String,Map<String,String>> filterMap = Configuration.getFilters(name);
			filterList = new ArrayList<>();
			Set<String> sets = filterMap.keySet();
			for(String s:sets){
				//this check is important,web.xml may contain redundant empty configuration
				if(s != null && !"".equals(s)){
					System.out.println(s);
					try {
						Filter filter = new SimpleFilterWrapper((Filter) classLoader.loadClass(filterMap.get(s)
										.get(Keys.CLASS)).newInstance(),context);
						filter.init(null);
						filterList.add(filter);
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (ServletException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			filters.put(name, filterList);
		}
	}
	
	/**
	 * 
	 * @param names config in web.xml or class name配置在web.xml中的名字，没有的话就是类名
	 * @return
	 */
	public static List<Filter> getFiltersByNames(List<String> names,Context context){
		List<Filter> filterList = filters.get(context.getName());
		if(filterList != null){
			List<Filter> list = new ArrayList<>();
			for(String s:names){
				for(Filter f:filterList){
					if(s.equals(((SimpleFilterWrapper) f).getFilterName()))
						list.add(f);
				}
			}
			return list;
		}
		return null;
	}

}
