package com.lwy.myserver.listener;

import java.util.EventListener;

/**
 * all listeners (except javaee listeners) must implements it or extends it
 * e.g.: @LifecycleListener
 * @author frank lee
 *
 */
public interface InternalListener extends EventListener {

}
