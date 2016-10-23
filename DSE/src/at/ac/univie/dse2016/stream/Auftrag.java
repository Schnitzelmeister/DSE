package at.ac.univie.dse2016.stream;


public class Auftrag {
	protected Integer id;
	public Integer getId() { return id; }
	
	protected boolean kaufen;
	public boolean getKaufen() { return kaufen; }
	
	protected String ticker;
	public String getTicker() { return ticker; }
	
	protected Integer anzahl;
	public Integer getAnzahl() { return anzahl; }
	
	protected float bedingung;
	public float getBedingung() { return bedingung; }
	
	protected AuftragStatus status;
	public AuftragStatus getStatus() { return status; }
	public void setStatus(AuftragStatus status) { this.status = status; }
	
	public Auftrag(int id, boolean kaufen, String ticker, int anzahl, float bedingung) {
		this.id = id;
		this.kaufen = kaufen;
		this.ticker = ticker;
		this.anzahl = anzahl;
		this.bedingung = bedingung;
		this.status = AuftragStatus.Init;
	}
}
