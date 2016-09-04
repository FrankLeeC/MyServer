package com.lwy.myserver.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.lwy.myserver.util.Constants;

/**
 * config
 * 	key:context name(web app name)
 * 	value: contextMap
 * 			key:Keys.SERVLET   value:servletMap
 * 			key:Keys.FILTER    value:filterMap
 * 			key:Keys.LISTENER  value:listenerMap
 * 
 * servletMap
 * 	key:name(programmer specify)
 * 	value:configMap
 * 			key:Keys.CLASS value:className
 * 			key:Keys.PATTERN value:url-pattern
 * 
 * listenerMap
 * 	key:name(programmer specify)
 * 	value:configMap
 * 		key:Keys.CLASS value:name
 * 
 * filterMap:
 * 	key:name(programmer specify)
 * 	value:configMap
 * 		key:Keys.CLASS value:className
 * 		key:Keys.PATTERN value:url-pattern
 * 	
 * @author frank lee
 *
 */
public class Configuration {
	private static String configPath = Constants.WEBROOT;
	private static List<String> apps = null;
	private static Map<String,Map<String,Map<String,Map<String,String>>>> config = new ConcurrentHashMap<>();
	private static boolean parsed = false;

//	public static void initTest(){
//		Map<String,Map<String,Map<String,String>>> contextMap = new HashMap<>();
//		Map<String,Map<String,String>> servletMap = new HashMap<>();
//		Map<String,Map<String,String>> listenerMap = new HashMap<>();
//		Map<String,Map<String,String>> filterMap = new HashMap<>();
//		contextMap.put(Keys.SERVLET,servletMap);
//		Map<String,String> context_start = new HashMap<>();
//		context_start.put(Keys.CLASS,"com.lwy.myserver.test.OnStartListener");
//		listenerMap.put("context_start",context_start);
//		Map<String,String> session_listener = new HashMap<>();
//		session_listener.put(Keys.CLASS,"com.lwy.myserver.test.SessionListener");
//		listenerMap.put("session_listener",session_listener);
//		contextMap.put(Keys.LISTENER,listenerMap);
//		config.put("first",contextMap);
//
//	}

	@SuppressWarnings("unchecked")
	public static void config(){
		if(parsed)
			return;
		SAXReader reader = new SAXReader();
		Document xml;
		List<String> apps = getApps();
		for(String s:apps){
			String path = configPath + File.separator + s + File.separator + "WEB-INF/web.xml";
			try {
				Map<String,Map<String,Map<String,String>>> contextMap = new HashMap<>();
				Map<String,Map<String,String>> servletMap = new HashMap<>();
				Map<String,Map<String,String>> listenerMap = new HashMap<>();
				Map<String,Map<String,String>> filterMap = new HashMap<>();
				xml = reader.read(path);
				Element root = xml.getRootElement();
				List<Element> elements = root.elements("servlet");
				for(Element e:elements){
					Map<String,String> configMap = new HashMap<>();
					configMap.put(Keys.ASYNC, "false"); //default is false
					String servletClass = e.elementText("class");
					String name = e.elementText("name");
					Element asyncElement = e.element("async");
					if(asyncElement != null)
						configMap.put(Keys.ASYNC, asyncElement.getText());
					configMap.put(Keys.CLASS, servletClass);
					servletMap.put(name, configMap);
				}
				elements = root.elements("servlet-mapping");
				for(Element e:elements)
					servletMap.get(e.elementText("name")).put(Keys.PATTERN, e.elementText("url-pattern"));
				contextMap.put(Keys.SERVLET, servletMap);
				
				elements = root.elements("listener");
				for(Element e:elements){
					Map<String,String> configMap = new HashMap<>();
					String name = e.elementText("name");
					String listenerClass = e.elementText("class");
					configMap.put(Keys.CLASS, listenerClass);
					listenerMap.put(name, configMap);
				}
				contextMap.put(Keys.LISTENER, listenerMap);
		
				elements = root.elements("filter");
				for(Element e:elements){
					Map<String,String> configMap = new HashMap<>();
					String name = e.elementText("name");
					String filterClass = e.elementText("class");
					configMap.put(Keys.CLASS, filterClass);
					filterMap.put(name, configMap);
				}
				elements = root.elements("filter-mapping");
				for(Element e:elements)
					filterMap.get(e.elementText("name")).put(Keys.PATTERN, e.elementText("url-pattern"));
				contextMap.put(Keys.FILTER, filterMap);
				config.put(s, contextMap);
			} catch (DocumentException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		parsed = true;
	} 
	
	public static List<String> getApps(){
		if(apps == null){
			apps = new ArrayList<>();
			File file = new File(configPath);
			File[] files = file.listFiles();
			for(File f:files){
				if(f.isDirectory())
					apps.add(f.getName());				
			}
		}
		return apps;
	}
	
	/**
	 * 
	 * @param name  context path
	 * @return
	 */
	protected static Map<String,Map<String,String>> getListeners(String name){
		Map<String,Map<String,Map<String,String>>> contextMap = config.get(name);
		if(contextMap != null){
			Map<String,Map<String,String>> listeners = contextMap.get(Keys.LISTENER);
			return listeners;
		}
		return null;
	}
	
	/**
	 * 
	 * @param name context name    it is request.getContextPath
	 * @return
	 */
	protected static Map<String,Map<String,String>> getFilters(String name){
		Map<String,Map<String,Map<String,String>>> contextMap = config.get(name);
		if(contextMap != null){
			Map<String,Map<String,String>> filters = contextMap.get(Keys.FILTER);
			return filters;
		}
		return null;
	}
	
	/**
	 * get name of servlet in web application configuration
	 * 获取web.xml中关于servlet的name参数
	 * @param contextName  context path
	 * @param pattern servlet pattern
	 * @return
	 */
	public static String getServletName(String contextName,String pattern){
		return getServletConfig(contextName,pattern,null);
	}

	/**
	 * get class name of servlet
     * 获取servlet的class name
	 * @param contextName context naem
	 * @param name name in web application configuration
	 * @return
	 */
	public static String getServletClassName(String contextName,String name){
		return getServletConfig(contextName,name,Keys.CLASS);
	}
	
	/**
	 * 
	 * @param contextName  context path
	 * @param name servlet name
	 * @return
	 */
	public static String getServletAsync(String contextName,String name){
		return getServletConfig(contextName,name,Keys.ASYNC);
	}
	
	/**
	 * 
	 * @param contextName  context path
	 * @param name servlet name
	 * @return
	 */
	public static String getServletPattern(String contextName,String name){
		return getServletConfig(contextName,name,Keys.PATTERN);
	}
	
	/**
	 * i
	 * @param contextName
	 * @param basis  name or pattern
	 * @param type   if type == null, then get servlet name(config in web.xml)  e.g.: <servlet-name>XXX<servlet-name>
     *               如果type == null，则返回配置给servlet的name属性
	 * @return
	 */
	private static String getServletConfig(String contextName,String basis,String type){
		String result = null;
		Map<String,Map<String,Map<String,String>>> contextMap = config.get(contextName);
		if(contextMap != null){
			Map<String,Map<String,String>> servlets = contextMap.get(Keys.SERVLET);
			if(servlets != null){
				if(type != null){
					Map<String,String> servletConfigs = servlets.get(basis);
					if(servletConfigs != null){
						result = servletConfigs.get(type);
					}
				}
				else{
					Set<String> sets = servlets.keySet();
					for(String s:sets){
						Map<String,String> servletConfigs = servlets.get(s);
						if(basis.equals(servletConfigs.get(Keys.PATTERN))){
							result = s;
						}
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @param contextName context path
	 * @param pattern
	 * @return
	 */
	public static List<String> getFilterNamesByPattern(String contextName,String pattern){
		Map<String,Map<String,Map<String,String>>> contextMap = config.get(contextName);
		List<String> list = null;
		if(contextMap != null){
			Map<String,Map<String,String>> filters = contextMap.get(Keys.FILTER);
			if(filters != null){
				list = new ArrayList<>();
				Set<String> sets = filters.keySet();
				for(String s:sets){
					Map<String,String> filterConfigs = filters.get(s);
					if(pattern.equals(filterConfigs.get(Keys.PATTERN)))
						list.add(s);
				}
			}
		}
		return list;
	}
}
