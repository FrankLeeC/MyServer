package com.lwy.myserver.lifecycle;

import java.util.EventListener;

/**
 *manage lifecycle of components
 */
public interface Lifecycle {

	public void start();
	
	public void stop();
	
	public void addListeners(Class<? extends EventListener> clazz, EventListener listener);
	
	public void removeListener(Class<? extends EventListener> clazz, EventListener listener);
	
	public void executeListener(String status, Object data);
}
 