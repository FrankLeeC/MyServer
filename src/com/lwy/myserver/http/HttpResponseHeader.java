package com.lwy.myserver.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpResponseHeader {

	private Map<String,String[]> headers = new HashMap<>();
	
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
	public void addHeader(String name,String value){
		String[] values = headers.get(name);
		if(values == null || values.length == 0)
			headers.put(name, new String[]{value});
		else{
			String[] newValues = new String[values.length+1];
			for(int i=0;i<values.length;i++)
				newValues[i] = values[i];
			newValues[values.length] = value;
			values = newValues;
			headers.put(name, values);
		}
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
	public void setHeader(String name,String value){
		headers.put(name, new String[]{value});
	}
	
	public void addIntHeader(String name,int value){
		String[] values = headers.get(name);
		if(values == null || values.length == 0){
			values = new String[]{String.valueOf(value)};
			headers.put(name, values);
		}
		else{
			String[] newValues = new String[values.length+1];
			for(int i=0;i<values.length;i++)
				newValues[i] = values[i];
			newValues[values.length] = String.valueOf(value);
			values = newValues;
			headers.put(name, values);
		}
	}
	
	public void setIntHeader(String name,int value){
		headers.put(name, new String[]{String.valueOf(value)});
	}
	
	public void addDateHeader(String name,long time){
		String[] times = headers.get(name);
		if(times == null || times.length == 0){
			times = new String[]{String.valueOf(time)};
			headers.put(name, times);
		}
		else{
			String[] newTimes = new String[times.length+1];
			for(int i=0;i<times.length;i++)
				newTimes[i] = times[i];
			newTimes[times.length] = String.valueOf(time);
			times = newTimes;
			headers.put(name, times);
		}
	}
	
	public void setDateHeader(String name, long time){
		headers.put(name, new String[]{String.valueOf(time)});
	}
	
	public boolean containsHeader(String name){
		if(headers.containsKey(name))
			return true;
		return false;
	}
	
	/**
     * Gets the value of the response header with the given name.
     * 
     * <p>If a response header with the given name exists and contains
     * multiple values, the value that was added first will be returned.
     *
     * <p>This method considers only response headers set or added via
     * {@link #setHeader}, {@link #addHeader}, {@link #setDateHeader},
     * {@link #addDateHeader}, {@link #setIntHeader}, or
     * {@link #addIntHeader}, respectively.
     *
     * @param name the name of the response header whose value to return
     *
     * @return the value of the response header with the given name,
     * or <tt>null</tt> if no header with the given name has been set
     * on this response
     *
     * @since Servlet 3.0
     */
	public String getHeader(String name){
		return headers.get(name)[0];
	}
	
	public Collection<String> getHeaders(String name) {
		String[] values = headers.get(name);
		List<String> list = Arrays.asList(values);
		return list;
	}
	
	public Collection<String> getHeaderNames() {
		List<String> list = new ArrayList<>();
		Set<String> sets = headers.keySet();
		for(String s:sets)
			list.add(s);
		return list;
	}
}
