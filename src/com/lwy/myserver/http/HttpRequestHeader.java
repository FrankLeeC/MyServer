package com.lwy.myserver.http;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpRequestHeader {
	private Map<String, String[]> headers = new HashMap<>();

	/**
	 * change to lazy initialize
	 * 
	 * @param name
	 *            name of header
	 * @return value of header
	 */
	public String[] getHeaders(String name) {
		return headers.get(name);
	}

	public String getHeader(String name) {
		return headers.get(name)[0];
	}

	public Enumeration<String> getHeaderNames() {
		Set<String> sets = headers.keySet();
		String[] t = new String[sets.size()];
		String[] s = sets.toArray(t);
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

	public void addHeader(String name, String[] value) {
		headers.put(name, value);
	}

}
