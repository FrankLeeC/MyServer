package com.lwy.myserver.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

public class SimpleServletInputStream extends ServletInputStream {
	
	/**
	 * this input is a @ByteArrayInputStream which only contain message(post not parsed),
	 * or query string. message/query string have been decoded
	 */
	private InputStream input; 
	
	public SimpleServletInputStream(InputStream input){
		this.input = input;
	}

	@Override
	public boolean isFinished() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setReadListener(ReadListener readListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public int read() throws IOException {
		return input.read();
	}

}
