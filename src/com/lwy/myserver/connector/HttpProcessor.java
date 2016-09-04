package com.lwy.myserver.connector;

import java.net.Socket;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import com.lwy.myserver.lifecycle.Lifecycle;
import com.lwy.myserver.listener.SimpleListenerExecutor;

public class HttpProcessor implements Runnable,Lifecycle{
	
	private int id;
	private HttpConnector connector;
	private SimpleListenerExecutor listenerExecutor = null;
	private boolean free = true;
	private boolean RUN = true;
	private int lastIndex = -1; // index of executors(list), which is get last time
	private Socket socket;
	private List<HttpExecutor> executors = new ArrayList<>();
	private List<Boolean> status = new ArrayList<>();
	private int minExecutors = 5;
	private int maxExecutors = 20;
	private int currentExecutors = 0; //current executor instantiation        executor实例数目
	private int executorsReady = 0; //executors which are ready to be used         executor可用数目
	
	public HttpProcessor(int id,HttpConnector connector){
		this.id = id;
		this.connector = connector;
	}
	
	protected HttpConnector getConnector(){
		return connector;
	}

	@Override
	public void run() {
		while(RUN){
			Socket socket = await();
			HttpExecutor executor = getExecutor();
			executor.activate(socket);
			setUnavailable(executor);
			free = true;
		}
	}
	
	/**
	 * wait for task
	 * use a local variable socket,because after pass socket,processor will wait for another socket immediately
	 * 等待任务，使用局部变量socket来赋值，是因为在传值之后，立马能去接受新的socket，而不会影响之前的socket
	 */
	private synchronized Socket await(){
		while(free){
			try {
				if(currentExecutors<maxExecutors||executorsReady>0)
					connector.setAvailable(this);
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Socket socket = this.socket; 
		return socket;
	}
	
	/**
	 * change and check whether all executors are working
	 * if all working(maxExecutors) return false.return true otherwise
	 * 检查是否所有的executor都在工作，如果是返回false，否则true
	 */
	private boolean changeAndCheck(){
		executorsReady--;
		if(currentExecutors>=maxExecutors&&executorsReady==0){ //fully working, change to inavailable status
			return false;
		}
		return true;
	}
	
	protected int getId(){
		return id;
	}
	
	private HttpExecutor getExecutor(){
		if(maxExecutors<0&&executorsReady>0)
			return fetchExecutor();
		if(maxExecutors<0&&executorsReady<=0){
			createHttpExecutor();
			return getExecutor();
		}
		if(maxExecutors>0&&currentExecutors<maxExecutors&&executorsReady>0)
			return fetchExecutor();
		if(maxExecutors>0&&currentExecutors<maxExecutors&&executorsReady<=0){
			createHttpExecutor();
			return getExecutor();
		}
		if(maxExecutors>0&&currentExecutors>=maxExecutors&&executorsReady>0)
			return fetchExecutor();
		if(maxExecutors>0&&currentExecutors>=maxExecutors&&executorsReady<=0)
			return null;
		return null;
	}
	
	/**
	 * int i=lastIndex+1, so the next executor have chance to be used, and executor at lastIndex
	 * can work lightly(wont't work fully), it is fair and high efficient
	 * @return
	 */
	private HttpExecutor fetchExecutor(){
		for(int i=lastIndex+1;i<status.size();i++){
			if(status.get(i)){
				executorsReady--;
				lastIndex = i;
				return executors.get(i);
			}
		}
		for(int i=0;i<=lastIndex;i++){
			if(status.get(i)){
				executorsReady--;
				lastIndex = i;
				return executors.get(i);
			}
		}
		return null;
	}
	
	/**
	 * set this executor unavailable, this method is used by this processor
	 * @param executor
	 */
	private void setUnavailable(HttpExecutor executor){
		status.set(executor.getId(), false);
	}
	
	/**
	 * set this executor available, this method is used by executor
	 * @param executor
	 */
	protected void setAvailable(HttpExecutor executor){
		status.set(executor.getId(), true);
	}
	
	/**
	 * create executor pool
	 */
	private void createHttpExecutorPool(){
		while(currentExecutors<minExecutors){
			createHttpExecutor();
		}
	}
	
	private void createHttpExecutor(){
		if(maxExecutors>0&&currentExecutors>=maxExecutors){
			return;
		}
		HttpExecutor executor = new HttpExecutor(currentExecutors,this);
		registerExecutor(executor); //register must happen before start, or it maybe wrong
		executor.start();
		currentExecutors++;
		executorsReady++;
	}
	
	/**
	 * register executors and set inavailable
	 * @param executor
	 */
	private void registerExecutor(HttpExecutor executor){
		executors.add(executor);
		status.add(false);
	}
	
	/**
	 * recycle executor
	 * @param executor
	 */
	protected void recycle(HttpExecutor executor){
		status.set(executor.getId(), true);
		signalProcessor();
	}
	
	/**
	 * this processor have available executor
	 * when currentExecutors>=maxExecutors, if executorReady form 0 to 1, then signal this processor
	 */
	private void signalProcessor(){
		executorsReady++;
		if(currentExecutors>=maxExecutors&&executorsReady == 1)
			connector.setAvailable(this);
	}
	
	/**
	 * connector signal this processor and check whether it's fully working
	 * return false if fully working, return true otherwise
	 * @param socket
	 * @return
	 */
	protected synchronized boolean activate(Socket socket){
		this.socket = socket;
		free = false;
		notifyAll();
		return changeAndCheck();
	}
	
	/**
	 * start to run and create executor pool
	 */
	@Override
	public void start(){
		Thread thread = new Thread(this);
		thread.start();
		createHttpExecutorPool();
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		RUN = false;
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
