package com.lwy.myserver.connector;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import com.lwy.myserver.container.Container;
import com.lwy.myserver.lifecycle.Lifecycle;
import com.lwy.myserver.listener.InternalListenerExecutor;
import com.lwy.myserver.listener.ListenerExecutor;

public class HttpConnector implements Runnable,Lifecycle{
	
	private Container container; //@Host
	private ListenerExecutor listenerExecutor = InternalListenerExecutor.getInstance();
	private List<HttpProcessor> processors = new ArrayList<>(); //HttpProcessor pool
	private List<Boolean> status = new ArrayList<>(); //HttpProcessor status
	private int lastIndex = -1; //index of status(list) when fetch processor last time 上一次获取processor的位置
	private boolean RUN = true;
	private int port = 8080;
	private int minProcessors = 5; //min HttpProcessor
	private int maxProcessors = 20; //max HttpProcessor can be used
	private int currentProcessors = 0; //current HttpProcessors instantiation 当前实例个数
	private int processorsReady = 0; //current HttpProcessors which are ready to be used 当前可用数目
	private ServerSocket server;
	
	public void setContainer(Container container){
		this.container = container;
	}
	
	protected Container getContainer(){
		return container;
	}

	@Override
	public void run() {
		while(RUN){
			try {
				Socket socket = server.accept();
				HttpProcessor processor = getProcessor();
				boolean available = processor.activate(socket); //notify processor
				if(!available)
					setUnavailable(processor);
			} catch (IOException e) {
				continue;
			}
		}
	}
	
	/**
	 *six conditions in all, but in case of miss, there is a else block
	 * @return
	 */
	private HttpProcessor getProcessor(){
		if(maxProcessors<0&&processorsReady<=0){
			createHttpProcessor();
			return getProcessor();
		}
		if(maxProcessors<0&&processorsReady>0){
			return fetchProcessor();
		}
		if(maxProcessors>0&&currentProcessors<maxProcessors&&processorsReady>0){
			return fetchProcessor();
		}
		if(maxProcessors>0&&currentProcessors<maxProcessors&&processorsReady<=0){
			createHttpProcessor();
			return getProcessor();
		}
		if(maxProcessors>0&&currentProcessors>=maxProcessors&&processorsReady>0){
			return fetchProcessor();
		}
		if(maxProcessors>0&&currentProcessors>=maxProcessors&&processorsReady<=0){
			return null;
		}
		else{
			return null;
		}
	}
	
	/**int i=lastIndex+1, so the next processor have chance to be used, and processor at lastIndex
	 * can work lightly(wont't work fully), it is fair and high efficient, otherwise,when there
	 * is very few request, next processors may be redundant thread
	 * int i=lastIndex+1,所以后面的processor有机会被使用，并且当前lastIndex的processor工作轻松点，不会满载，
	 * 这样效率好点，否则，放请求非常少的时候，下一个processor可能变成用不上的空闲线程
	 * obtain a HttpProcessor from pool 
	 * if can't fetch one, return null, and ignore this request, but this will not happen 
	 * because only if processorReady>0 can getProcessor() use fetchProcessor().
	 * @return
	 */
	private  HttpProcessor fetchProcessor(){
		for(int i=lastIndex+1;i<currentProcessors;i++){
			if(status.get(i)){
				lastIndex = i;
				return processors.get(i);
			}
		}
		for(int i=0;i<=lastIndex;i++){
			if(status.get(i)){
				lastIndex = i;
				return processors.get(i);
			}
		}
		return null;
	}
	
	/**
	 * create HttpProcessorPool
	 */
	private void createHttpProcessorPool(){
		while(currentProcessors < minProcessors){
			createHttpProcessor();
		}
	}
	
	/**
	 * 
	 * @return a HttpProcessor
	 * if currentProcessor >= maxProcessors,it mean pool is fulled, then return null and ignore the request
	 */
	private void createHttpProcessor(){
		if(maxProcessors>0&&currentProcessors>=maxProcessors){
			return;
		}
		HttpProcessor processor = new HttpProcessor(currentProcessors,this);
		registerProcessor(processor); //register must happen before start, or it maybe wrong
		processor.start();
		currentProcessors++;
		processorsReady++;
	}
	
	/**
	 * register processor into pool, and set unavailable
	 * @param processor
	 */
	private void registerProcessor(HttpProcessor processor){
		processors.add(processor);
		status.add(currentProcessors, false);
	}
	
	/**
	 * initialization
	 */
	private void initializeServer(){
		try {
			server = new ServerSocket(port,1,InetAddress.getByName("127.0.0.1"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * recycle processor,set available
	 * called by @HttpProcess neither
	 * 1.when process has enough executor and processor can be used
	 * or
	 * 2.when create a process, we set it unavailable,after the processor execute start method,
	 * it is on duty,so it can be used
	 * 回收processor，设置可获取变量，通过HttpProcessor在以下两种情况下调用
	 * 1.processor有足够的executor并且processor可以被使用
	 * 2.当创建一个processor时，设置为不可用，然后，processor执行start方法后，它就在待命了，然后可以被使用
	 * @param processor
	 */
	protected void setAvailable(HttpProcessor processor){
		status.set(processor.getId(), true);
	}
	
	/**
	 * this processor is fully working and can't take any socket
	 * @param processor
	 */
	private void setUnavailable(HttpProcessor processor){
		processorsReady--;
		status.set(processor.getId(), false);
	}
	
	/**
	 * start the connector
	 */
	@Override
	public void start(){
		initializeServer();
		Thread thread = new Thread(this);
		thread.start();
		createHttpProcessorPool();
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
