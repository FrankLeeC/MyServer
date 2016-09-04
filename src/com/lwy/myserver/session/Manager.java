package com.lwy.myserver.session;

import javax.servlet.http.HttpSession;

import com.lwy.myserver.container.Context;

public interface Manager {
	
	public HttpSession createSession(Context context);
	public HttpSession getSession(String sessionId);
	public void removeSession(HttpSession session);
}
