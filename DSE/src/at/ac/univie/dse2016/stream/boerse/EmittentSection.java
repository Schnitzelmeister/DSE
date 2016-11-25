package at.ac.univie.dse2016.stream.boerse;

import java.util.concurrent.atomic.AtomicInteger;

import at.ac.univie.dse2016.stream.common.Auftrag;

public class EmittentSection {
	
	AtomicInteger msgCounter;
	
	public EmittentSection() {
		sell = new java.util.TreeMap<Float, java.util.TreeSet<Auftrag> >();
		buy = new java.util.TreeMap<Float, java.util.TreeSet<Auftrag> >(new DescendingFloatComparator());
		msgCounter.set(0);
	}
	
	public java.util.TreeMap<Float, java.util.TreeSet<Auftrag> > sell;
	public java.util.TreeMap<Float, java.util.TreeSet<Auftrag> > buy;

	public java.util.TreeMap<Integer, UDPSession > sessionsUDP;
	
	static class DescendingFloatComparator implements java.util.Comparator<Float> {
	   	public int compare(Float p1, Float p2)
		{
			if (p1 == p2)
				return 0;
			else if (p2 > p1)
				return 1;
			else
				return -1;
		}
	}
	
	public java.util.TreeMap<Integer, UDPSession > activeUDPSessions;

}
