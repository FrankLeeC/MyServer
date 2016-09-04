package com.lwy.myserver.valve;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import com.lwy.myserver.container.Container;
import com.lwy.myserver.container.Context;
import com.lwy.myserver.container.Host;
import com.lwy.myserver.http.HttpRequest;
import com.lwy.myserver.http.HttpResponse;
import com.lwy.myserver.util.Constants;
/**
 * this valve is the first host-level valve, it checks session in request, bound session to it or create a new one
 * @author frank lee
 *
 */
public class SessionValve implements Valve {

	private Container container; //@SimpleHost
	public SessionValve(Container container){
		this.container = container;
	}
	
	/**
	 * can not use request.getParameter(Constants.SSID),must parse request.getQueryString
	 * because the first time request.getParameter() is called, setCharacter will transfer parameters' value to
	 * the code that programmer specify.
	 * but at this time, server doesn't know the code, so can't transfer here
	 * 不可以使用request.getParameter(Constants.SSID)，必须使用解析request.getQueryString的方法
	 * 因为当第一次使用request.getParameter()时，setCharacter会被调用，它会将parameters' value编码成程序员规定的
	 * 但是此时，server尚未知道编码，所以不能在这里转换
	 * 
	 * first, check query string to assign ssid,then check ssid in cookie.
	 * in case of programmer ues SSID as a parameter name in their system, so we can must use ssid in cookie to 
	 * cover ssid in query string
	 * however,if cookie don't contain ssid,query string will have ssid,in case of programmer ues SSID as 
	 * a parameter name in their system,we encodeURL at last,to cover programmer's ssid parameter value,and ensure
	 * value of ssid is session's id, not value of a parameter in their special system
	 * 为了防止程序员使用ssid作为他们系统的参数名，我们最后对url进行编码，去覆盖程序员的ssid参数的值，保证ssid的值是session id
	 */
	@Override
	public void invoke(HttpRequest request, HttpResponse response, ValveContext context) {
		String SSID = null;
		String query = request.getQueryString();
		String[] qs = query.split("&");
		String[] names = new String[qs.length];
		String[] values = new String[qs.length];
		for(int i=0;i<qs.length;i++){
			String[] pairs = qs[i].split("=");
			names[i] = pairs[0];
			values[i] = pairs[1];
		}
		for(int i=0;i<names.length;i++){
			if(Constants.SSID.equals(names[i])){
				SSID = values[i];
				break;
			}
		}
		//ssid in cookie will cover ssid in query string(ssid in query will not be a paramter of programmer's system,
		//read comments above)
		//however,ssid won't be in query string and cookie at same time.i just make sure ssid in cookie is the one i want
		Cookie[] cookies = request.getCookies();
		for(Cookie c:cookies){
			if(Constants.SSID.equals(c.getName()))
				SSID = c.getValue();
		}
		HttpSession session = null;
		if(SSID == null){
			session = container.getManager().createSession(((Host)container).getContext(request.getContextPath()));
			request.setSession(session);
		}
		else{
			session = container.getManager().getSession(SSID);
			request.setSession(session);
		}
		request.setParameters(Constants.SSID, new String[]{session.getId()});
		context.invoke(request, response, context);
		System.out.println("out of session valve");
	}

	@Override
	public Container getContainer() {
		// TODO Auto-generated method stub
		return container;
	}

}
