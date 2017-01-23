package at.ac.univie.dse2016.stream.common.dao;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * Universal DAO kann nur Objekte, die PersistableObject implementieren, bearbeiten
 */
public class UniversalDAO <T extends PersistableObject> {
	
	private String source;
	private java.util.TreeMap<Integer, T> container;
	private AtomicInteger idGen;
	private boolean ignoreConflicts = false;

	public UniversalDAO(String source) throws IllegalArgumentException {
		this(source, false);
	}

	@SuppressWarnings("unchecked")
	public UniversalDAO(String source, boolean ignoreConflicts) throws IllegalArgumentException {
		this.ignoreConflicts = ignoreConflicts;
		this.idGen = new AtomicInteger();
		this.idGen.set(0);
		
		this.source = source;
		this.container = new java.util.TreeMap<Integer, T>();
		
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(this.source));
			this.container = (java.util.TreeMap<Integer, T>)ois.readObject();
			ois.close();
			
			if (this.container.size() > 0)
				this.idGen.set( this.container.lastKey() );
		}
		catch (FileNotFoundException e) {
	        return;
		}
	    catch(ClassNotFoundException e) {
			throw new IllegalArgumentException("Illegal DataSource");
		}
		catch (IOException e) {
			throw new IllegalArgumentException("IO Exception in DataSource");
		}
	}
	
	/**
	 * Get Copy of Container
	 */
	@SuppressWarnings("unchecked")
	public java.util.TreeMap<Integer, T> getItems() {
		java.util.TreeMap<Integer, T> ret = null;
		synchronized(this.container)
		{
			ret = (java.util.TreeMap<Integer, T>)container.clone();
		}
		return ret;
	}
	
	/**
	 * get Item by ItemId
	 */
	public T getItemById(int id) throws IllegalArgumentException {
		if ( !container.containsKey(id) )
			throw new IllegalArgumentException("Item with id=" + id + " doesn't exist");
		synchronized(this.container)
		{
			return container.get(id);
		}
	}
	
	/**
	 * Save Item in Container (new or exist)
	 */
	public void speichereItem(T item) throws IllegalArgumentException {
		
		//add new item, sonst edit
		if ( item.getId().equals(-1) ) {
			item.setId(this.idGen.incrementAndGet());
			synchronized(this.container)
			{
				this.container.put(item.getId(), item);
			}
		}
		else {
			if ( !ignoreConflicts && !container.containsKey(item.getId()) )
				throw new IllegalArgumentException("Item with id=" + String.valueOf( item.getId() ) + " doesn't exist");
			synchronized(this.container)
			{
				this.container.put(item.getId(), item);
			}
		}

		save();
	}
	
	/**
	 * Remove Item from Container
	 */
	public void loescheItem(T item) throws IllegalArgumentException {
		if ( !container.containsKey(item.getId()) )
			throw new IllegalArgumentException("Item with id=" + String.valueOf( item.getId() ) + " doesn't exist");
		synchronized(this.container)
		{
			this.container.remove(item.getId());
		}
		save();
	}
	
	
	/**
	 * Speichert Container in einem Datei
	 */
	private synchronized void save() throws IllegalArgumentException {
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(this.source));
			synchronized(this.container)
			{
				oos.writeObject(this.container);
			}
			oos.flush();
			oos.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new IllegalArgumentException("Serialization Exception occured");
		}
	}
	
	public boolean containsKey(Integer id) {
		return this.container.containsKey(id);
	}

}
