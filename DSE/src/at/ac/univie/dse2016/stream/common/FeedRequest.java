package at.ac.univie.dse2016.stream.common;

import java.io.*;;

public class FeedRequest implements Serializable {

	private static final long serialVersionUID = 100L;
	
	protected Integer sessionId;
	public Integer getSessionId() { return sessionId; }

	protected Integer status;
	public Integer getStatus() { return status; }

	protected Integer[] emittentIds;
	public Integer[] getEmittentIds() { return emittentIds; }


	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(sessionId);
		out.writeInt(status);
		out.writeObject(emittentIds);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		sessionId = in.readInt();
		status = in.readInt();
		emittentIds = (Integer[])in.readObject();
	}
	
	public FeedRequest(Integer sessionId, Integer status, Integer[] emittentIds) {
		this.sessionId = sessionId;
		this.status = status;
		this.emittentIds = emittentIds;
	}
}