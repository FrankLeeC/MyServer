package com.lwy.myserver.jsp;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.HttpJspPage;
import java.io.IOException;

/**
 * implementation of HttpJspPage
 * Created by frank lee on 7/12/2016.
 */
public abstract class JspPageBase implements HttpJspPage {
    private ServletConfig config;

    @Override
    abstract public void _jspService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

    @Override
    public void jspInit() {

    }

    @Override
    public void jspDestroy() {

    }

    @Override
    final public void init(ServletConfig config) throws ServletException {
        this.config = config;
        jspInit();
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    @Override
    final public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        _jspService((HttpServletRequest)req,(HttpServletResponse)res);
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    final public void destroy() {
        jspDestroy();
    }
}
