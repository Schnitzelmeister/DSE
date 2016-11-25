package at.ac.univie.dse2016.stream.common;

public enum AuftragStatus {
	Init (0), Accepted (1), Bearbeitet (2), TeilweiseBearbeitet(3), Canceled (4);
	
	private Integer ival;
	
	AuftragStatus(Integer ival) { this.ival = ival; }
	
    public Integer getNumVal() { return ival; }
    
    /*
    public static AuftragStatus forInt(int ival) {
        for (AuftragStatus v : values()) {
            if (v.ival == ival)
                return v;
        }
        throw new IllegalArgumentException("Invalid AuftragStatus: " + String.valueOf(ival));
    }*/
}