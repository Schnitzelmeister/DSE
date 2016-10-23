package at.ac.univie.dse2016.stream;

public class Credit {
	protected int clientId;
	public int getClientId() { return clientId; }
	protected int boersenMaklerId;
	public int getBoersenMaklerId() { return boersenMaklerId; }
	protected int geldgeberId;
	public int getGeldgeberId() { return geldgeberId; }
	
	protected float rate;
	public float getRate() { return rate; }
	protected float leverage;
	public float getLeverage() { return leverage; }
	
	protected float used;
	public float getUsed() { return used; }
	public void setUsed(float used) { this.used = used; }
	
	public Credit(int clientId, int boersenMaklerId, int geldgeberId, float rate, float leverage, float used) {
		this.clientId = clientId;
		this.boersenMaklerId = boersenMaklerId;
		this.geldgeberId = geldgeberId;
		this.rate = rate;
		this.leverage = leverage;
		this.used = used;
	}
}
