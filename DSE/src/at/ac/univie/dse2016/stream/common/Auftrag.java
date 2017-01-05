package at.ac.univie.dse2016.stream.common;

import javax.xml.bind.annotation.*;

@XmlRootElement
public class Auftrag extends at.ac.univie.dse2016.stream.common.dao.PersistableObject implements Comparable<Auftrag>  {
	
	private static final long serialVersionUID = 100L;
		
	@XmlElement
	protected boolean kaufen;
	public boolean getKaufen() { return kaufen; }
	
	@XmlElement
	protected String ticker;
	public String getTicker() { return ticker; }
	
	@XmlElement
	protected Integer anzahl;
	public Integer getAnzahl() { return anzahl; }
	public void setAnzahl(Integer anzahl) { this.anzahl = anzahl; }
	
	@XmlElement
	protected float bedingung;
	public float getBedingung() { return bedingung; }
	
	@XmlElement
	protected AuftragStatus status;
	public AuftragStatus getStatus() { return status; }
	public void setStatus(AuftragStatus status) { this.status = status; }

	@XmlElement	
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
	
	public int compareTo(Auftrag compareAuftrag) {
		//ascending order
		return this.id - compareAuftrag.id;
	}
	
	public void setId(Integer id) {
		super.setId(id);
	}

}