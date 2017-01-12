package at.ac.univie.dse2016.stream.common;

import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Auftrag extends at.ac.univie.dse2016.stream.common.dao.PersistableObject implements Comparable<Auftrag>  {
	
	private static final long serialVersionUID = 100L;
		
	@XmlAttribute
	protected boolean kaufen;
	public boolean getKaufen() { return kaufen; }
	
	@XmlAttribute
	protected String ticker;
	public String getTicker() { return ticker; }
	
	@XmlAttribute
	protected Integer anzahl;
	public Integer getAnzahl() { return anzahl; }
	public void setAnzahl(Integer anzahl) { this.anzahl = anzahl; }
	
	@XmlAttribute
	protected float bedingung;
	public float getBedingung() { return bedingung; }
	
	@XmlAttribute
	protected AuftragStatus status;
	public AuftragStatus getStatus() { return status; }
	public void setStatus(AuftragStatus status) { this.status = status; }

	@XmlAttribute
	protected Integer ownerId;
	//BrokerId oder ClientId
	public Integer getOwnerId() { return ownerId; }


	/**
	 * Auftrag ohne Bedingung
	 */
	public Auftrag(int ownerId, boolean kaufen, String ticker, int anzahl) {
		this(-1, ownerId, kaufen, ticker, anzahl, -1);
	}

	/**
	 * Auftrag mit Bedingung
	 */
	public Auftrag(int ownerId, boolean kaufen, String ticker, int anzahl, float bedingung) {
		this(-1, ownerId, kaufen, ticker, anzahl, bedingung);
	}
	
	public Auftrag(int id, int ownerId, boolean kaufen, String ticker, int anzahl, float bedingung) {
		super(id);
		this.ownerId = ownerId;
		this.kaufen = kaufen;
		this.ticker = ticker;
		this.anzahl = anzahl;
		this.bedingung = bedingung;
		this.status = AuftragStatus.Init;
	}
	
	public Auftrag() {}
	
	public int compareTo(Auftrag compareAuftrag) {
		return this.id - compareAuftrag.id;
	}
	
	public void setId(Integer id) {
		super.setId(id);
	}

	public String toString() { 
	    return ((this.kaufen) ? "buy" : "sell")  + " " + this.anzahl + " " + this.ticker + ((this.bedingung > 0) ? " (price " + ((this.kaufen) ? "<= " : ">= ")  + this.bedingung + ")" : "");
	} 
}