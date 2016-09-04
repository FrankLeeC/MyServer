package com.lwy.myserver.loader;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashSet;
import java.util.Set;

/**
 * classes contains all javaee class name
 * if name is in classes,it means name refers to javaee class
 * classes包含所有的javaee 类名
 * 如果name在classes中，说明这是一个javaee class
 * Created by frank lee on 7/12/2016.
 */
public class JavaEEClassSets {

    private static Set<String> classes = new HashSet<>();

    protected static boolean containsClass(String name){
        return classes.contains(name);
    }

    static{
        try {
            LineNumberReader reader = new LineNumberReader(new FileReader("src/com/lwy/myserver/loader/JavaEEClass.txt"));
            String s;
            while((s = reader.readLine()) != null)
                classes.add(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
