package at.ac.univie.dse2016.stream.common;

public enum NetworkResource {
	UDP (0), RMI (1), SOAP (2), REST(3);
	
	private Integer ival;
	
	NetworkResource(Integer ival) { this.ival = ival; }
	
    public Integer getNumVal() { return ival; }
    
}
