package at.ac.univie.dse2016.stream.brokeradminapp;

import at.ac.univie.dse2016.stream.boerse.BoerseServer;
import at.ac.univie.dse2016.stream.broker.BrokerServer;
import at.ac.univie.dse2016.stream.common.Client;
import at.ac.univie.dse2016.stream.common.Emittent;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by rabizo on 25.11.16.
 */
@Path("/public")
public class BrokerPublicRESTful {

    private BrokerServer server;
    public BrokerPublicRESTful(BrokerServer server) { this.server = server; }

    /**
     * Einen neuen Client erzeugen
     * @param broker_id
     * @param name
     * @throws RemoteException
     */
    @POST
    @Path("/client/{name}/broker/{broker_id}")
    @Produces(MediaType.TEXT_HTML)
    public void clientAddNew(@PathParam ("broker_id") Integer broker_id, @PathParam("name")String name) throws RemoteException {
        Client client = new Client(broker_id, name);
        server.clientAddNew(client);
    }

    /**
     * Client aendern
     * @param broker_id
     * @param name
     * @throws RemoteException
     */
    @PUT
    @Path("/client/id/{id}/name/{name}/broker/{broker_id}/kontostand/{kontostand}")
    @Produces(MediaType.TEXT_HTML)
    public void clientEdit(@PathParam("id") Integer id,@PathParam ("broker_id") Integer broker_id,
                           @PathParam("name")String name, @PathParam("kontostand") float kontostand) throws RemoteException {
        Client client = new Client(id, broker_id, kontostand, name);
        server.clientEdit(client);
    }

    /**
     * Client blockieren. Brauch nur ID
     * @param id
     * @throws RemoteException
     */
    @POST
    @Path("/client/lock/id/{id}/")
    @Produces(MediaType.TEXT_HTML)
    public void clientEdit(@PathParam("id") Integer id) throws RemoteException {
        Client client = new Client(id, 0,0,"0");
        server.clientLock(client);
    }

    /**
     * Alle Clienten ausgeben
     * @return
     * @throws RemoteException
     */
    @GET
    @Path("/client/")
    @Produces(MediaType.TEXT_PLAIN)
    public List<Client> getClientsList() throws RemoteException {
        return server.getClientsList();
    }
}
