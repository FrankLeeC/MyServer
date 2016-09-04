package com.lwy.myserver.listener;

import java.util.EventListener;

/**
 * all listener executors must implements this interface
 * @author frank lee
 *
 */
public interface ListenerExecutor {

	/**
	 * need to be synchronized
	 * @param clazz
	 * @param listener
	 */
	public void addListener(Class<? extends EventListener> clazz, EventListener listener);
	
	/**
	 * need to be synchronized
	 * @param clazz
	 * @param listener
	 */
	public void removeListener(Class<? extends EventListener> clazz, EventListener listener);
	
	public void executeListener(String status, Object data);
}
