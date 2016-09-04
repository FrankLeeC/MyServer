package com.lwy.myserver.jsp;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspEngineInfo;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * implementation of JspFactory
 * 与Context在同一级
 * Created by frank lee on 7/12/2016.
 */
public class SimpleJspFactory extends JspFactory{

    private ConcurrentHashMap<ServletContext,JspApplicationContext> jspApplicationContexts = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Servlet,PageContext> pageContexts = new ConcurrentHashMap<>();

    public SimpleJspFactory(){}

    /**
     * 初步想法：通过servlet来获取到page context,page context作为service方法的局部变量
     * @param servlet   the requesting servlet
     * @param request	the current request pending on the servlet
     * @param response	the current response pending on the servlet
     * @param errorPageURL the URL of the error page for the requesting JSP, or null
     * @param needsSession true if the JSP participates in a session
     * @param buffer	size of buffer in bytes, JspWriter.NO_BUFFER if no buffer,
     *			JspWriter.DEFAULT_BUFFER if implementation default.
     * @param autoflush	should the buffer autoflush to the output stream on buffer
     *			overflow, or throw an IOException?
     *
     * @return
     */
    @Override
    public PageContext getPageContext(Servlet servlet, ServletRequest request, ServletResponse response, String errorPageURL,                                           boolean needsSession, int buffer, boolean autoflush) {
        PageContext pageContext = pageContexts.get(servlet);
        if(pageContext == null){
            PageContext pc = new SimplePageContext();
            try {
                pc.initialize(servlet,request,response,errorPageURL,needsSession,buffer,autoflush);
            } catch (IOException e) {
                e.printStackTrace();
            }
            PageContext temp = pageContexts.putIfAbsent(servlet,pc);
            return temp == null? pc : temp;
        }
        return pageContext;
    }

    @Override
    public void releasePageContext(PageContext pc) {

    }

    @Override
    public JspEngineInfo getEngineInfo() {
        return null;
    }

    @Override
    public JspApplicationContext getJspApplicationContext(ServletContext context) {
        JspApplicationContext jspApplicationContext = jspApplicationContexts.get(context);
        if(jspApplicationContext == null){
            JspApplicationContext instance = new SimpleJspApplicationContext();
            JspApplicationContext temp = jspApplicationContexts.putIfAbsent(context,instance);
            jspApplicationContext = temp == null? instance : temp;
        }
        return jspApplicationContext;
    }

}
