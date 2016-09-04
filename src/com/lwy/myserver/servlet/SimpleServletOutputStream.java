package com.lwy.myserver.servlet;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class SimpleServletOutputStream extends ServletOutputStream {
	
	private OutputStream output;
	private int size;
	private byte[] buffer;
	private int count = 0;
	

	public SimpleServletOutputStream(OutputStream output,int size) {
		this.output = output;
		this.size = size;
		buffer = new byte[this.size];
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void setWriteListener(WriteListener writeListener) {
		// TODO Auto-generated method stub

	}

	/**
	 *comments below come from @OutputStream.write(int b ) method
	 * 
	 * Writes the specified byte to this output stream. The general
     * contract for <code>write</code> is that one byte is written
     * to the output stream. The byte to be written is the eight
     * low-order bits of the argument <code>b</code>. The 24
     * high-order bits of <code>b</code> are ignored.
     * <p>
     * Subclasses of <code>OutputStream</code> must provide an
     * implementation for this method.
	 */
	@Override
	public void write(int b) throws IOException {
		if(count>=buffer.length){
			flush();
		}
		buffer[count++] = (byte)b;
	}
	
	@Override
	public void flush() throws IOException {
		if(count > 0){ 
			output.write(buffer,0,count);
			count = 0;
		}
	}

}
