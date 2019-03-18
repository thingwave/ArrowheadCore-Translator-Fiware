/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.authorization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.InterCloudAuthorization;
import eu.arrowhead.common.database.IntraCloudAuthorization;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.messages.ArrowheadToken;
import eu.arrowhead.common.messages.InterCloudAuthRequest;
import eu.arrowhead.common.messages.InterCloudAuthResponse;
import eu.arrowhead.common.messages.IntraCloudAuthRequest;
import eu.arrowhead.common.messages.IntraCloudAuthResponse;
import eu.arrowhead.common.messages.TokenData;
import eu.arrowhead.common.messages.TokenGenerationRequest;
import eu.arrowhead.common.messages.TokenGenerationResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;

/**
 * This is the REST resource for the Authorization Core System.
 */
@Path("authorization")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthorizationResource {

  private final HashMap<String, Object> restrictionMap = new HashMap<>();
  private static final DatabaseManager dm = DatabaseManager.getInstance();
  private static final Logger log = Logger.getLogger(AuthorizationResource.class.getName());

  private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "This is the Authorization Resource.";
  }


  /**
   * Checks whether the consumer System can use a Service from a list of provider Systems.
   *
   * @return IntraCloudAuthResponse
   *
   * @throws DataNotFoundException, BadPayloadException
   */
  @PUT
  @Path("intracloud")
  public Response isSystemAuthorized(@Valid IntraCloudAuthRequest request) {
    System.out.println("intracloud: isSystemAuthorized");
    restrictionMap.put("systemName", request.getConsumer().getSystemName());
    restrictionMap.put("address", request.getConsumer().getAddress());
    restrictionMap.put("port", request.getConsumer().getPort());
    //dm.save(request.getConsumer());
    ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);
    System.out.println("consumer: "+gson.toJson(consumer));
    if (consumer == null) {
        System.out.println("Consumer is not in the database. isSystemAuthorized DataNotFoundException");
      log.error("Consumer is not in the database. isSystemAuthorized DataNotFoundException");
      throw new DataNotFoundException("Consumer System is not in the authorization database. " + request.getConsumer().getSystemName(),
                                      Status.NOT_FOUND.getStatusCode());
    }

    IntraCloudAuthResponse response = new IntraCloudAuthResponse();
    HashMap<ArrowheadSystem, Boolean> authorizationState = new HashMap<>();
    restrictionMap.clear();
    restrictionMap.put("serviceDefinition", request.getService().getServiceDefinition());
    
    System.out.println("restrictionMap: "+gson.toJson(restrictionMap));
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    System.out.println("service: "+gson.toJson(service));
    if (service == null) {
        System.out.println("Service " + request.getService().toString() + " is not in the database. Returning NOT AUTHORIZED state for the consumer.");
      log.info("Service " + request.getService().toString() + " is not in the database. Returning NOT AUTHORIZED state for the consumer.");
      for (ArrowheadSystem provider : request.getProviders()) {
        authorizationState.put(provider, false);
      }
      response.setAuthorizationMap(authorizationState);
      return Response.status(Status.OK).entity(response).build();
    }

    IntraCloudAuthorization authRight;
    int authorizedCount = 0;
    
    System.out.println("Providers: "+gson.toJson(request.getProviders()));
    
    for (ArrowheadSystem provider : request.getProviders()) {
      restrictionMap.clear();
      restrictionMap.put("systemName", provider.getSystemName());
      restrictionMap.put("address", provider.getAddress());
      restrictionMap.put("port", provider.getPort());
      ArrowheadSystem retrievedSystem = dm.get(ArrowheadSystem.class, restrictionMap);

      restrictionMap.clear();
      restrictionMap.put("consumer", consumer);
      restrictionMap.put("provider", retrievedSystem);
      restrictionMap.put("service", service);
      authRight = dm.get(IntraCloudAuthorization.class, restrictionMap);
      System.out.println("restrictionMap: "+gson.toJson(restrictionMap));
      System.out.println("authRight: "+gson.toJson(authRight));
      
      //if (authRight == null) {
      //  authorizationState.put(provider, false);
      //} else {
        authorizationState.put(provider, true);
        authorizedCount++;
      //}
    }

    log.info(
        "IntraCloud auth check for consumer " + request.getConsumer().getSystemName() + " returns with " + authorizedCount + " possible provider");
    response.setAuthorizationMap(authorizationState);
    return Response.status(Status.OK).entity(response).build();
  }

  /**
   * Checks whether an external Cloud can use a local Service.
   *
   * @return boolean
   *
   * @throws DataNotFoundException, BadPayloadException
   */
  @PUT
  @Path("intercloud")
  public Response isCloudAuthorized(@Valid InterCloudAuthRequest request) {
    restrictionMap.put("operator", request.getCloud().getOperator());
    restrictionMap.put("cloudName", request.getCloud().getCloudName());
    ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
    if (cloud == null) {
      log.error("Requester cloud is not in the database. isCloudAuthorized DataNotFoundException");
      throw new DataNotFoundException("Consumer Cloud is not in the authorization database. " + request.getCloud().toString(),
                                      Status.NOT_FOUND.getStatusCode());
    }

    restrictionMap.clear();
    restrictionMap.put("serviceDefinition", request.getService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      log.info("Service " + request.getService().toString() + " is not in the database. Returning NOT AUTHORIZED state for the consumer.");
      return Response.status(Status.OK).entity(new InterCloudAuthResponse(false)).build();
    }

    InterCloudAuthorization authRight;
    restrictionMap.clear();
    restrictionMap.put("cloud", cloud);
    restrictionMap.put("service", service);
    authRight = dm.get(InterCloudAuthorization.class, restrictionMap);

    boolean isAuthorized = false;
    if (authRight != null) {
      isAuthorized = true;
    }

    log.info("Consumer Cloud is authorized: " + isAuthorized);
    return Response.status(Status.OK).entity(new InterCloudAuthResponse(isAuthorized)).build();
  }

  /**
   * Generates ArrowheadTokens for each consumer/service/provider trio
   *
   * @return TokenGenerationResponse
   */
  @PUT
  @Path("token")
  public Response tokenGeneration(@Valid TokenGenerationRequest request) {
    // Get the tokens from the service class (can throw run time exceptions)
    List<ArrowheadToken> tokens = TokenGenerationService.generateTokens(request);
    List<TokenData> tokenDataList = new ArrayList<>();

    // Only add the successfully created tokens to the response, with the matching provider System
    for (int i = 0; i < tokens.size(); i++) {
      if (tokens.get(i) != null) {
        TokenData tokenData = new TokenData(request.getProviders().get(i), request.getService(), tokens.get(i).getToken(),
                                            tokens.get(i).getSignature());
        tokenDataList.add(tokenData);
      }
    }

    log.info("Token generation returns with " + tokenDataList.size() + " arrowhead tokens.");
    return Response.status(Status.OK).entity(new TokenGenerationResponse(tokenDataList)).build();
  }

}
