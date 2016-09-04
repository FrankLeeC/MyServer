package com.lwy.myserver.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import com.lwy.myserver.container.Container;
import com.lwy.myserver.container.Context;
import com.lwy.myserver.lifecycle.Lifecycle;
import com.lwy.myserver.listener.ListenerExecutor;
import com.lwy.myserver.listener.SimpleListenerExecutor;
import com.lwy.myserver.servlet.SimpleServletInputStream;
import com.lwy.myserver.util.Constants;

public class HttpRequest implements HttpServletRequest,Lifecycle {

	private Container container; //@SimpleContext
	private ListenerExecutor lisExecutor = new SimpleListenerExecutor.Builder().build((Context) container);
	private HttpRequestLine requestLine;
	private HttpRequestHeader header;
	private Cookie[] cookies = new Cookie[0];
	private HttpSession session;
	private SocketInputStream socketInput;
	private String charset; // charset
	private boolean charSet = false; // have already setCharset()?
	private String DEFAULT_CHARSET = "ISO-8859-1";
	private boolean useInput = false;
	private boolean useReader = false;
	private ByteArrayInputStream input = null;
	private SimpleServletInputStream ssis = null;
	private BufferedReader br = null;
	private StringReader reader = null;
	private String message = null; //post message(source,not parsed)
	private Map<String, String[]> parameters = new HashMap<>(); // save
																// parameters
																// and values
	private Map<String, Object> attributes = new HashMap<>(); // attributes in
																// request scope

	public Container getContainer() {
		return container;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setRequestLine(HttpRequestLine requestLine) {
		this.requestLine = requestLine;
	}

	public void setHttpHeader(HttpRequestHeader header) {
		this.header = header;
	}

	// public void setMessage(String message){
	// this.message = message;
	// }

	public void setCookie(List<Cookie> cookieList) {
		cookieList.toArray(cookies);
	}

	public void setParameters(Map<String, String[]> parameters) {
		this.parameters = parameters;
	}

	public HttpRequest(InputStream input) {
		socketInput = new SocketInputStream(input, this, 2048);
	}
	
	public void setContainer(Container container){
		this.container = container;
	}

	public void parse() {
		socketInput.prepare();
		socketInput.readReqeust();
	}

	@Override
	public Object getAttribute(String arg0) {
		return attributes.get(arg0);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		Set<String> set = attributes.keySet();
		String[] t = new String[set.size()];
		String[] s = set.toArray(t);
		return new Enumeration<String>() {
			private int count = 0;

			@Override
			public boolean hasMoreElements() {
				return count < s.length;
			}

			@Override
			public String nextElement() {
				return s[count++];
			}
		};
	}

	@Override
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return charset;
	}

	@Override
	public int getContentLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	@Override
	public ServletInputStream getInputStream() throws IOException {
		if(useReader)
			return null;
		useInput = true;
		if(!charSet)
			setCharacter();
		if(input == null){ //maybe both getQueryString != null and message != null are true,so combine them
			String allValue = "";
			if(message != null)
				allValue += message;
			allValue += "&";
			if(getQueryString() != null)
				allValue += getQueryString();
			input = new ByteArrayInputStream(allValue.getBytes(charset));
			ssis = new SimpleServletInputStream(input);
		}
		return ssis;
	}

	@Override
	public String getLocalAddr() {
		return null;
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLocalPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<Locale> getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setParameters(String name, String[] values) {
		parameters.put(name, values);
	}

	@Override
	public String getParameter(String arg0) {
		// TODO Auto-generated method stub
		if (!charSet)
			setCharacter();
		if (!Constants.SSID.equals(arg0))
			return parameters.get(arg0)[0];
		if (parameters.get(Constants.SSID) != null)
			return parameters.get(Constants.SSID)[0];
		if (cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (Constants.SSID.equals(cookie.getName()))
					return cookie.getValue();
			}
		}
		return null;// no SSID
	}

	/**
	 * decode
	 */
	private void setCharacter() {
		if ((charset != null) && (!DEFAULT_CHARSET.equals(charset))) {
			Set<String> set = parameters.keySet();
			for (String s : set) {
				try {
					String[] str = parameters.get(s);
					String[] newStr = new String[str.length];
					for (int i = 0; i < str.length; i++) {
						String value = URLEncoder.encode(str[i],
								DEFAULT_CHARSET);
						value = URLDecoder.decode(value, charset);
						newStr[i] = value;
					}
					parameters.put(s, newStr);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if(message != null){
			try {
				String value = URLEncoder.encode(message,DEFAULT_CHARSET);
				message = URLDecoder.decode(value,charset);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(getQueryString() != null){
			try {
				String value = getQueryString();
				value = URLDecoder.decode(value,DEFAULT_CHARSET);
				requestLine.setQueryString(URLEncoder.encode(value,charset));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		charSet = true;
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		// TODO Auto-generated method stub
		if (!charSet)
			setCharacter();
		return parameters;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		Set<String> set = parameters.keySet();
		String[] t = new String[set.size()];
		String[] s = set.toArray(t);
		return new Enumeration<String>() {
			private int count = 0;

			@Override
			public boolean hasMoreElements() {
				return count < s.length;
			}

			@Override
			public String nextElement() {
				return s[count++];
			}
		};
	}

	@Override
	public String[] getParameterValues(String name) {
		if (!charSet) {
			setCharacter();
		}
		return parameters.get(name);
	}

	@Override
	public String getProtocol() {
		// TODO Auto-generated method stub
		return requestLine.getProtocol();
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	@Override
	public BufferedReader getReader() throws IOException {
		if(useInput)
			return null;
		useReader = true;
		if(reader == null){// maybe both getQueryString != null and message != null are true,so combine them
			String allValue = "";
			if(message != null)
				allValue += message;
			allValue += "&";
			if(getQueryString() != null)
				allValue += getQueryString();
			reader = new StringReader(allValue);
			br = new BufferedReader(reader);
		}
		return br;
	}

	@Override
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteHost() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRemotePort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getScheme() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServerName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getServerPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeAttribute(String arg0) {
		if(attributes.containsKey(arg0)){ // must check, or listener will fail and programmers will be confused(mengbi)
			executeListener(Constants.REQUEST_ATTRIBUTE_REMOVE
					,new ServletRequestAttributeEvent(((Context)container).getServletContext(),this,arg0,attributes.get(arg0)));
			attributes.remove(arg0);
		}
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		if(attributes.containsKey(arg0))
			executeListener(Constants.REQUEST_ATTRIBUTE_REPLACE,
					new ServletRequestAttributeEvent(((Context)container).getServletContext(),this,arg0,arg1));
		executeListener(Constants.REQUEST_ATTRIBUTE_ADD,
				new ServletRequestAttributeEvent(((Context)container).getServletContext(),this,arg0,arg1));
		attributes.put(arg0, arg1);
	}

	@Override
	public void setCharacterEncoding(String charset)
			throws UnsupportedEncodingException {
		this.charset = charset;
	}

	@Override
	public String getAuthType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContextPath() {
		return requestLine.getContextPath();
	}

	@Override
	public Cookie[] getCookies() {
		return cookies;
	}

	@Override
	public long getDateHeader(String arg0) {
		return 0;
	}

	@Override
	public String getHeader(String arg0) {
		// TODO Auto-generated method stub
		return header.getHeader(arg0);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		// TODO Auto-generated method stub
		Enumeration<String> e = header.getHeaderNames();
		return e;
	}

	@Override
	public Enumeration<String> getHeaders(String arg0) {
		String[] values = header.getHeaders(arg0);
		return new Enumeration<String>() {
			private int count = 0;

			@Override
			public boolean hasMoreElements() {
				return count < values.length;
			}

			@Override
			public String nextElement() {
				return values[count++];
			}
		};
	}

	@Override
	public int getIntHeader(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getMethod() {
		// TODO Auto-generated method stub
		return requestLine.getMethod();
	}

	@Override
	public String getPathInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getQueryString() {
		// TODO Auto-generated method stub
		return requestLine.getQueryString();
	}

	@Override
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestURI() {
		// TODO Auto-generated method stub
		return requestLine.getRequestUri();
	}

	@Override
	public StringBuffer getRequestURL() {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer();
		return sb
				.append("http://" + header.getHeader("Host") + getRequestURI());
	}

	@Override
	public String getRequestedSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSession(HttpSession session) {
		this.session = session;
	}

	@Override
	public HttpSession getSession() {
		return session;
	}

	@Override
	public HttpSession getSession(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUserInRole(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AsyncContext getAsyncContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DispatcherType getDispatcherType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return ((Context)container).getServletContext();
	}

	@Override
	public boolean isAsyncStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AsyncContext startAsync() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean authenticate(HttpServletResponse arg0) throws IOException,
			ServletException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Part getPart(String arg0) throws IOException, IllegalStateException,
			ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Part> getParts() throws IOException,
			IllegalStateException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void login(String arg0, String arg1) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void logout() throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public long getContentLengthLong() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String changeSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> arg0)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void start() {
		executeListener(Constants.REQUEST_CREATE,new ServletRequestEvent(getServletContext(),this));
	}

	@Override
	public void stop() {
		executeListener(Constants.REQUEST_DESTROY,new ServletRequestEvent(getServletContext(),this));
	}

	@Override
	public void addListeners(Class<? extends EventListener> clazz, EventListener listener) {
		lisExecutor.addListener(clazz,listener);
	}

	@Override
	public void removeListener(Class<? extends EventListener> clazz, EventListener listener) {
		lisExecutor.removeListener(clazz, listener);
	}

	@Override
	public void executeListener(String status, Object data) {
		lisExecutor.executeListener(status, data);
	}

}
