/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.serviceregistry_sql;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.exception.DuplicateEntryException;
import eu.arrowhead.common.messages.ServiceQueryForm;
import eu.arrowhead.common.messages.ServiceQueryResult;
import java.util.HashMap;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;

@Path("serviceregistry")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ServiceRegistryResource {

  static final DatabaseManager dm = DatabaseManager.getInstance();

  private final HashMap<String, Object> restrictionMap = new HashMap<>();
  private static final Logger log = Logger.getLogger(ServiceRegistryResource.class.getName());
  private Gson gson = new GsonBuilder().setPrettyPrinting().create();
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "This is the Service Registry Arrowhead Core System.";
  }

  @POST
  @Path("register")
  public Response registerService(@Valid ServiceRegistryEntry entry) {
    System.out.println("\nRegister!!!\n"+gson.toJson(entry));
    entry.toDatabase();
    restrictionMap.put("serviceDefinition", entry.getProvidedService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      service = dm.save(entry.getProvidedService());
    } else {
      service.setInterfaces(entry.getProvidedService().getInterfaces());
      dm.merge(service);
    }
    entry.setProvidedService(service);

    restrictionMap.clear();
    restrictionMap.put("systemName", entry.getProvider().getSystemName());
    restrictionMap.put("address", entry.getProvider().getAddress());
    restrictionMap.put("port", entry.getProvider().getPort());
    ArrowheadSystem provider = dm.get(ArrowheadSystem.class, restrictionMap);
    if (provider == null) {
      provider = dm.save(entry.getProvider());
    } else {
      provider.setAuthenticationInfo(entry.getProvider().getAuthenticationInfo());
      dm.merge(provider);
    }
    entry.setProvider(provider);

    restrictionMap.clear();
    restrictionMap.put("provider", provider);
    restrictionMap.put("providedService", service);
    ServiceRegistryEntry savedEntry = dm.get(ServiceRegistryEntry.class, restrictionMap);
    if (savedEntry == null) {
      savedEntry = dm.save(entry);
    } else {
      throw new DuplicateEntryException(
          "There is already a Service Registry entry with this provider(" + provider.getSystemName() + ") and " + "providedService(" + service
              .getServiceDefinition() + ")");
    }

    savedEntry.fromDatabase(true);
    log.info("New " + entry.toString() + " is saved.");
    return Response.status(Status.CREATED).entity(savedEntry).build();
  }

  @PUT
  @Path("query")
  public Response queryRegistry(@Valid ServiceQueryForm queryForm) {
      
    System.out.println("\nquery!!!\n"+gson.toJson(queryForm));
    restrictionMap.put("serviceDefinition", queryForm.getService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      log.info("Service " + queryForm.getService().toString() + " is not in the registry.");
      return Response.status(Status.PARTIAL_CONTENT).entity(new ServiceQueryResult()).build();
    }

    restrictionMap.clear();
    restrictionMap.put("providedService", service);
    List<ServiceRegistryEntry> providedServices = dm.getAll(ServiceRegistryEntry.class, restrictionMap);
    for (ServiceRegistryEntry entry : providedServices) {
      entry.fromDatabase(true);
    }

    log.debug("Potential service providers before filtering:" + providedServices.size());
    RegistryUtils.filterOnInterfaces(providedServices, queryForm.getService());
    if (queryForm.getVersion() != null) {
      RegistryUtils.filterOnVersion(providedServices, queryForm.getVersion());
    } else {
      String minVersionValue = queryForm.getService().getServiceMetadata().get("minVersion");
      int minVersion = minVersionValue != null ? Integer.valueOf(minVersionValue) : 0;

      String maxVersionValue = queryForm.getService().getServiceMetadata().get("maxVersion");
      int maxVersion = maxVersionValue != null ? Integer.valueOf(maxVersionValue) : Integer.MAX_VALUE;

      if (minVersion > 0 || maxVersion < Integer.MAX_VALUE) {
        RegistryUtils.filterOnVersion(providedServices, minVersion, maxVersion);
      }
    }
    if (queryForm.isMetadataSearch()) {
      queryForm.getService().getServiceMetadata().remove("minVersion");
      queryForm.getService().getServiceMetadata().remove("maxVersion");
      RegistryUtils.filterOnMeta(providedServices, queryForm.getService().getServiceMetadata());
    }
    if (queryForm.isPingProviders()) {
      RegistryUtils.filterOnPing(providedServices);
    }
    log.debug("Potential service providers after filtering:" + providedServices.size());

    log.info("Service " + queryForm.getService().toString() + " queried successfully.");
    ServiceQueryResult result = new ServiceQueryResult(providedServices);
    return Response.status(Status.OK).entity(result).build();
  }

  @PUT
  @Path("remove")
  public Response removeService(@Valid ServiceRegistryEntry entry) {
      
    System.out.println("\nremove!!!\n"+gson.toJson(entry));
    restrictionMap.put("serviceDefinition", entry.getProvidedService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);

    restrictionMap.clear();
    restrictionMap.put("systemName", entry.getProvider().getSystemName());
    restrictionMap.put("address", entry.getProvider().getAddress());
    restrictionMap.put("port", entry.getProvider().getPort());
    ArrowheadSystem provider = dm.get(ArrowheadSystem.class, restrictionMap);

    restrictionMap.clear();
    restrictionMap.put("providedService", service);
    restrictionMap.put("provider", provider);
    ServiceRegistryEntry retrievedEntry = dm.get(ServiceRegistryEntry.class, restrictionMap);
    if (retrievedEntry != null) {
      dm.delete(retrievedEntry);
      retrievedEntry.fromDatabase(true);
      log.info(retrievedEntry.toString() + " deleted.");
      return Response.status(Status.OK).entity(retrievedEntry).build();
    } else {
      log.info(entry.toString() + " was not found in the SR to delete.");
      return Response.status(Status.NO_CONTENT).entity(entry).build();
    }
  }

}
