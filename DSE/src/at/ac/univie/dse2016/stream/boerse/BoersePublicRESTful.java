package at.ac.univie.dse2016.stream.boerse;

import at.ac.univie.dse2016.stream.common.Emittent;

import java.rmi.RemoteException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

@Path("/public")
public class BoersePublicRESTful {
	
	private BoerseServer server;
	public BoersePublicRESTful(BoerseServer server) { this.server = server; }
	
	/**
	 * Status der Boerse, Open, Closed oder Error
	 */
	@GET
	@Path("/status")
	@Produces(MediaType.TEXT_HTML)
	public String getStatusHTML() throws RemoteException {
		return "<html><title>BoerseStatus</title><body><h1>BoerseStatus = " + server.getStatus() + "</h1></body></html>";
	}

	@GET
	@Path("/status")
	@Produces(MediaType.TEXT_PLAIN)
	public String getStatus() throws RemoteException {
		return server.getStatus().toString();
	}

	/**
	 * Get all Emitents
	 */
	@GET
	@Path("/emittents")
	@Produces(MediaType.TEXT_HTML)
	public String getEmittentsListHTML() throws RemoteException {
		StringBuilder sb = new StringBuilder("<html><title>BoerseStatus</title><body><ul>");
		for(Emittent e : server.getEmittentsList()) {
			sb.append("<li>");
			sb.append(e.getTicker());
			sb.append(" - ");
			sb.append(e.getName());
			sb.append("</li>");
		}
		sb.append("</ul></html>");
		return sb.toString();
	}
	
	/**
	 * Get all Emitents
	 */
	@GET
	@Path("/emittents")
	@Produces(MediaType.TEXT_PLAIN)
	public String getEmittentsList() throws RemoteException {
		StringBuilder sb = new StringBuilder();
		for(Emittent e : server.getEmittentsList()) {
			sb.append(e.getTicker());
			sb.append("; ");
		}
		return sb.substring(0, sb.length() - 2);
	}
	
	/**
	 * Normaleweise sollten Clients die Adresse ihrer Brokers kennen
	 * einfachheitshalber bekommt ein Client diese Adresse mithilfe dieser Methode, z.B. localhost:12001
	 */
	
	@GET
	@Path("/broker_network_address/{broker_id}")
	@Produces(MediaType.TEXT_HTML)
	public String getBrokerNetworkAddressHTML(@PathParam("broker_id") Integer brokerId) throws RemoteException {
		return "<html><title>broker_network_address</title><body><h1>broker_network_address = " + server.getBrokerNetworkAddress(brokerId) + "</h1></body></html>";
	}

	@GET
	@Path("/broker_network_address/{broker_id}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getBrokerNetworkAddress(@PathParam("broker_id") Integer brokerId) throws RemoteException {
		return server.getBrokerNetworkAddress(brokerId);
	}
}
