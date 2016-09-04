package com.lwy.myserver.listener;

import java.util.EventListener;

/**
 * myserver's internal listeners executor.
 * internal listeners are  e.g.: @LifecycleListener
 * @author frank lee
 *
 */
public class InternalListenerExecutor implements ListenerExecutor {

	private static InternalListenerExecutor instance = null;
	private static final Object lock = new Object();

	private InternalListenerExecutor(){}

	public static InternalListenerExecutor getInstance(){
		if(instance == null) {
			synchronized (lock) {
				if (instance == null) {
					instance = new InternalListenerExecutor();
				}
			}
		}
		return instance;
	}

	@Override
	public void addListener(Class<? extends EventListener> clazz,
			EventListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeListener(Class<? extends EventListener> clazz,
			EventListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executeListener(String status, Object data) {
		// TODO Auto-generated method stub
		
	}
}
