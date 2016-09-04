package com.lwy.myserver.session;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionContext;

import com.lwy.myserver.container.Context;
import com.lwy.myserver.lifecycle.Lifecycle;
import com.lwy.myserver.listener.ListenerExecutor;
import com.lwy.myserver.listener.SimpleListenerExecutor;
import com.lwy.myserver.util.Constants;

/**
 * standard implementation of session
 * @author frank lee
 *
 */
@SuppressWarnings("deprecation")
public class StandardSessionImpl implements HttpSession,Lifecycle {
	
	private long creationTime; 
	private Context context;
	private String id;// sessionId
	private long lastAccessTime;
	private int interval = Constants.SESSION_INTERVAL;
	private Map<String,Object> attributes = new ConcurrentHashMap<>();
	private ListenerExecutor lisExecutor = new SimpleListenerExecutor.Builder().build(context);
	
	public StandardSessionImpl(String id, Context context){
		this.id = id;
		this.context = context;
		creationTime = System.currentTimeMillis();
		lastAccessTime = creationTime;
	}
	
	@Override
	public long getCreationTime() {
		// TODO Auto-generated method stub
		return creationTime;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return id;
	}
	
	public void setLastAccessedTime(long lastAccessTime){
		this.lastAccessTime = lastAccessTime;
	}

	@Override
	public long getLastAccessedTime() {
		// TODO Auto-generated method stub
		return lastAccessTime;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		this.interval = interval;
	}

	@Override
	public int getMaxInactiveInterval() {
		// TODO Auto-generated method stub
		return interval;
	}

	@Override
	public HttpSessionContext getSessionContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Object getValue(String name) {
		return getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		Set<String> set = attributes.keySet();
		String[] t = new String[set.size()];
		String[] s = set.toArray(t);
		return new Enumeration<String>(){
			private int count = 0;
			@Override
			public boolean hasMoreElements() {
				// TODO Auto-generated method stub
				return count<s.length;
			}
			@Override
			public String nextElement() {
				return s[count++];
			}
		};
	}

	@Override
	public String[] getValueNames() {
		Enumeration<String> e = getAttributeNames();
		String[] s = new String[attributes.size()];
		List<String> list = new ArrayList<>();
		while(e.hasMoreElements())
			list.add(e.nextElement());
		return list.toArray(s);
	}

	@Override
	public void setAttribute(String name, Object value) {
		executeListener(Constants.SESSION_VALUE_BOUND,new HttpSessionBindingEvent(this, name, value));
		attributes.put(name, value);
	}

	@Override
	public void putValue(String name, Object value) {
		setAttribute(name,value);
	}

	@Override
	public void removeAttribute(String name) {
		Object value = attributes.get(name);
		attributes.remove(name);
		executeListener(Constants.SESSION_VALUE_UNBOUND,new HttpSessionBindingEvent(this, name, value));
	}

	@Override
	public void removeValue(String name) {
		removeAttribute(name);
	}

	@Override
	public void invalidate() {
		stop();
		attributes.clear();
		id = null;
	}

	@Override
	public boolean isNew() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void start() {
		executeListener(Constants.SESSION_CREATION,this); //activate listener
	}

	@Override
	public void stop() {
		executeListener(Constants.SESSION_DESTROY,this); //activate session destroy in listener
	}

	@Override
	public void addListeners(Class<? extends EventListener> clazz, EventListener listener) {
		lisExecutor.addListener(clazz,listener);
	}

	@Override
	public void removeListener(Class<? extends EventListener> clazz, EventListener listener) {
		lisExecutor.removeListener(clazz, listener);
	}

	@Override
	public void executeListener(String status, Object data) {
		lisExecutor.executeListener(status, data);
	}

}
