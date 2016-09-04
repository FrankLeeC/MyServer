package com.lwy.myserver.session;

import java.util.EventListener;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import com.lwy.myserver.container.Context;
import com.lwy.myserver.lifecycle.Lifecycle;
import com.lwy.myserver.listener.InternalListenerExecutor;
import com.lwy.myserver.listener.ListenerExecutor;

/**
 * session contains context name, so sessions in different context will not be same 
 * so, when creating session, we must pass context as an arguement,
 * when obtaining or removing a session, we don't need to pass context
 * @author frank lee
 *
 */
public class SessionManager implements Manager,Lifecycle,Runnable{

	private Map<String,HttpSession> sessions; //ConcurrentHashMap
	private SessionIdGenerator idGenerator;
	private ListenerExecutor listenerExecutor = InternalListenerExecutor.getInstance();
	private boolean RUN = true;//check-thread flag
	@Override
	public HttpSession createSession(Context context) {
		String sessionId = idGenerator.generate(context.getName());
		HttpSession session = new StandardSessionImpl(sessionId,context);
		((Lifecycle)session).start();
		sessions.put(sessionId, session);
		return session;
	}

	/**
	 * session.setLashAccessedTime is not thread-safe,for example,one thread access session when interval equals 2*60*1000
	 * after this thread step into setLastAccessedTime and before it goes out, checking-thread will remove this session,
	 * but this method will still return this session,but next time,this session is not in map
	 * if this method accesses first,checking-thread can't run
	 * if checking-thread accessed first,this method can't run
	 * so add synchronized(sessions)
	 *
	 * session.setLastAccessedTime不是线程安全的。例如：当时间间隔为2*60*1000时一个线程获取到session，当线程进入
	 * setLastAccessedTime方法并且出来之前，check-thread 将会移除这个session，但是这个方法将会返回这个session，
	 * 但是这个session不再map中了
	 * 如果这个方法先获取到session，check-thread就不能运行
	 * 如果check-thread先获取session，那么这个方法就不能运行
	 * 所以加上synchronized(sessions)
	 */
	@Override
	public HttpSession getSession(String sessionId) {
		if(sessionId == null) //if sessionId = null. sessions.get(sessionId) will cause NullPointerException
			return null;
		StandardSessionImpl sessionImpl = null;
		synchronized(sessions){
			sessionImpl = (StandardSessionImpl) sessions.get(sessionId);
			if(sessionImpl == null)
				return null;
			sessionImpl.setLastAccessedTime(System.currentTimeMillis()); //modify lastAccessedTime only when requesting with it
		}
		HttpSession session = new HttpSessionFacade(sessionImpl);
		return session;
	}

	@Override
	public void removeSession(HttpSession session) {
		String sessionId = session.getId();
		if(sessions.containsKey(sessionId)){
			session.invalidate();
			sessions.remove(sessionId);
		}
	}
	
	private void threadStart(){
		Thread thread = new Thread(this);
		thread.start();
	}

	@Override
	public void start() {
		sessions = new ConcurrentHashMap<>();
		idGenerator = new SessionIdGenerator();
		idGenerator.start();
		threadStart();
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListeners(Class<? extends EventListener> clazz, EventListener listener) {
		listenerExecutor.addListener(clazz,listener);
	}

	@Override
	public void removeListener(Class<? extends EventListener> clazz, EventListener listener) {
		listenerExecutor.removeListener(clazz, listener);
	}

	@Override
	public void executeListener(String status, Object data) {
		listenerExecutor.executeListener(status, data);
	}

	/**
	 * this is not thread-safe,for example,one thread access session when interval equals 2*60*1000
	 * after this thread step into setLastAccessedTime and before it goes out, checking-thread will remove this session,
	 * but getSession() will still return this session,but next time,this session is not in map
	 * if this method accesses first,getSession() can't run
	 * if getSession() accessed first,this method can't run
	 * so add synchronized(sessions)
	 *
	 * session.setLastAccessedTime不是线程安全的。例如：当时间间隔为2*60*1000时一个线程获取到session，当线程进入
	 * setLastAccessedTime方法并且出来之前，check-thread 将会移除这个session，但是这个方法将会返回这个session，
	 * 但是这个session不再map中了
	 * 如果这个方法先获取到session，check-thread就不能运行
	 * 如果check-thread先获取session，那么这个方法就不能运行
	 * 所以加上synchronized(sessions)
	 */
	@Override
	public void run() {
		while(RUN){
			synchronized(sessions){
				Set<String> set = sessions.keySet();
				for(String s:set){
					HttpSession session = sessions.get(s);
					long current = System.currentTimeMillis();
					long interval = current - session.getLastAccessedTime();
					if(interval >= 2*60*1000){
						removeSession(session);
						session.invalidate();
					}
				}
			}
			
		}
	}

}
