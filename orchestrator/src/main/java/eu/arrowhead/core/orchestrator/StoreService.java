/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.orchestrator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.OrchestrationStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class StoreService {


  private static final DatabaseManager dm = DatabaseManager.getInstance();
  private static final HashMap<String, Object> restrictionMap = new HashMap<>();
  private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

  /**
   * This method returns the active Orchestration Store entries for a consumer.
   */
  public static List<OrchestrationStore> getDefaultStoreEntries(ArrowheadSystem consumer) {
    restrictionMap.clear();
    ArrowheadSystem savedConsumer = getConsumerSystem(consumer);
    if (savedConsumer == null) {
      return new ArrayList<>();
    }

    restrictionMap.put("consumer", savedConsumer);
    restrictionMap.put("defaultEntry", true);
    return dm.getAll(OrchestrationStore.class, restrictionMap);
  }

  /**
   * This method returns a list of Orchestration Store entries specified by the consumer system and the requested service.
   */
  public static List<OrchestrationStore> getStoreEntries(ArrowheadSystem consumer, ArrowheadService service) {
    restrictionMap.clear();
    System.out.println("getStoreEntries");
    ArrowheadSystem savedConsumer = getConsumerSystem(consumer);
    ArrowheadService savedService = getRequestedService(service.getServiceDefinition());
    
    System.out.println("savedConsumer: \n"+gson.toJson(savedConsumer));
    System.out.println("savedService: \n"+gson.toJson(savedService));
    if (savedConsumer == null || savedService == null) {
      return new ArrayList<>();
    }

    if (!savedService.getInterfaces().isEmpty()) {
      if (!hasMatchingInterfaces(savedService, service)) {
        return new ArrayList<>();
      }
    }

    restrictionMap.put("consumer", savedConsumer);
    restrictionMap.put("service", savedService);
    return dm.getAll(OrchestrationStore.class, restrictionMap);
  }

  public static List<OrchestrationStore> getStoreEntries(ArrowheadService service) {
    restrictionMap.clear();
    ArrowheadService savedService = getRequestedService(service.getServiceDefinition());

    if (!savedService.getInterfaces().isEmpty()) {
      if (!hasMatchingInterfaces(savedService, service)) {
        return new ArrayList<>();
      }
    }

    restrictionMap.put("service", savedService);
    return dm.getAll(OrchestrationStore.class, restrictionMap);
  }

  /**
   * This private method returns an ArrowheadSystem from the database.
   */
  private static ArrowheadSystem getConsumerSystem(ArrowheadSystem system) {
    HashMap<String, Object> rm = new HashMap<>();
    rm.put("systemName", system.getSystemName());
    rm.put("address", system.getAddress());
    rm.put("port", system.getPort());
    return dm.get(ArrowheadSystem.class, rm);
  }

  /**
   * This private method returns an ArrowheadService from the database.
   */
  private static ArrowheadService getRequestedService(String serviceDefinition) {
    HashMap<String, Object> rm = new HashMap<>();
    rm.put("serviceDefinition", serviceDefinition);
    return dm.get(ArrowheadService.class, rm);
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private static boolean hasMatchingInterfaces(ArrowheadService savedService, ArrowheadService givenService) {
    if (givenService.getInterfaces().isEmpty()) {
      return savedService.getInterfaces().isEmpty();
    }
    for (String givenInterface : givenService.getInterfaces()) {
      for (String savedInterface : savedService.getInterfaces()) {
        if (givenInterface.equalsIgnoreCase(savedInterface)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * This method returns all the entries of the Orchestration Store.
   */
  @SuppressWarnings("unused")
  public static List<OrchestrationStore> getAllStoreEntries() {
    restrictionMap.clear();
    return dm.getAll(OrchestrationStore.class, restrictionMap);
  }

}
