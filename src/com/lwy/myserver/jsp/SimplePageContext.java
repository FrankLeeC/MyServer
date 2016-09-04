package com.lwy.myserver.jsp;

import javax.el.ELContext;
import javax.print.DocFlavor;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static jdk.nashorn.internal.runtime.regexp.joni.encoding.CharacterType.D;

/**
 * implementation of PageContext
 * Created by frank lee on 7/12/2016.
 */
public class SimplePageContext extends PageContext{

    private HttpSession session;
    private Servlet servlet;
    private ServletContext servletContext;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private String errorPageURL;
    private boolean needsSession;
    private int bufferSize;
    private boolean autoFlush;

    private Map<String,Object> attributes = new HashMap<>();


    /**
     * <p>
     * The initialize method is called to initialize an uninitialized PageContext
     * so that it may be used by a JSP Implementation class to service an
     * incoming request and response within it's _jspService() method.
     *
     * <p>
     * This method is typically called from JspFactory.getPageContext() in
     * order to initialize state.
     *
     * <p>
     * This method is required to create an initial JspWriter, and associate
     * the "out" name in page scope with this newly created object.
     *
     * <p>
     * This method should not be used by page  or tag library authors.
     *
     * @param servlet The Servlet that is associated with this PageContext
     * @param request The currently pending request for this Servlet
     * @param response The currently pending response for this Servlet
     * @param errorPageURL The value of the errorpage attribute from the page
     *     directive or null
     * @param needsSession The value of the session attribute from the
     *     page directive
     * @param bufferSize The value of the buffer attribute from the page
     *     directive
     * @param autoFlush The value of the autoflush attribute from the page
     *     directive
     *
     * @throws IOException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     */
    @Override
    public void initialize(Servlet servlet, ServletRequest request, ServletResponse response, String errorPageURL, boolean needsSession, int bufferSize, boolean autoFlush) throws IOException, IllegalStateException, IllegalArgumentException {
        this.servlet = servlet;
        this.request = (HttpServletRequest) request;
        this.response = (HttpServletResponse) response;
        this.errorPageURL = errorPageURL;
        this.needsSession = needsSession;
        this.bufferSize = bufferSize;
        this.autoFlush = autoFlush;
    }

    @Override
    public void release() {

    }

    @Override
    public HttpSession getSession() {
        return session;
    }

    @Override
    public Object getPage() {
        return null;
    }

    @Override
    public ServletRequest getRequest() {
        return null;
    }

    @Override
    public ServletResponse getResponse() {
        return null;
    }

    @Override
    public Exception getException() {
        return null;
    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public void forward(String relativeUrlPath) throws ServletException, IOException {

    }

    @Override
    public void include(String relativeUrlPath) throws ServletException, IOException {

    }

    @Override
    public void include(String relativeUrlPath, boolean flush) throws ServletException, IOException {

    }

    @Override
    public void handlePageException(Exception e) throws ServletException, IOException {

    }

    @Override
    public void handlePageException(Throwable t) throws ServletException, IOException {

    }

    @Override
    public void setAttribute(String name, Object value) {
        setAttribute(name,value,PAGE_SCOPE);
    }

    @Override
    public void setAttribute(String name, Object value, int scope) {
        switch (scope) {
            case PAGE_SCOPE:
                attributes.put(name, value);
                break;
            case REQUEST_SCOPE:
                request.setAttribute(name,value);
        }
    }

    @Override
    public Object getAttribute(String name) {
        return getAttribute(name,PAGE_SCOPE);
    }

    @Override
    public Object getAttribute(String name, int scope) {
        switch (scope){
            case PAGE_SCOPE:
                return attributes.get(name);
            case REQUEST_SCOPE:
                return request.getAttribute(name);
            case SESSION_SCOPE:
                return session.getAttribute(name);
            case APPLICATION_SCOPE:
                return servletContext.getAttribute(name);
        }
        return null;
    }

    /**
     * Searches for the named attribute in page, request, session (if valid),
     * and application scope(s) in order and returns the value associated or
     * null.
     * @param name the name of the attribute to search for
     * @return object
     */
    @Override
    public Object findAttribute(String name) {
        Object object;
        if(attributes.containsKey(name)){
            return attributes.get(name);
        }
        object = request.getAttribute(name);
        if(object != null)
            return object;
        object = session.getAttribute(name);
        if(object != null)
            return object;
        object = servletContext.getAttribute(name);
        if(object != null)
            return object;
        return null;
    }

    @Override
    public void removeAttribute(String name) {
        removeAttribute(name,PAGE_SCOPE);
    }

    @Override
    public void removeAttribute(String name, int scope) {
        switch (scope){
            case PAGE_SCOPE:
                attributes.remove(name);
                break;
            case REQUEST_SCOPE:
                request.removeAttribute(name);
                break;
            case SESSION_SCOPE:
                session.removeAttribute(name);
                break;
            case APPLICATION_SCOPE:
                servletContext.removeAttribute(name);
                break;
        }
    }

    /**
     * Get the scope where a given attribute is defined.
     * @param name the name of the attribute to return the scope for
     * @return
     */
    @Override
    public int getAttributesScope(String name) {
        if(attributes.containsKey(name))
            return PAGE_SCOPE;
        else if(request.getAttribute(name) != null)
            return REQUEST_SCOPE;
        else if(session.getAttribute(name) != null)
            return SESSION_SCOPE;
        else if(servletContext.getAttribute(name) != null)
            return APPLICATION_SCOPE;
        return 0;
    }

    @Override
    public Enumeration<String> getAttributeNamesInScope(int scope) {
        switch (scope){
            case PAGE_SCOPE:
                Set<String> set = attributes.keySet();
                String[] temp = new String[set.size()];
                String[] s = set.toArray(temp);
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
            case REQUEST_SCOPE:
                return request.getAttributeNames();
            case SESSION_SCOPE:
                return session.getAttributeNames();
            case APPLICATION_SCOPE:
                return servletContext.getAttributeNames();
        }
        return null;
    }

    @Override
    public JspWriter getOut() {
        return null;
    }

    @Override
    public ExpressionEvaluator getExpressionEvaluator() {
        return null;
    }

    @Override
    public VariableResolver getVariableResolver() {
        return null;
    }

    @Override
    public ELContext getELContext() {
        return null;
    }
}
