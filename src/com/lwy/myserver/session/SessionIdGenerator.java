package com.lwy.myserver.session;

import java.util.EventListener;
import java.util.Random;
import java.util.Vector;

import com.lwy.myserver.lifecycle.Lifecycle;
import com.lwy.myserver.listener.SimpleListenerExecutor;
import com.lwy.myserver.util.MD5;

/**
 * generate session id for each client
 * even different web apps in this server will not get same id,because server only contains one instance of this class
 * @author frank lee
 *
 */
public class SessionIdGenerator implements Lifecycle{
	
	private int seed = 47;
	private Random random;
	private SimpleListenerExecutor listenerExecutor = null;
	private Vector<String> sids = new Vector<>(); //thread-safe when manipulating contains() method and add() method
	
	protected String generate(String contextName){
		String sessionId = "";
		int length = contextName.length();
		byte[] nameBytes = new byte[length];
		for(int i=0;i<length;i++)
			nameBytes[i] = (byte) contextName.charAt(i);
		String name = contextName.toString();
		do{
			byte[] bytes = new byte[16];
			synchronized(this){
				random.nextBytes(bytes);
			}
			long current = System.currentTimeMillis();
			String time = String.valueOf(current);
			StringBuilder sb = new StringBuilder();
			sb.append(time);
			for(int i=0;i<16;i++){
				byte b = bytes[i];
				int k = random.nextInt(time.length());
				sb.insert(k, b);
			}
			String raw = MD5.getMD5(sb.toString().getBytes());
			int len = raw.length();
			int s = random.nextInt(len/2);
			sessionId = raw.substring(s,len/2);
			sessionId = name + sessionId;
		}while(sids.contains(sessionId)); // if contains, then generate a new one, make sure it's unique
		sids.add(sessionId);
		return sessionId;
	}
	
	@Override
	public void start() {
		random = new Random(seed);
	}

	@Override
	public void stop() {
		
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
	public void executeListener(String status,Object data) {
		listenerExecutor.executeListener(status, data);
	}

}
