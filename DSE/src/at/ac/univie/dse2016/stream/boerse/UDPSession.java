package at.ac.univie.dse2016.stream.boerse;

public final class UDPSession {
	public final Integer[] emittentIds;
	public java.util.Date lastConnectionTime;
	public final java.net.InetAddress address;
	public final int port;
	
	public UDPSession(Integer[] emittentIds, java.net.InetAddress address, int port) {
		this.emittentIds = emittentIds;
		this.address = address;
		this.port = port;
		this.lastConnectionTime = java.util.Calendar.getInstance().getTime();
	}
}
