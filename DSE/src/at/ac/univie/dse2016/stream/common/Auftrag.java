package at.ac.univie.dse2016.stream.common;

import java.io.*;

public class Auftrag implements Serializable {
	
	private static final long serialVersionUID = 100L;
	
	protected Integer id;
	public Integer getId() { return id; }
	public void setId(Integer id) { this.id = id; }
	
	protected boolean kaufen;
	public boolean getKaufen() { return kaufen; }
	
	protected String ticker;
	public String getTicker() { return ticker; }
	
	protected Integer anzahl;
	public Integer getAnzahl() { return anzahl; }
	public void setAnzahl(Integer anzahl) { this.anzahl = anzahl; }
	
	protected float bedingung;
	public float getBedingung() { return bedingung; }
	
	protected AuftragStatus status;
	public AuftragStatus getStatus() { return status; }
	public void setStatus(AuftragStatus status) { this.status = status; }

	
	protected transient Integer ownerId;
	//BrokerId oder ClientId
	public Integer getOwnerId() { return ownerId; }


	/**
	 * Auftrag ohne Bedingung
	 */
	public Auftrag(boolean kaufen, String ticker, int anzahl) {
		this(-1, -1, kaufen, ticker, anzahl, -1);
	}

	/**
	 * Auftrag mit Bedingung
	 */
	public Auftrag(boolean kaufen, String ticker, int anzahl, float bedingung) {
		this(-1, -1, kaufen, ticker, anzahl, bedingung);
	}
	
	public Auftrag(int id, int ownerId, boolean kaufen, String ticker, int anzahl, float bedingung) {
		this.id = id;
		this.ownerId = ownerId;
		this.kaufen = kaufen;
		this.ticker = ticker;
		this.anzahl = anzahl;
		this.bedingung = bedingung;
		this.status = AuftragStatus.Init;
	}
}