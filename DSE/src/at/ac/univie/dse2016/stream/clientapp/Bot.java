package at.ac.univie.dse2016.stream.clientapp;

/*
 * Base Class for Bot Implementation
 */
public abstract class Bot {
	protected ClientGUI gui;
	
	public abstract String getName();
	
	public void setClientGUI(ClientGUI gui) {
		this.gui = gui;
	}

	public ClientGUI getClientGUI() {
		return gui;
	}
	
	public abstract void InitValues();
	public abstract void Start();
	public abstract void Stop();
}
