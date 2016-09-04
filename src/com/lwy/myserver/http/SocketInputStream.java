package com.lwy.myserver.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

public class SocketInputStream extends InputStream {

	private InputStream input;
	private HttpRequest request;
	private byte[] bytes;
	private int max = 0, pos = 0, start = 0;
	private static final byte SP = ' ';
	private static final byte LF = '\r';
	private static final byte CR = '\n';
	private static final byte COLON = ':';
	private static final byte QM = '?';

	public SocketInputStream(InputStream input, HttpRequest request, int size) {
		this.input = input;
		this.request = request;
		bytes = new byte[size];
	}

	public void prepare() {
		try {
			max = read();
			System.out.println(new String(bytes, 0, max));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readReqeust() {
		HttpRequestLine requestLine = readLine();
		request.setRequestLine(requestLine);
		HttpRequestHeader headers = readHeaders();
		request.setHttpHeader(headers);
		String message = readMessage();
		request.setMessage(message);
		Map<String, String[]> parameters = readParameters(requestLine, message);
		request.setParameters(parameters);
	}

	/**
	 * @param next
	 *            :move next bytes
	 */
	private void moveNext(int next) {
		start = pos + next;
		pos = start;
	}

	private HttpRequestLine readLine() {
		HttpRequestLine requestLine = new HttpRequestLine();
		// read request method
		boolean sp = false;
		while (!sp) {
			if (SP == bytes[pos]) {
				sp = true;
				requestLine.setMethod(new String(bytes, start, pos));
			} else {
				pos++;
			}
		}
		moveNext(1);
		// read request uri
		boolean sp2 = false;
		boolean query = false;
		int queryIndex = start;
		while (!sp2) {
			if (SP == bytes[pos]) {
				sp2 = true;
				requestLine
						.setRequestUri(new String(bytes, start, pos - start));
				requestLine.setUri(new String(bytes, start, pos - start));
			} else {
				if (QM == bytes[pos]) {
					query = true;
					queryIndex = pos;
				}
				pos++;
			}
		}
		// read requestUri and queryString if exist
		if (query) {
			requestLine.setRequestUri(new String(bytes, start, queryIndex
					- start));
			requestLine.setQueryString(new String(bytes, queryIndex + 1, pos
					- (queryIndex + 1)));
		}
		moveNext(1);
		
		//set request context path
		String requestUri = requestLine.getRequestUri();
		int index = requestUri.indexOf("/", 1); // search from 1 index, because index 0 is a "/"
		String contextPath = requestUri.substring(1,index);
		requestLine.setContextPath(contextPath);
		
		// read protocol and version
		boolean lf = false;
		while (!lf) {
			if (CR == bytes[pos]) {
				lf = true;
				requestLine.setProtocol(new String(bytes, start, pos - start
						- 1));
			} else {
				pos++;
			}
		}
		moveNext(1);
		return requestLine;
	}

	private HttpRequestHeader readHeaders() {
		HttpRequestHeader headers = new HttpRequestHeader();
		List<Cookie> cookieList = new ArrayList<>();
		boolean crlf = false;
		while (!crlf) {
			if (LF == bytes[pos]) {
				crlf = true;
			} else if (COLON == bytes[pos]) {
				String name = new String(bytes, start, pos - start);
				String value = null;
				start = pos + 1;
				pos = start;
				boolean crlf2 = false;
				while (!crlf2) {
					if ('\n' == bytes[pos]) {
						crlf2 = true;
						value = new String(bytes, start, pos - start - 1);
						String[] values = value.split(";");
						headers.addHeader(name, values);
						start = pos + 1;
						pos = start;
					} else {
						pos++;
					}
				}
				if (("Cookie".equals(name))) {
					String[] str = value.split(";");
					for (String s : str) {
						int index = s.indexOf("=");
						Cookie cookie = new Cookie(s.substring(0, index),
								s.substring(index + 1));
						cookieList.add(cookie);
					}
					request.setCookie(cookieList);
				}
			} else {
				pos++;
			}
		}
		moveNext(2);
		return headers;
	}

	private String readMessage() {
		return new String(bytes, start, max - start);
	}

	private Map<String, String[]> readParameters(HttpRequestLine requestLine,
			String message) {
		Map<String, String[]> parameters = new HashMap<>();
		if (requestLine.getQueryString() != null) {
			String[] str = requestLine.getQueryString().split("&");
			for (String s : str) {
				int index = s.indexOf("=");
				String value = s.substring(index + 1);
				value = transferMeaning(value);
				String[] values = value.split(";");
				parameters.put(s.substring(0, index), values);
			}
		}
		if (message != null && message.length() > 0) {
			String[] str = message.split("&");
			for (String s : str) {
				int index = s.indexOf("=");
				String name = s.substring(0, index);
				// name = transferMeaning(name);
				String value = s.substring(index + 1);
				value = transferMeaning(value);
				String[] values = value.split(";");
				parameters.put(name, values);
			}
		}
		return parameters;
	}

	/**
	 * transfer "+"("%20") to " " first, because "%2B" can be transferred to "+"
	 * transfer "%25" to "%" last at last, encode with ISO-8859-1(by default)
	 * 
	 * @param string
	 */
	private String transferMeaning(String string) {

		try {
			string = URLDecoder.decode(string, "ISO-8859-1");
			return string;
		} catch (UnsupportedEncodingException e) {
			return string;
		}
	}

	@Override
	public int read() throws IOException {
		return input.read(bytes, 0, bytes.length);
	}

}
