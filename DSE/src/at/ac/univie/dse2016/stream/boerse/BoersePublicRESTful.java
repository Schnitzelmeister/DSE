package at.ac.univie.dse2016.stream.boerse;

import at.ac.univie.dse2016.stream.common.Emittent;
import at.ac.univie.dse2016.stream.common.NetworkResource;

import java.rmi.RemoteException;

import javax.jws.WebParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.FormParam;
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
			sb.append("<li><div>");
			sb.append(e.getTicker());
			sb.append(" - ");
			sb.append(e.getName());
			sb.append("<form action=\"edit_emittent\" method=\"post\"><input type=\"hidden\" name=\"id\" value=\""+ e.getId() +"\"><input type=\"hidden\" name=\"ticker\" value=\""+ e.getTicker() +"\">Name:<input type=\"text\" name=\"name\" value=\""+ e.getName() +"\"><input type=\"submit\" value=\"Update\"></form>"
					+ "<form action=\"lock_emittent\" method=\"post\"><input type=\"hidden\" name=\"id\" value=\""+ e.getId() +"\"><input type=\"hidden\" name=\"ticker\" value=\""+ e.getTicker() +"\"><input type=\"submit\" value=\"Remove\"></form>"
					+ "</div></li>");
		}
		sb.append("</ul><br><form action=\"add_new_emittent\" method=\"post\">Ticker: <input type=\"text\" name=\"ticker\"><br>Name: <input type=\"text\" name=\"name\"><br><input type=\"submit\" value=\"Add New\">"
		+ "</form></html>");
		return sb.toString();
	}
	
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
	private NetworkResource getNetworkResourceFromId(Integer id) {
		if (NetworkResource.UDP.getNumVal() == id.intValue())
			return NetworkResource.UDP;
		if (NetworkResource.SOAP.getNumVal() == id.intValue())
			return NetworkResource.SOAP;
		if (NetworkResource.REST.getNumVal() == id.intValue())
			return NetworkResource.REST;

		return NetworkResource.RMI;
	}
	
	@GET
	@Path("/broker_network_address/{broker_id}/{resourceKind}")
	@Produces(MediaType.TEXT_HTML)
	public String getBrokerNetworkAddressHTML(@PathParam("broker_id") Integer brokerId, @PathParam("resourceKind") Integer resourceKind) throws RemoteException {
		return "<html><title>broker_network_address</title><body><h1>broker_network_address (" + getNetworkResourceFromId(resourceKind).toString() + ") = " + server.getBrokerNetworkAddress(brokerId, getNetworkResourceFromId(resourceKind)) + "</h1></body></html>";
	}

	@GET
	@Path("/broker_network_address/{broker_id}/{resourceKind}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getBrokerNetworkAddress(@PathParam("broker_id") Integer brokerId, @PathParam("resourceKind") Integer resourceKind) throws RemoteException {
		return server.getBrokerNetworkAddress(brokerId,  getNetworkResourceFromId(resourceKind));
	}
	
	
	
	/**
	 * AddNew Emittent, Admin's Function
	 */
	@POST
    @Path("/add_new_emittent")
	@Produces(MediaType.TEXT_HTML)
	public String emittentAddNewHTML(@FormParam("ticker") String ticker, @FormParam("name") String name) throws RemoteException, IllegalArgumentException {
		try {
			server.emittentAddNew( new Emittent(ticker, name) );
			return getEmittentsListHTML();
		}
		catch (Exception e) {
			return e.getMessage();
		}
	}

	@POST
    @Path("/add_new_emittent")
	@Produces(MediaType.TEXT_PLAIN)
	public String emittentAddNew(@FormParam("ticker") String ticker, @FormParam("name") String name) throws RemoteException, IllegalArgumentException {
		StringBuilder sb = new StringBuilder();
		try {
			sb.append( "ok - " + server.emittentAddNew( new Emittent(ticker, name) ).toString() );
		}
		catch (Exception e) {
			sb.append( e.getMessage() );
		}
		return sb.toString(); 
	}

	/**
	 * Edit Emittent, Admin's Function
	 */
	//@PUT
	@POST
    @Path("/edit_emittent")
	@Produces(MediaType.TEXT_HTML)
	public String emittentEditHTML(@FormParam("id") Integer id, @FormParam("ticker") String ticker, @FormParam("name") String name) throws RemoteException, IllegalArgumentException {
		try {
			server.emittentEdit( new Emittent(id, ticker, name) );
			return getEmittentsListHTML();
		}
		catch (Exception e) {
			return e.getMessage();
		}
	}
	
	@PUT
    @Path("/edit_emittent")
	@Produces(MediaType.TEXT_PLAIN)
	public String emittentEdit(@FormParam("id") Integer id, @FormParam("ticker") String ticker, @FormParam("name") String name) throws RemoteException, IllegalArgumentException {
		try {
			server.emittentEdit( new Emittent(id, ticker, name) );
			return "ok";
		}
		catch (Exception e) {
			return e.getMessage();
		}
	}
	
	
	/**
	 * Remove Emittent, Admin's Function
	 */
	//@DELETE
	@POST
    @Path("/lock_emittent")
	@Produces(MediaType.TEXT_HTML)
	public String emittentLockHTML(@FormParam("id") Integer id, @FormParam("ticker") String ticker) throws RemoteException, IllegalArgumentException {
		try {
			server.emittentLock( new Emittent(id, ticker, "") );
			return getEmittentsListHTML();
		}
		catch (Exception e) {
			return e.getMessage();
		}
	}

	@DELETE
    @Path("/lock_emittent")
	@Produces(MediaType.TEXT_PLAIN)
	public String emittentLock(@FormParam("id") Integer id, @FormParam("ticker") String ticker) throws RemoteException, IllegalArgumentException {
		try {
			server.emittentLock( new Emittent(id, ticker, "") );
			return "ok";
		}
		catch (Exception e) {
			return e.getMessage();
		}
	}
}
