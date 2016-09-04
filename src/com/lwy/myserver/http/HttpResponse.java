package com.lwy.myserver.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.lwy.myserver.container.Container;
import com.lwy.myserver.servlet.SimpleServletOutputStream;

public class HttpResponse implements HttpServletResponse {

	private Container container;
	private OutputStream output;
	private PrintWriter writer;
	private int bufferSize;
	private ServletOutputStream out = null;
	private boolean useWriter = false;
	private boolean useOutput = false;
	private boolean committed = false;
	private HttpResponseHeader header;
	private Cookie[] cookies = new Cookie[0];

	public Container getContainer() {
		return container;
	}

	public HttpResponse(OutputStream output, Container container) {
		this.output = output;
		this.container = container;
	}

	@Override
	public void flushBuffer() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getBufferSize() {
		// TODO Auto-generated method stub
		return bufferSize;
	}

	@Override
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @getOutputStream and @getWriter() can't all be used
	 */
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if(!useWriter){	
			if(out == null)
				out = new SimpleServletOutputStream(output,bufferSize);
			useOutput = true;
			return out;
		}
		return null;
	}

	/**
	 * @getOutputStream and @getWriter() can't all be used
	 */
	@Override
	public PrintWriter getWriter() throws IOException {
		if(!useOutput){
			if(writer == null)
				writer = new PrintWriter(output, true);
			useWriter = true;
			return writer;
		}
		return null;
	}

	@Override
	public boolean isCommitted() {
		return committed;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resetBuffer() {
		if(isCommitted()){
			throw new IllegalStateException();
		}
	}

	@Override
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	@Override
	public void setCharacterEncoding(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setContentLength(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setContentType(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLocale(Locale arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addCookie(Cookie cookie) {
		Cookie[] newCookies = new Cookie[cookies.length+1];
		for(int i=0;i<cookies.length;i++)
			newCookies[i] = cookies[i];
		newCookies[cookies.length] = cookie;
		cookies = newCookies;
	}

	@Override
	public void addDateHeader(String name, long time) {
		header.addDateHeader(name, time);
	}

	/**
     * Adds a response header with the given name and value.
     * This method allows response headers to have multiple values.
     * 
     * @param	name	the name of the header
     * @param	value	the additional header value   If it contains
     *		octet string, it should be encoded
     *		according to RFC 2047
     *		(http://www.ietf.org/rfc/rfc2047.txt)
     *
     * @see #setHeader
     */
	@Override
	public void addHeader(String name, String value) {
		header.addHeader(name, value);
	}

	@Override
	public void addIntHeader(String name, int value) {
		header.addIntHeader(name, value);
	}

	@Override
	public boolean containsHeader(String name) {
		return header.containsHeader(name);
	}

	@Override
	public String encodeRedirectURL(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeRedirectUrl(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeURL(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeUrl(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendError(int arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendError(int arg0, String arg1) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendRedirect(String arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDateHeader(String name, long time) {
		header.setDateHeader(name, time);
	}

	/**
    *
    * Sets a response header with the given name and value.
    * If the header had already been set, the new value overwrites the
    * previous one.  The <code>containsHeader</code> method can be
    * used to test for the presence of a header before setting its
    * value.
    * 
    * @param	name	the name of the header
    * @param	value	the header value  If it contains octet string,
    *		it should be encoded according to RFC 2047
    *		(http://www.ietf.org/rfc/rfc2047.txt)
    *
    * @see #containsHeader
    * @see #addHeader
    */
	@Override
	public void setHeader(String name, String value) {
		header.setHeader(name, value);
	}

	@Override
	public void setIntHeader(String name, int value) {
		header.setIntHeader(name, value);
	}

	@Override
	public void setStatus(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStatus(int arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getHeader(String name) {
		return header.getHeader(name);
	}

	@Override
	public Collection<String> getHeaderNames() {
		return header.getHeaderNames();
	}

	@Override
	public Collection<String> getHeaders(String name) {
		return header.getHeaders(name);
	}

	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setContentLengthLong(long arg0) {
		// TODO Auto-generated method stub

	}

}
