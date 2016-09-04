package com.lwy.myserver.valve;

import com.lwy.myserver.container.Container;
import com.lwy.myserver.http.HttpRequest;
import com.lwy.myserver.http.HttpResponse;

/**
 * Created by frank lee on 2016/7/14.
 */
public class FirstContextValve implements Valve{
    private Container container;
    public FirstContextValve(){}
    public FirstContextValve(Container container){
        this.container = container;
    }
    @Override
    public Container getContainer() {
        return container;
    }

    @Override
    public void invoke(HttpRequest request, HttpResponse response,
                       ValveContext context) {
        System.out.println("=====come in first valve");
        context.invoke(request, response, context);
        System.out.println("=====come out first valve");
    }
}
