package com.lwy.myserver.loader;


import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**common class loader
 * repositories are:
 * 1.jars of this myserver
 * 2.javaee-api jar
 * Created by frank lee on 7/10/2016.
 */
public class CommonClassLoader extends ClassLoader{

    private ClassLoader parent; //app class loader    later,change it to ext loader
    private URL[] urls; //urls[0] must be javaee-api
    private Map<String,Class<?>> classes = new ConcurrentHashMap<>();

    public CommonClassLoader(ClassLoader parent, URL[] urls) {
        this.parent = parent;
        this.urls = urls;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) {
        if(name == null || "".equals(name))
            return null;
        Class<?> clazz = classes.get(name);
        if(clazz == null) {
            try {
                clazz = parent.getParent().loadClass(name);
            } catch (ClassNotFoundException e) {
                clazz = findClass(name);
            }
        }
        return clazz;
    }

    @Override
    protected Class<?> findClass(String name) {
        Class<?> clazz = null;
        try {
            clazz = readClass(name);
        } catch (ClassNotFoundException e) {
            System.err.println("can not find class:"+name);
            e.printStackTrace();
        }
        return clazz;
    }

    /**
     * findClass must throws @ClassNotFoundException, but it won't,
     * so add this method and throws @ClassNotFoundException,
     * then, findClass will throws @ClassNotFoundException.
     * @param name class name
     * @return clazz
     * @throws ClassNotFoundException
     */
    private Class<?> readClass(String name) throws ClassNotFoundException{
        if(name.startsWith("javax")){
            return findJavaEEClass(name);
        }
        else
            return findInternalClass(name);
    }

    /**
     * load javaee-api class
     * @param name class name
     * @return
     */
    private Class<?> findJavaEEClass(String name){
        String root = urls[0].getPath();
        Class<?> clazz = realFindClass(root,name);
        if(clazz != null) {
            classes.put(name,clazz);
            return clazz;
        }
        return null;
    }

    /**
     * load other class, e.g.:org.dom4j......
     * @param name class name
     * @return
     */
    private Class<?> findInternalClass(String name){
        Class<?> clazz = null;
        for (int i = 0; i < urls.length; i++) {
            String root = urls[i].getPath();
            clazz = realFindClass(root,name);
            if(clazz != null)
                break;
        }
        if(clazz != null) {
            classes.put(name,clazz);
            return clazz;
        }
        return null;
    }

    private Class<?> realFindClass(String root,String name){
        Class<?> clazz = null;
        try{
            if (root.endsWith(".jar")) {
                JarFile jar = new JarFile(root);
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (name.replaceAll("\\.", "/").concat(".class").equals(entry.getName())) {
                        InputStream in = jar.getInputStream(entry);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        byte[] read = new byte[1024];
                        byte[] classBytes;
                        int len;
                        while ((len = in.read(read)) > 0) {
                            out.write(read, 0, len);
                        }
                        classBytes = out.toByteArray();
                        clazz = defineClass(name, classBytes, 0, classBytes.length);
                        break;
                    }
                }
            }
            else{
                File file = new File(root + "\\" + name.replaceAll("\\.","/").concat(".class"));
                if(file.exists()){
                    FileInputStream in = new FileInputStream(file);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] read = new byte[1024];
                    byte[] classBytes;
                    int len;
                    while((len = in.read(read)) > 0){
                        out.write(read,0,len);
                    }
                    classBytes = out.toByteArray();
                    clazz = defineClass(name,classBytes,0,classBytes.length);
                }
            }
        }catch (IOException e){
            System.err.println("fail to load class:"+name);
            e.printStackTrace();
        }
        return clazz;
    }
}
