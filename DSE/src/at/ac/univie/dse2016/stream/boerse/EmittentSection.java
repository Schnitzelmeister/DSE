package at.ac.univie.dse2016.stream.boerse;

import java.util.concurrent.atomic.AtomicInteger;

import at.ac.univie.dse2016.stream.common.Auftrag;

public class EmittentSection {
	
	AtomicInteger msgCounter;
	
	public EmittentSection() {
		sell = new java.util.TreeMap<Integer, java.util.TreeSet<Auftrag> >();
		buy = new java.util.TreeMap<Integer, java.util.TreeSet<Auftrag> >(java.util.Collections.reverseOrder());
		activeUDPSessions = new java.util.TreeMap<Integer, UDPSession >();
		msgCounter = new AtomicInteger();
		msgCounter.set(0);
	}
	
	public java.util.TreeMap<Integer, java.util.TreeSet<Auftrag> > sell;
	public java.util.TreeMap<Integer, java.util.TreeSet<Auftrag> > buy;
	
	public java.util.TreeMap<Integer, UDPSession > activeUDPSessions;

}
