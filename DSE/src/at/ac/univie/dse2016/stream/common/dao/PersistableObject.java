package at.ac.univie.dse2016.stream.common.dao;

import java.io.Serializable;

/*
 * Base Class fuer Objecte, die wir speichern
 */
public class PersistableObject implements Serializable {
	
	private static final long serialVersionUID = 100L;
	
	protected Integer id;
	
	/*
	 * Constrictor for new Objects
	 */
	public PersistableObject() {
		this.id = -1;
	}
	
	/*
	 * Constrictor for existend Objects
	 */
	public PersistableObject(int id) {
		this.id = id;
	}
	
	public Integer getId() {
		return this.id;
	}

	protected void setId(Integer id) {
		this.id = id;
	}
}
