package com.lwy.myserver.valve;

import com.lwy.myserver.container.Container;
import com.lwy.myserver.exceptions.NoValveException;
import com.lwy.myserver.http.HttpRequest;
import com.lwy.myserver.http.HttpResponse;

/**
 * Be in common use for Host,Context,Wrapper
 * Container can be Host,Context or Wrapper
 * Created by frank lee on 2016/7/14.
 */
public class CommonValveContext implements ValveContext{
    private Valve[] valves = new Valve[0];
    private static ThreadLocal<Integer> local = new ThreadLocal<>(); //set and get methods are thread-safe
    private Container container;
    public CommonValveContext(Container container){
        this.container = container;
    }
    @Override
    public void invoke(HttpRequest request, HttpResponse response,
                       ValveContext context) {
        Integer takeIndex = local.get();
        if(takeIndex == null){
            takeIndex = new Integer(0);
        }
        Valve valve = getValve(takeIndex++);
        if(valve != null){
            local.set(takeIndex);
            valve.invoke(request, response, context);
        }
        else{
            try {
                throw new NoValveException("no valve");
            } catch (NoValveException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public ValveContext addValve(Valve valve) {
        Valve[] newValves = new Valve[valves.length+1];
        for(int i=0;i<valves.length;i++)
            newValves[i] = valves[i];
        newValves[valves.length] = valve;
        valves = newValves;
        return this;
    }

    @Override
    public void remove(Valve valve) {
        if(valves.length == 0)
            return;
        else{
            for(int i=0;i<valves.length;i++){
                if(valve == valves[i]){
                    Valve[] newValves = new Valve[valves.length-1];
                    for(int j=0;j<i;j++)
                        newValves[j] = valves[j];
                    for(int j=i;j<valves.length-1;j++)
                        newValves[j] = valves[j+1];
                    valves = newValves;
                    break;
                }
            }
        }
    }

    /**
     * valves is property, so must add synchronized
     */
    @Override
    public synchronized Valve getValve(int index) {
        return valves[index];
    }

    @Override
    public Container getContainer(){
        return container;
    }
}