package com.lwy.myserver.http;

public class HttpRequestLine {

	private String method;
	private String uri; // the whole string
	private String requestUri; // string before '?'
	private String queryString; // string after '?'
	private String protocol;
	private String contextPath;

	protected String getMethod() {
		return method;
	}

	protected void setMethod(String method) {
		this.method = method;
	}

	protected String getUri() {
		return uri;
	}

	protected void setUri(String uri) {
		this.uri = uri;
	}

	protected String getRequestUri() {
		return requestUri;
	}

	protected void setRequestUri(String requestUri) {
		this.requestUri = requestUri;
	}

	protected String getQueryString() {
		return queryString;
	}

	protected void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	protected String getProtocol() {
		return protocol;
	}

	protected void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	protected String getContextPath() {
		return contextPath;
	}

	protected void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

}
