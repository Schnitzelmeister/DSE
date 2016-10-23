package at.ac.univie.dse2016.stream;

import java.io.*;;

public class FeedMsg implements Externalizable {

	protected Integer id;
	public Integer getId() { return Math.abs(id); }
	public boolean getKaufen() { return (this.id < 0); }

	protected Integer tickerId;
	public Integer getTickerId() { return tickerId; }

	protected Integer anzahl;
	public Integer getAnzahl() { return anzahl; }

	protected AuftragStatus status;
	public AuftragStatus getStatus() { return status; }

	protected Integer counter;
	public Integer getCounter() { return counter; }

	protected float price;
	public float getPrice() { return price; }

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(counter);
		out.writeInt(id);
		out.writeInt(tickerId);
		out.writeInt(anzahl);
		out.writeInt(status.getNumVal()); 
		out.writeFloat(price);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		counter = in.readInt();
		id = in.readInt();
		tickerId = in.readInt();
		anzahl = in.readInt();
		status = AuftragStatus.values()[in.readInt() - 1];
		price = in.readFloat();
	}
	
	public FeedMsg(Integer counter, Integer id, Integer tickerId, boolean kaufen, Integer anzahl, float price, AuftragStatus status) {
		this.counter = counter;
		this.id = id * ((kaufen) ? -1 : 1);
		this.tickerId = tickerId;
		this.anzahl = anzahl;
		this.price = price;
		this.status = status;
	}
}
