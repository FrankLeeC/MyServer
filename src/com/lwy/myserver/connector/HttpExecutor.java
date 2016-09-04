package com.lwy.myserver.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.EventListener;

import com.lwy.myserver.config.Configuration;
import com.lwy.myserver.container.Container;
import com.lwy.myserver.container.Context;
import com.lwy.myserver.container.Host;
import com.lwy.myserver.container.SimpleContext;
import com.lwy.myserver.http.HttpRequest;
import com.lwy.myserver.http.HttpResponse;
import com.lwy.myserver.lifecycle.Lifecycle;
import com.lwy.myserver.listener.SimpleListenerExecutor;
import com.lwy.myserver.util.Constants;

public class HttpExecutor implements Runnable, Lifecycle {

	private HttpProcessor processor;
	private SimpleListenerExecutor listenerExecutor = null;
	private Socket socket;
	private boolean free = true;
	private boolean RUN = true;
	private boolean async = false; // servlet support async?
	private boolean wait = true;
	private int id;

	HttpExecutor(int id, HttpProcessor processor) {
		this.id = id;
		this.processor = processor;
	}

	protected int getId() {
		return id;
	}

	@Override
	public void run() {
		while (RUN) {
			processor.setAvailable(this);
			await();
			free = true;
			execute();
			if(async){
				waitForAsync();
			}
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void execute() {
		try {
			System.out.println("current thread id="
					+ Thread.currentThread().getId() + "   executor id="
					+ getId() + "   processor id=" + processor.getId());
			InputStream input = socket.getInputStream();
			OutputStream output = socket.getOutputStream();
			HttpRequest request = new HttpRequest(input);
			request.start();// it's important
			request.parse();
			Host host = getContainer();
			Container container = host.getContext(request.getContextPath());
			((SimpleContext) container).setExecutor(this);
			request.setContainer(container); //SimpleContext
			/**
			 * add some code here, judge whether the requested servlet support async, 
			 * if true, set async true, otherwise false 
			 */
			String uri = request.getRequestURI();
			String servletName = Configuration.getServletName(((Context) container).getName(),uri);
			String isAsync = Configuration.getServletAsync(((Context) container).getName(),servletName);
			if("true".equals(isAsync))
				async = true;
			if(request.getRequestURI().contains(".do")||request.getRequestURI().endsWith(".jsp")){//request to a servlet or jsp
				HttpResponse response = new HttpResponse(output, container);
				host.invoke(request, response);
			}
			else { //request to a static resource(except jsp)
				String path = request.getRequestURI();
				File file = new File(Constants.WEBROOT);
				FileInputStream in = new FileInputStream(file.getPath() + path);
				byte[] bytes = new byte[1024];
				int len = in.read(bytes);
				in.close();
				OutputStream out = socket.getOutputStream();
				out.write(bytes, 0, len);
				out.flush();
				out.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Container getContainer(String name) {
		return ((Host)processor.getConnector().getContainer()).getContext(name);
	}

	private Host getContainer(){
		return (Host) processor.getConnector().getContainer();
	}

	private synchronized void await() {
		while (free) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private synchronized void waitForAsync(){
		while(wait){
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void activateAsync(){
		wait = false;
		notifyAll();
	}

	protected synchronized void activate(Socket socket) {
		free = false;
		notifyAll();
		this.socket = socket;
	}

	@Override
	public void start() {
		Thread thread = new Thread(this);
		thread.start();
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		RUN = false; // stop task
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

}
