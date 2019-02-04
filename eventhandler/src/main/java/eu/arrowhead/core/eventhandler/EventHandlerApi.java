package eu.arrowhead.core.eventhandler;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.EventFilter;
import eu.arrowhead.common.exception.DataNotFoundException;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("eventhandler/mgmt")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EventHandlerApi {

  private static final DatabaseManager dm = DatabaseManager.getInstance();

  @Path("subscriptions")
  public List<EventFilter> getAllEventSubscriptions() {
    return dm.getAll(EventFilter.class, null);
  }

  @Path("subscriptions/{id}")
  public Response getEventSubscriptionById(@PathParam("id") long id) {
    EventFilter subscription = dm.get(EventFilter.class, id)
                                 .orElseThrow(() -> new DataNotFoundException("EventFilter not found with id: " + id));
    return Response.ok().entity(subscription).build();
  }

}
