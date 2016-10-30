package at.ac.univie.dse2016.stream.boerse;

import at.ac.univie.dse2016.stream.common.Auftrag;

public class EmittentSection {
	
	
	public EmittentSection() {
		sell = new java.util.TreeMap<Float, java.util.ArrayList<Auftrag> >();
		buy = new java.util.TreeMap<Float, java.util.ArrayList<Auftrag> >(new DescendingFloatComparator());
	}
	
	public java.util.TreeMap<Float, java.util.ArrayList<Auftrag> > sell;
	public java.util.TreeMap<Float, java.util.ArrayList<Auftrag> > buy;

	public java.util.TreeMap<Integer, UDPSession > sessionsUDP;
	
	/*public java.util.TreeMap<Integer, java.util.ArrayList<Integer> > commitedAuftraege;
	
	public void setCommitedAuftrage(int auftragId1, int auftragId2) {
		if (!commitedAuftraege.containsKey(auftragId1))
			commitedAuftraege.put(auftragId1, new java.util.ArrayList<Integer>() );
		commitedAuftraege.get(auftragId1).add(auftragId2);

		if (!commitedAuftraege.containsKey(auftragId2))
			commitedAuftraege.put(auftragId2, new java.util.ArrayList<Integer>() );
		commitedAuftraege.get(auftragId2).add(auftragId1);
	}*/
	
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
