package com.lwy.myserver.listener;

import java.util.EventObject;

/**
 * check @EventObject instanceof @LifecycleEvent, then cast to @LifecycleEvent
 * @author frank lee
 *
 */
public interface LifecycleListener extends InternalListener{

	public void beforeStartEvent(EventObject data);
	
	public void processStartEvent(EventObject data);
	
	public void afterStartEvent(EventObject data);
	
	public void beforeStopEvent(EventObject data);
	
	public void processStopEvent(EventObject data);
	
	public void afterStopEvent(EventObject data);
}
 