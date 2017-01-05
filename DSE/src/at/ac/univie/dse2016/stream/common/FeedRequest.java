package at.ac.univie.dse2016.stream.common;

import java.io.*;

public class FeedRequest implements Externalizable {

	private static final long serialVersionUID = 100L;
	
	protected Integer sessionId;
	public Integer getSessionId() { return sessionId; }

	protected Integer[] emittentIds;
	public Integer[] getEmittentIds() { return emittentIds; }


	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(sessionId);
		out.writeObject(emittentIds);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		sessionId = in.readInt();
		emittentIds = (Integer[])in.readObject();
	}
	
	public FeedRequest(Integer sessionId, Integer[] emittentIds) {
		this.sessionId = sessionId;
		this.emittentIds = emittentIds;
	}

	public FeedRequest(Integer[] emittentIds) {
		this(-1, emittentIds);
	}
	
	public FeedRequest() {
	}
}