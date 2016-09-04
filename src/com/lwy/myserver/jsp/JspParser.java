package com.lwy.myserver.jsp;

import com.lwy.myserver.util.Constants;

import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.JspPage;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * parse jsp and save it
 * Created by frank lee on 2016/7/14.
 */
public class JspParser {

    private JspFactory jspFactory;
    private JspApplicationContext jspApplicationContext;
    private Map<String,JspPage> jspServlets = new ConcurrentHashMap<>();

    public JspParser(JspFactory jspFactory,JspApplicationContext jspApplicationContext){
        this.jspFactory = jspFactory;
        this.jspApplicationContext = jspApplicationContext;
    }

    public JspPage getJspServlet(String contextName,String jspPath,ClassLoader webLoader){
        JspPage jsp = jspServlets.get(jspPath);
        if(jsp == null){
            synchronized (jspServlets){
                if(jsp == null){
                    jsp = createJspPage(contextName,jspPath,webLoader);
                }
            }
        }
        return jsp;
    }

    /**
     * if already parse this jsp before, then just load it,
     * else parse it
     * 如果已经解析过，那么就加载该类
     * 如果没有，就解析
     * @param contextName context name e.g. bbs
     * @param jspPath jsp path   e.g. /user/login.jsp
     * @param webLoader web app class loader
     * @return
     */
    private JspPage createJspPage(String contextName,String jspPath,ClassLoader webLoader){
        String path = Constants.JSP_ROOT + File.separator + contextName
                        + File.separator + "com\\lwy\\server\\jsp\\" + jspPath.replace(".jsp","_servlet.class");
        File file = new File(path);
        String name = jspPath.replaceAll("/",".").replace(".jsp","_servlet.class");
        if(!file.exists()){ //if not parsed, then parse it
            String location = Constants.WEBROOT + "\\" + contextName + jspPath;
            try {
                FileInputStream in = new FileInputStream(location);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] read = new byte[1024];
                byte[] target;
                int len;
                while((len=in.read(read))>0){
                    out.write(read,0,len);
                }
                target = out.toByteArray(); //target--->String    pageEncoding is set in jsp,get it first
                String encode = getPageEncoding(target);
                String jsp = new String(target,encode);
                createJspServlet(jsp,webLoader,path,name);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return newInstance(name,webLoader);
    }

    private JspPage newInstance(String name,ClassLoader webLoader){
        try {
            Class<?> clazz = webLoader.loadClass("com.lwy.server.jsp"+name);
            Constructor constructor = clazz.getConstructor(JspFactory.class);
            return (JspPage) constructor.newInstance(jspFactory);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException |                                 InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * default is utf-8
     * @param bytes
     * @return
     */
    private String getPageEncoding(byte[] bytes){
        int len = bytes.length;
        int end;
        for(end=0;end<len;end++){
            if(((char)bytes[end]) == '>'){
                break;
            }
        }
        int start ;
        for(start=end;start>0;start--){
            if(((char)bytes[start]) == '='){
                break;
            }
        }
        start += 2;
        end -= 3;
        byte[] pageEncoding = new byte[end-start+1];
        for(int i=0;i<pageEncoding.length;i++){
            pageEncoding[i] = bytes[start+i];
        }
        String code = new String(pageEncoding);
        if(code == null)
            code = "UTF-8";
        return code;
    }

    /**
     * 先解析与上下文无关的java代码，生成html，然后再解析html里面的el expression
     * @param source
     * @param webAppLoader
     * @param path class文件的路径
     */
    private void createJspServlet(String source,ClassLoader webAppLoader,String path,String name){
        String packageName = getPackage(name);
        String html = createJspHtml(source,webAppLoader,path);
        createServlet(html,path,webAppLoader,packageName);
    }

    /**
     * 解析与上下文无关的java代码，生成html
     * @param source
     * @param webAppLoader
     * @param path
     * @return
     */
    private String createJspHtml(String source,ClassLoader webAppLoader,String path){
        StringBuilder src = new StringBuilder();
        int importIndex = source.indexOf("import=\"");
        int importTail = source.indexOf("\"",importIndex+"import=\"".length());
        String importClass = source.substring(importIndex+"import=\"".length(),importTail);
        String[] str = importClass.split(","); //import is split by comma
        for(int i=0;i<str.length;i++){
            String s = str[i];
            if(s.endsWith(";"))
                s = s.substring(0,s.length()-1);
            src.append("import ").append(s).append(";\r\n");
        }
        StringBuilder target = new StringBuilder(); //all code
        List<String> codeList = new ArrayList<>();
        List<String> resultList = new ArrayList<>();
        int htmlIndex = source.indexOf("<!DOCTYPE html");       //maybe html5
        source = source.substring(htmlIndex);
        int index = 0;
        while(source.indexOf("<%",index)>=index){
            index = source.indexOf("<%",index);
            int tail = source.indexOf("%>",index);
            String temp = source.substring(index,index+3);
            if("<%=".equals(temp)){
                String result = source.substring(index,tail+2);
                resultList.add(result);
                target.append(result);
                index = tail + 1;
                /*
                 index = tail + 1;
                 reason:
                 in html
                            <% ghurhguiyuwr %>
                            <%=s %>


                            <% ghuiygu %>

                            <%=d %>
                 but in string ,it is:
                 <% ghurhguiyuwr %><%=s %><% ghuiygu %><%=d %>
                 so if index = tail + 2; may ignore code
                  */
            }
            else{
                String code = source.substring(index,tail+2);
                codeList.add(code);                           			//<% code %>
                String realCode = code.substring(2,code.length()-2);    // code
                target.append(realCode);
                index = tail + 1;
            }
        }
        String re = generate(src.toString(),target.toString(),webAppLoader,path); //get result
        re = decode(re);                             //decode
        String[] res = re.split("&");                //split by &
        for (int i = 0; i < res.length; i++) {
            source = source.replace(resultList.get(i), res[i]);
        }
        for (int i = 0; i < codeList.size(); i++) {  //replace all code
            source = source.replace(codeList.get(i),"");
        }
//            FileOutputStream out = new FileOutputStream(path.replace("_servlet.class",".html"));
//            out.write(source.getBytes());
//            out.flush();
//            out.close();
        return source;
    }

    private String getPackage(String name){
        int last = name.lastIndexOf(".");
        name = name.substring(0,last);
        int secondLast = name.lastIndexOf(".");
        name = name.substring(0,secondLast);
        return "com.lwy.server.jsp."+name;
    }

    private String getClassName(String path){
        int last = path.lastIndexOf(".");
        path = path.substring(0,last);
        int secondLast = path.lastIndexOf(".");
        return path.substring(secondLast+1);
    }

    private void createServlet(String source,String path,ClassLoader webAppLoader,String packageName){
        path = path.replace(".class",".java");
        String className = getClassName(path);
        StringBuilder servletSource = new StringBuilder();
        servletSource.append("package").append(" ").append(packageName).append(";\n");
        servletSource.append("import com.lwy.myserver.jsp.*;\n");
        servletSource.append("import javax.servlet.jsp.*;\n");
        servletSource.append("import javax.servlet.ServletConfig;\n");
        servletSource.append("import javax.servlet.ServletException;\n");
        servletSource.append("import javax.servlet.ServletRequest;\n");
        servletSource.append("import javax.servlet.ServletResponse;\n");
        servletSource.append("import javax.servlet.http.HttpServletRequest;\n");
        servletSource.append("import javax.servlet.http.HttpServletResponse;\n");
        servletSource.append("import javax.servlet.jsp.HttpJspPage;\n");
        servletSource.append("import java.io.IOException;\n");
        servletSource.append("public class").append(" ").append(className).append(" ").append("extends JspPageBase {\n");
        servletSource.append("  private JspFactory jspFactory;\n");
        servletSource.append("  public").append(" ").append(className).append(" ").append("(JspFactory jspFactory){\n");
        servletSource.append("      this.jspFactory = jspFactory;\n");
        servletSource.append("  };\n");

        servletSource.append("  public void _jspService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{\n");
        servletSource.append("\n");               // 等待实现
        servletSource.append("  }\n");

        servletSource.append("  public void jspInit() {\n");
        servletSource.append("\n");               // 等待实现
        servletSource.append("  }\n");

        servletSource.append("  public void jspDestroy() {\n");
        servletSource.append("\n");              // 等待实现
        servletSource.append("  }\n");



    }

    private String decode(String str){
        str = str.replaceAll("%27","&");
        str = str.replaceAll("%25","%");
        return str;
    }

    private String randomName(){
        char[] chars = new char[3];
        for (int i = 0; i < 3; i++) {
            byte b = (byte) (Math.random()*26+65);
            chars[i] = (char) b;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(chars[0]).append(chars[1]).append(chars[2]);
        return sb.toString();
    }

    private String generate(String importClass,String code,ClassLoader webAppLoader,String path){
        path = path.replace("_servlet.class","_java.java");
        String outStream = "";
        do{
            outStream = "_JASPER_OUT_" + randomName();
        }while(code.contains(" "+outStream));
        try {
            while(code.contains("<%=")){
                int index = code.indexOf("<%=");
                int tail = code.indexOf("%>",index);
                String hcode = code.substring(index,tail+2);
                String rcode = code.substring(index+3,tail).trim();
                String mcode = outStream + ".write(encode(String.valueOf("+rcode+")).getBytes());\n";
                code = code.replace(hcode,mcode);
            }
            OutputStream out = new FileOutputStream(path);
            StringBuilder src = new StringBuilder();
            src.append("package file;\n");
            src.append(importClass);
            src.append("import java.io.IOException;\r\n");
            src.append("import java.io.ByteArrayOutputStream;\n");
            src.append("public class First{\n");
            src.append("	private ByteArrayOutputStream " + outStream + " = new ByteArrayOutputStream();\r\n");
            src.append("	public String process(){\n");
            src.append("		try{\n");
            src.append("			"+code+"\n");
            src.append("			return new String(" + outStream + ".toByteArray());\n");
            src.append("		}\n");
            src.append("		catch(IOException e){\n");
            src.append("		}\n");
            src.append("		return null;\n");
            src.append("	}\n");
            src.append("	private String encode(String str){\n");
            src.append("		str = str.replaceAll(\"%\",\"%25\");\n");
            src.append("		str = str.replaceAll(\"&\",\"%27\");\n");
            src.append("		str = str + \"&\";\n");
            src.append("		return str;\n");
            src.append("	}\n");
            src.append("}\n");
            out.write(src.toString().getBytes());
            out.flush();
            out.close();

            JavaCompiler compiler= ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager jfm=compiler.getStandardFileManager(null, null, null);
            Iterable<? extends JavaFileObject> units=jfm.getJavaFileObjects(path);
            JavaCompiler.CompilationTask ct=compiler.getTask(null, jfm, null, null, null, units);
            ct.call();
            jfm.close();

            int index = path.indexOf("com.lwy.server.jsp.");
            String javaPath = path.substring(index+"com.lwy.server.jsp.".length()).replace("_servlet.class","_java");
            Class<?> c = webAppLoader.loadClass(javaPath);
            Object obj = c.newInstance();
            Method method = c.getDeclaredMethod("process");
            String result = (String) method.invoke(obj);
            System.out.println(result);
            return result;
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
