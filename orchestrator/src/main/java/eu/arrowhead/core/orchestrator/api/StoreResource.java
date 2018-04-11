package eu.arrowhead.core.orchestrator.api;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.OrchestrationStore;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.security.SecurityUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;

@Path("orchestrator/store")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StoreResource {

  private final HashMap<String, Object> restrictionMap = new HashMap<>();
  private static final Logger log = Logger.getLogger(StoreResource.class);
  private static final DatabaseManager dm = DatabaseManager.getInstance();

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "orchestrator/store got it!";
  }

  @GET
  @Path("systemname/{systemName}")
  public List<OrchestrationStore> getDefaultEntriesForConsumer(@PathParam("systemName") String systemName) {
    restrictionMap.put("systemName", systemName);
    ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);

    restrictionMap.clear();
    restrictionMap.put("consumer", consumer);
    restrictionMap.put("defaultEntry", true);
    List<OrchestrationStore> store = dm.getAll(OrchestrationStore.class, restrictionMap);
    if (store.isEmpty()) {
      log.info("getDefaultEntriesForConsumer throws DataNotFoundException.");
      throw new DataNotFoundException("Default Orchestration Store entries were not found for consumer: " + systemName);
    }

    Collections.sort(store);
    log.info("getDefaultEntriesForConsumer returns with non-empty list");
    return store;
  }

  @GET
  @Path("systemname/{systemName}/servicedef/{serviceDef}")
  public List<OrchestrationStore> getStoreEntries(@PathParam("systemName") String systemName, @PathParam("serviceDef") String serviceDef) {
    restrictionMap.put("systemName", systemName);
    ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);

    restrictionMap.clear();
    restrictionMap.put("serviceDefinition", serviceDef);
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);

    restrictionMap.clear();
    restrictionMap.put("consumer", consumer);
    restrictionMap.put("service", service);
    List<OrchestrationStore> store = dm.getAll(OrchestrationStore.class, restrictionMap);
    if (store.isEmpty()) {
      log.info("getStoreEntries throws DataNotFoundException.");
      throw new DataNotFoundException("Orchestration Store entries were not found for this consumer/service pair.");
    }

    Collections.sort(store);
    log.info("getStoreEntries returns with non-empty list");
    return store;
  }

  @GET
  @Path("query")
  public List<OrchestrationStore> getEntriesForSecureConsumer(@Context ContainerRequestContext requestContext) {
    if (!requestContext.getSecurityContext().isSecure()) {
      log.error("getEntriesForSecureConsumer called in insecure mode");
      throw new ArrowheadException("This endpoint can only be called in secure mode!", Status.UNAUTHORIZED.getStatusCode());
    }
    String subjectName = requestContext.getSecurityContext().getUserPrincipal().getName();
    String clientCN = SecurityUtils.getCertCNFromSubject(subjectName);
    String[] clientFields = clientCN.split("\\.", 2);

    restrictionMap.put("systemName", clientFields[0]);
    ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);

    restrictionMap.clear();
    restrictionMap.put("consumer", consumer);
    List<OrchestrationStore> store = dm.getAll(OrchestrationStore.class, restrictionMap);
    if (store.isEmpty()) {
      log.info("getEntriesForSecureConsumer throws DataNotFoundException.");
      throw new DataNotFoundException("Orchestration Store entries were not found for consumer: " + clientFields[0]);
    }

    Collections.sort(store);
    log.info("getEntriesForSecureConsumer returns with non-empty list");
    return store;
  }

  @GET
  @Path("query/servicedef/{serviceDef}")
  public List<OrchestrationStore> getEntriesForSecureConsumer(@PathParam("serviceDef") String serviceDef,
                                                              @Context ContainerRequestContext requestContext) {
    if (!requestContext.getSecurityContext().isSecure()) {
      log.error("getEntriesForSecureConsumer called in insecure mode");
      throw new ArrowheadException("This endpoint can only be called in secure mode!", Status.UNAUTHORIZED.getStatusCode());
    }
    String subjectName = requestContext.getSecurityContext().getUserPrincipal().getName();
    String clientCN = SecurityUtils.getCertCNFromSubject(subjectName);
    String[] clientFields = clientCN.split("\\.", 2);

    restrictionMap.put("systemName", clientFields[0]);
    ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);

    restrictionMap.clear();
    restrictionMap.put("serviceDefinition", serviceDef);
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);

    restrictionMap.clear();
    restrictionMap.put("consumer", consumer);
    restrictionMap.put("service", service);
    List<OrchestrationStore> store = dm.getAll(OrchestrationStore.class, restrictionMap);
    if (store.isEmpty()) {
      log.info("getEntriesForSecureConsumer throws DataNotFoundException.");
      throw new DataNotFoundException("Orchestration Store entries were not found for this consumer/service pair");
    }

    Collections.sort(store);
    log.info("getEntriesForSecureConsumer returns with non-empty list");
    return store;
  }


}