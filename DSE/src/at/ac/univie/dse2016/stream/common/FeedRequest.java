package at.ac.univie.dse2016.stream.common;

import java.io.*;;

public class FeedRequest implements Serializable {

	private static final long serialVersionUID = 100L;
	
	protected Integer clientId;
	public Integer getClientId() { return clientId; }

	protected Integer status;
	public Integer getStatus() { return status; }

	protected String[] tickers;
	public String[] getTickers() { return tickers; }


	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(clientId);
		out.writeInt(status);
		out.writeObject(tickers);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		clientId = in.readInt();
		status = in.readInt();
		tickers = (String[])in.readObject();
	}
	
	public FeedRequest(Integer clientId, Integer status, String[] tickers) {
		this.clientId = clientId;
		this.status = status;
		this.tickers = tickers;
	}
}