package com.lwy.fal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class MainServlet
 */
@WebServlet("/MainServlet")
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MainServlet() {
        super();
        // TODO Auto-generated constructor stub
        System.out.println("MainServlet created");
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		out.print("hello\n");
		out.print("code="+request.getParameter("code")+"   code2="+request.getParameter("code2")+"\n");
		System.out.println("===================");
//		System.out.println("name="+request.getParameter("name")+"   value="+request.getParameter("value"));
//		out.println("name="+request.getParameter("name")+"   value="+request.getParameter("value"));
//		System.out.println("request.getQueryString():"+request.getQueryString());
//		System.out.println("request.getRequestURI():"+request.getRequestURI());
//		System.out.println("request.getLocalAddr():"+request.getLocalAddr());
//		System.out.println("request.getLocalName():"+request.getLocalName());
//		System.out.println("request.getRequestURL():"+request.getRequestURL());
//		System.out.println("request.getLocalPort():"+request.getLocalPort());
//		System.out.println("request.getLocale():"+request.getLocale());
//		System.out.println("request.getLocales():"+request.getLocales());
//		System.out.println("request.getPathInfo():"+request.getPathInfo());
//		System.out.println("request.getPathTranslated():"+request.getPathTranslated());
//		System.out.println("request.getProtocol():"+request.getProtocol());
//		System.out.println("request.getServerName():"+request.getServerName());
//		System.out.println("request.getServerPort():"+request.getServerPort());
//		System.out.println("request.getServletContext():"+request.getServletContext());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	//	request.setCharacterEncoding("utf-8");
		response.setHeader("content-type", "text/html,charset=utf-8");
		String name = request.getParameter("name");
		System.out.println("before name="+name);
		name = URLEncoder.encode(name, "ISO-8859-1");
		System.out.println("======++++"+name);
		name = URLDecoder.decode(name, "utf-8");
		System.out.println("++++++++====="+name);
//		name = URLDecoder.decode(name, "utf-8");
		name = new String(name.getBytes("ISO-8859-1"),"utf-8");
		System.out.println("after decode  name="+name);
		System.out.println("name:"+request.getParameter("name"));
		System.out.println("value:"+request.getParameter("value"));
	}

}
