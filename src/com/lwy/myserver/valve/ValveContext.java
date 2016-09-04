package com.lwy.myserver.valve;


public interface ValveContext extends Valve{

	public ValveContext addValve(Valve valve);
	public void remove(Valve valve);
	public Valve getValve(int index); //must synchronized
}
