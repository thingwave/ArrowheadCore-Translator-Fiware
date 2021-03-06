package eu.arrowhead.core.fiware;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.messages.ServiceRequestForm;
import eu.arrowhead.core.fiware.arrowhead.ArrowheadEntity;
import eu.arrowhead.core.fiware.arrowhead.OrchestrationResponse;
import eu.arrowhead.core.fiware.arrowhead.OrchestrationService;
import eu.arrowhead.core.fiware.client.FiwareClient;
import eu.arrowhead.core.fiware.common.FiwareTools;
import eu.arrowhead.core.fiware.common.SenML;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.DELETE;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.HttpHeaders;
import org.apache.http.HttpEntityEnclosingRequest;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;


/**
 *
 * @author Pablo Puñal Pereira <pablo.punal@thingwave.eu>
 */

/**
 * REST resource for the Fiware Plugin.
 */

@Path("v2")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class FiwareResource implements Observer{
    
    private final GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping();
    private final Gson gson = gsonBuilder.create();
    private final Gson pretyGson = gsonBuilder.setPrettyPrinting().create();
    private final FiwareClient fiwareClient;
    private final CloseableHttpClient httpClient;
    
    private final ArrayList<ArrowheadEntity> arrowheadEntities;
    
    public FiwareResource() {
        System.out.println("FIWARE Resource start!");
        fiwareClient = new FiwareClient(getPropFiwareURL());
        arrowheadEntities = new ArrayList<>();
        //httpClient = HttpClients.createDefault();
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 1000);
        httpClient = new DefaultHttpClient(httpParams);
        
        ScheduledExecutorService sesAHregistration = Executors.newSingleThreadScheduledExecutor();
        System.out.println("Starting Auto-registration...");
        sesAHregistration.scheduleAtFixedRate(
            () -> {
                System.out.println("--- Scheduled :: Update AH FIWARE registration ---");
                registerAllEntities();
            }, 5, 60, TimeUnit.SECONDS);
        
        // Adding Hook to auto-unregister the service
        Runtime.getRuntime().addShutdownHook(
                new Thread() {
                    @Override
                    public void run() {
                        unregisterAllEntities();
                    }
                });
    }
    
    /* ------------------------------ Load Prop ----------------------------- */
    private String getPropFiwareURL() {
        Properties defaults = new Properties();
        defaults.setProperty("fiware_url", "http://0.0.0.0:1026");
        Properties prop = new Properties(defaults);
        InputStream input = null;
        
        try {
            input = new FileInputStream("config/default.conf");
            prop.load(input);
        } catch (IOException ex) {
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }        
        return prop.getProperty("fiware_url");
    }
    
    private ArrowheadEntity findArrowheadEntity(String id) {
        for (ArrowheadEntity ae :arrowheadEntities){
            if (ae.getId().equals(id))
                return ae;
        }
        return null;
    }
    
    private void addArrowheadEntity(ArrowheadEntity ae) {
        if (findArrowheadEntity(ae.getId()) == null) {
            arrowheadEntities.add(ae);
        }
    }
    
    private String requestHttpService(String url) {
        try {
            HttpGet get = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(get);
            
            
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            response.getEntity().getContent()
                    )
            );
            String line;
            StringBuilder sb = new StringBuilder();
            while((line = br.readLine()) != null) {
                sb.append(line);
            }
            get.releaseConnection();
            return sb.toString();
            
        } catch (MalformedURLException ex) {
        } catch (IOException ex) {
        }
        return "ERROR";
    }
    
    private SenML requestServiceSenML(String url) {
        return new SenML().fromJSON(requestHttpService(url));
    }
    
    /* ------------------------------ ARROWHEAD ----------------------------- */
    private ServiceRegistryEntry fiware2ArrowheadEntry(JsonObject entity) {
        if (!entity.has("id") || !entity.has("type")) return null;
        String serviceDef = "FIWARE-"+entity.get("type").getAsString();
        String serviceUri = "/plugin/service/"+entity.get("id").getAsString()+"/"+entity.get("type").getAsString();
        String interfaceList = "FIWARE";
        Set<String> interfaces = new HashSet<>();
        if (interfaceList != null && !interfaceList.isEmpty()) {
            //Interfaces are read from a comma separated list
            interfaces.addAll(Arrays.asList(interfaceList.replaceAll("\\s+", "").split(",")));
        }
        Map<String, String> metadata = new HashMap<>();
        String metadataString = "unit-celsius";
        if (metadataString != null && !metadataString.isEmpty()) {
            //Metadata in the properties file: key1-value1, key2-value2, ...
            String[] parts = metadataString.split(",");
            for (String part : parts) {
                String[] pair = part.split("-");
                metadata.put(pair[0], pair[1]);
            }
        }
        ArrowheadService service = new ArrowheadService(serviceDef, interfaces, metadata);
        String insecProviderName = entity.get("id").getAsString()+"-"+entity.get("type").getAsString();
        ArrowheadSystem provider = new ArrowheadSystem(insecProviderName, "0.0.0.0", 8462, null);
        
        return new ServiceRegistryEntry(service, provider, serviceUri);
    }
    
    
    private void registerAllEntities() {
        try {
            ArrayList<JsonObject> entities = fiwareClient.listEntities(new JsonObject());
            
            for (int i=0; i< entities.size(); i++) {
                JsonObject entity = entities.get(i);
                System.out.printf("%3d/%d :: %80s | %20s ", (i+1), entities.size(), entity.get("id").getAsString(), entity.get("type").getAsString());
                //System.out.print("["+(i+1)+"/"+entities.size()+"] "+entity.get("id")+" | "+entity.get("type")+" ");
                try {
                    ServiceRegistryEntry entry = fiware2ArrowheadEntry(entity);
                    
                    int status = registerEntry(entry);
                    
                    if (status != 201) {
                        status = unregisterEntry(entry);
                        status = registerEntry(entry);
                        if (status == 201) {
                            System.out.println("[REGISTERED]");
                        } else {
                            System.out.println("[NOT REGISTERED]");
                        }
                    } else {
                        System.out.println("[REGISTERED]");
                        //System.out.println("Service ["+insecProviderName+"] registered on Arrowhead!");
                    }
                } catch (IOException ex) {
                    System.out.println("IOException!: "+ex.getLocalizedMessage());
                }
            }
            
        } catch (IOException ex) {
            System.out.println("IOException!: "+ex.getLocalizedMessage());
        } catch (URISyntaxException ex) {
            Logger.getLogger(FiwareResource.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void unregisterAllEntities() {
        try {
            ArrayList<JsonObject> entities = fiwareClient.listEntities(new JsonObject());
            
            for (int i=0; i< entities.size(); i++) {
                JsonObject entity = entities.get(i);
                System.out.printf("%3d/%d :: %80s | %20s ", (i+1), entities.size(), entity.get("id").getAsString(), entity.get("type").getAsString());
                //System.out.print("["+(i+1)+"/"+entities.size()+"] "+entity.get("id")+" | "+entity.get("type")+" ");
                try {
                    ServiceRegistryEntry entry = fiware2ArrowheadEntry(entity);
                    unregisterEntry(entry);
                    
                } catch (IOException ex) {
                    System.out.println("IOException!: "+ex.getLocalizedMessage());
                }
            }
            
        } catch (IOException ex) {
            System.out.println("IOException!: "+ex.getLocalizedMessage());
        } catch (URISyntaxException ex) {
            Logger.getLogger(FiwareResource.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private int registerEntry(ServiceRegistryEntry entry) throws UnsupportedEncodingException, IOException {
        HttpPost post = new HttpPost("http://0.0.0.0:8442/serviceregistry/register");
        // Header
        post.setHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, "application/json");
        
        
        System.out.println("registerEntry: "+gson.toJson(entry));
        // Content
        StringEntity entity = new StringEntity(gson.toJson(entry));
        post.setEntity(entity);
        int status = httpClient.execute(post).getStatusLine().getStatusCode();
        post.releaseConnection();
        return status;
    }
    
    private int unregisterEntry(ServiceRegistryEntry entry) throws UnsupportedEncodingException, IOException {
        HttpPut put = new HttpPut("http://0.0.0.0:8442/serviceregistry/remove");
        // Header
        put.setHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, "application/json");
        
        //System.out.println("unregisterEntry: "+gson.toJson(entry));
        // Content
        StringEntity entity = new StringEntity(gson.toJson(entry));
        put.setEntity(entity);
        int status = httpClient.execute(put).getStatusLine().getStatusCode();
        put.releaseConnection();
        return status;
    }
    
    private CloseableHttpResponse httpRequest(HttpUriRequest request) throws IOException {
        request.setHeader(CONTENT_TYPE, "application/json");
        HttpEntityEnclosingRequest req = (HttpEntityEnclosingRequest) request;
        CloseableHttpResponse response = httpClient.execute(request);
        return response;
    }
    
    private CloseableHttpResponse orchestrationAH(ServiceRequestForm serviceRequestForm) {
        try {
            HttpPost post = new HttpPost("http://0.0.0.0:8440/orchestrator/orchestration");
            post.setEntity(new StringEntity(gson.toJson(serviceRequestForm)));
            CloseableHttpResponse response = httpRequest(post);
            post.releaseConnection();
            return response;
        } catch (IOException ex) {}
        return null;
    }
    
    private OrchestrationResponse orchestration(ServiceRequestForm serviceRequestForm) {
  
        CloseableHttpResponse response = orchestrationAH(serviceRequestForm);
        
        if (response.getEntity() == null) return new OrchestrationResponse();
        
        try {
            return gson.fromJson(
                    new BufferedReader(
                            new InputStreamReader(
                                    response.getEntity().getContent())),
                    OrchestrationResponse.class);
        } catch (IOException ex) {
            //LOG.warn("IOException: "+ex.getLocalizedMessage());
            return new OrchestrationResponse();
        } catch (UnsupportedOperationException ex) {
            //LOG.warn("UnsupportedOperationException: "+ex.getLocalizedMessage());
            return new OrchestrationResponse();
        }
    }
    
    
    
    
    @GET
    public String getIt() {
        JsonObject jresp = new JsonObject();
        jresp.addProperty("entities_url", "/v2/entities");
        jresp.addProperty("types_url", "/v2/types");
        jresp.addProperty("subscriptions_url", "/v2/subscriptions");
        jresp.addProperty("registrations_url", "/v2/registrations");
        return pretyGson.toJson(jresp);
    }

    /* ------------------------------ ENTITIES ------------------------------ */
    @GET
    @Path("entities")
    public String listEntities(
            @QueryParam("id") String id,
            @QueryParam("type") String type,
            @QueryParam("idPattern") String idPattern,
            @QueryParam("typePattern") String typePattern,
            @QueryParam("q") String q,
            @QueryParam("mq") String mq,
            @QueryParam("georel") String georel,
            @QueryParam("geometry") String geometry,
            @QueryParam("coords") String coords,
            @QueryParam("limit") int limit,
            @QueryParam("offset") int offset,
            @QueryParam("attrs") String attrs,
            @QueryParam("metadata") String metadata,
            @QueryParam("orderBy") String orderBy,
            @QueryParam("options") String options) {
        //System.out.println("listEntities");
        JsonObject jQueryParams = new JsonObject();
        if (id != null) jQueryParams.addProperty("id", id);
        if (type != null) jQueryParams.addProperty("type", type);
        if (idPattern != null) jQueryParams.addProperty("idPattern", idPattern);
        if (typePattern != null) jQueryParams.addProperty("typePattern", typePattern);
        if (q != null) jQueryParams.addProperty("q", q);
        if (mq != null) jQueryParams.addProperty("mq", mq);
        if (georel != null) jQueryParams.addProperty("georel", georel);
        if (geometry != null) jQueryParams.addProperty("geometry", geometry);
        if (coords != null) jQueryParams.addProperty("coords", coords);
        if (limit != 0) jQueryParams.addProperty("limit", limit);
        if (offset != 0) jQueryParams.addProperty("offset", offset);
        if (attrs != null) jQueryParams.addProperty("attrs", attrs);
        if (metadata != null) jQueryParams.addProperty("metadata", metadata);
        if (orderBy != null) jQueryParams.addProperty("orderBy", orderBy);
        if (options!= null) jQueryParams.addProperty("options", options);
        
        ArrayList<JsonObject> entities = null;
        
        //System.out.println("Request All entities: "+pretyGson.toJson(jQueryParams));
        while(entities == null) {
            try {
                entities = fiwareClient.listEntities(jQueryParams);
                //System.out.println(gson.toJson(entities));
            } catch (Exception ex) {
                System.out.println("Exception: "+ex.getLocalizedMessage());
                if (ex.getLocalizedMessage().equals("Socket closed")) {
                    
                } else {
                    entities = new ArrayList<>();
                }
                
            }
        }
        
        // Check Arrowhead Services
            ServiceRequestForm srf = gson.fromJson("{\"requesterSystem\":{\"systemName\":\"demo_Temperature_Consumer\",\"address\":\"0.0.0.0\",\"port\":8888,\"authenticationInfo\":\"null\"},\"requestedService\":{\"serviceDefinition\":\""+type+"\",\"interfaces\":[\"SenML\"],\"serviceMetadata\":{\"unit\":\"celsius\"}},\"orchestrationFlags\":{\"onlyPreferred\":false,\"overrideStore\":true,\"externalServiceRequest\":false,\"enableInterCloud\":true,\"enableQoS\":false,\"matchmaking\":false,\"metadataSearch\":true,\"triggerInterCloud\":false,\"pingProviders\":false},\"preferredProviders\":[],\"requestedQoS\":{},\"commands\":{}}", ServiceRequestForm.class);
            OrchestrationResponse or = orchestration(srf);
            
            for (OrchestrationService os: or.getAllServices()) {
                JsonObject newEntity = new JsonObject();
                newEntity.addProperty("id", os.getProvider().getSystemName());
                newEntity.addProperty("type", type);
                newEntity.addProperty("arrowheadSystem", true);
                entities.add(newEntity);
                try {
                    // Save entity on the list
                    addArrowheadEntity(new ArrowheadEntity(
                            os.getProvider().getSystemName(),
                            os.getServiceURL().toString())
                    );

                } catch (MalformedURLException ex) {}
            }
            
            System.out.println(pretyGson.toJson(or));
        
        System.out.println("GET entities - Transparent request/response");
        System.out.println(pretyGson.toJson(entities));
        
        return pretyGson.toJson(entities);
    }
    
    @POST
    @Path("entities")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createEntity(
            @QueryParam("options") String options,
            String content,
            @Context HttpHeaders headers) {
        //System.out.println("createEntity");
        String contentType = headers.getHeaderString("Content-type"); 
        JsonObject jQueryParams = new JsonObject();
        if (options != null) jQueryParams.addProperty("options", options);
        try {
            // Register Entity in Arrowhead
            registerEntry(fiware2ArrowheadEntry(new JsonParser().parse(content).getAsJsonObject()));
        } catch (IOException ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        try {
            return Response.status(fiwareClient.createEntity(jQueryParams,contentType, content)).build();
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        return Response.status(0).build();
    }

    @GET
    @Path("entities/{entityId}")
    public String retrieveEntity(
            @PathParam("entityId") String entityId,
            @QueryParam("type") String type,
            @QueryParam("attrs") String attrs,
            @QueryParam("metadata") String metadata,
            @QueryParam("options") String options,
            @Context UriInfo uriInfo
            ) throws UnsupportedEncodingException, URISyntaxException {
        //System.out.println("retrieveEntity");
        
        JsonObject jresp = null;
        
        try {
            jresp = fiwareClient.retrieveEntity(entityId, FiwareTools.parseQueryParams(uriInfo.getRequestUri().getQuery()));
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        return pretyGson.toJson(jresp);
    }
    
    @GET
    @Path("entities/{entityId}/attrs")
    @Consumes(MediaType.APPLICATION_JSON)
    public String retrieveEntityAttributes(
            String content,
            @PathParam("entityId") String entityId,
            @QueryParam("type") String type,
            @QueryParam("attrs") String attrs,
            @QueryParam("metadata") String metadata,
            @QueryParam("options") String options,
            @Context UriInfo uriInfo) {
        //System.out.println("retrieveEntityAttributes");
        
        JsonObject jresp = null;
        
        try {
            jresp = fiwareClient.retrieveEntityAttributes(entityId, FiwareTools.parseQueryParams(uriInfo.getRequestUri().getQuery()));
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        return pretyGson.toJson(jresp);
    }
    
    @POST
    @Path("entities/{entityId}/attrs")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateAppendEntityAttributes(
            @PathParam("entityId") String entityId,
            @QueryParam("type") String type,
            @QueryParam("options") String options,
            String content,
            @Context HttpHeaders headers) {
        //System.out.println("updateAppendEntityAttributes");
        String contentType = headers.getHeaderString("Content-type");
        JsonObject jQueryParams = new JsonObject();
        if (type != null) jQueryParams.addProperty("type", type);
        if (type != null) jQueryParams.addProperty("options", options);
        
        try {
            return Response.status(fiwareClient.updateAppendEntityAttributes(entityId, jQueryParams, contentType, content)).build();
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        return Response.status(0).build();
    }
    
    @PATCH
    @Path("entities/{entityId}/attrs")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateExistingEntityAttributes(
            String content,
            @PathParam("entityId") String entityId,
            @QueryParam("type") String type,
            @QueryParam("options") String options) {
        //System.out.println("updateExistingEntityAttributes");
        JsonObject jQueryParams = new JsonObject();
        if (type != null) jQueryParams.addProperty("type", type);
        if (type != null) jQueryParams.addProperty("options", options);
        JsonObject jContent = new JsonParser().parse(content).getAsJsonObject();
                
        try {
            return Response.status(fiwareClient.updateExistingEntityAttributes(entityId, jQueryParams, jContent)).build();
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        return Response.status(0).build();
    }
    
    @PUT
    @Path("entities/{entityId}/attrs")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response replaceAllEntityAttributes(
            String content,
            @Context HttpHeaders headers,
            @PathParam("entityId") String entityId,
            @QueryParam("type") String type,
            @QueryParam("options") String options) {
        //System.out.println("replaceAllEntityAttributes");
        String contentType = headers.getHeaderString("Content-type");
        JsonObject jQueryParams = new JsonObject();
        if (type != null) jQueryParams.addProperty("type", type);
        if (type != null) jQueryParams.addProperty("options", options);
                
        try {
            return Response.status(fiwareClient.replaceAllEntityAttributes(entityId, jQueryParams, contentType, content)).build();
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        return Response.status(0).build();
    }
    
    @DELETE
    @Path("entities/{entityId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeEntity(
            @PathParam("entityId") String entityId,
            @QueryParam("type") String type) {
        System.out.println("removeEntity");
        JsonObject jQueryParams = new JsonObject();
        if (type != null) jQueryParams.addProperty("type", type);
        
        
        try {
            // Remove from Arrowhead
            JsonObject entity = new JsonObject();
            entity.addProperty("id", entityId);
            entity.addProperty("type", type);
            System.out.println("unregister AH: "+pretyGson.toJson(entity));
            unregisterEntry(fiware2ArrowheadEntry(entity));
        } catch (IOException ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        
        try {
                        
            return Response.status(fiwareClient.removeEntity(entityId, jQueryParams)).build();
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        return Response.status(0).build();
    }
        
    /* ----------------------------- ATTRIBUTES ----------------------------- */
    
    @GET
    @Path("entities/{entityId}/attrs/{attrName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public String getAttributeData(
            String content,
            @PathParam("entityId") String entityId,
            @PathParam("attrName") String attrName,
            @QueryParam("type") String type,
            @QueryParam("metadata") String metadata) {
        //System.out.println("getAttributeData");
        JsonObject jQueryParams = new JsonObject();
        if (type != null) jQueryParams.addProperty("type", type);
        if (metadata != null) jQueryParams.addProperty("metadata", metadata);
        
        JsonObject jresp = null;
        try {
            jresp = fiwareClient.getAttributeData(entityId, attrName, jQueryParams);
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        return pretyGson.toJson(jresp);
    }
    
    @PUT
    @Path("entities/{entityId}/attrs/{attrName}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response updateAttributeData(
            String content,
            @Context HttpHeaders headers,
            @PathParam("entityId") String entityId,
            @PathParam("attrName") String attrName,
            @QueryParam("type") String type) {
        //System.out.println("updateAttributeData");
        String contentType = headers.getHeaderString("Content-type");
        JsonObject jQueryParams = new JsonObject();
        if (type != null) jQueryParams.addProperty("type", type);
                
        try {
            return Response.status(fiwareClient.updateAttributeData(entityId, attrName, jQueryParams, contentType, content)).build();
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        return Response.status(0).build();
    }
    
    @DELETE
    @Path("entities/{entityId}/attrs/{attrName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeASingleAttribute(
            @PathParam("entityId") String entityId,
            @PathParam("attrName") String attrName,
            @QueryParam("type") String type) {
        //System.out.println("removeASingleAttribute");
        JsonObject jQueryParams = new JsonObject();
        if (type != null) jQueryParams.addProperty("type", type);
                
        try {
            return Response.status(fiwareClient.removeASingleAttribute(entityId, attrName, jQueryParams)).build();
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        return Response.status(0).build();
    }
    
    /* --------------------------- ATTRIBUTE VALUE -------------------------- */
    
    @GET
    @Path("entities/{entityId}/attrs/{attrName}/value")
    @Consumes(MediaType.APPLICATION_JSON)
    public String getAttributeValue(
            String content,
            @PathParam("entityId") String entityId,
            @PathParam("attrName") String attrName,
            @QueryParam("type") String type) {
                
        ArrowheadEntity ae;
        if ((ae = findArrowheadEntity(entityId)) != null) {
            System.out.println("URL: "+ae.getURL());
            SenML senML = requestServiceSenML(ae.getURL());
            System.out.println("senML: "+pretyGson.toJson(senML));
            
            return ""+requestServiceSenML(ae.getURL()).getLastest().getValue().doubleValue();
        }
        
        JsonObject jQueryParams = new JsonObject();
        if (type != null) jQueryParams.addProperty("type", type);
        try {
            return fiwareClient.getAttributeValue(entityId, attrName, jQueryParams);
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        return null;
    }
    
    @PUT
    @Path("entities/{entityId}/attrs/{attrName}/value")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response updateAttributeValue(
            String content,
            @Context HttpHeaders headers,
            @PathParam("entityId") String entityId,
            @PathParam("attrName") String attrName,
            @QueryParam("type") String type) {
        //System.out.println("updateAttributeValue");
        String contentType = headers.getHeaderString("Content-type");
        JsonObject jQueryParams = new JsonObject();
        if (type != null) jQueryParams.addProperty("type", type);
                
        try {
            return Response.status(fiwareClient.updateAttributeValue(entityId, attrName, jQueryParams, contentType, content)).build();
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        return Response.status(0).build();
    }
    
    /* -------------------------------- TYPES ------------------------------- */
    @GET
    @Path("types")
    public String listEntityTypes(
            @QueryParam("limit") int limit,
            @QueryParam("offset") int offset,
            @QueryParam("options") String options) {
        //System.out.println("listEntityTypes");
        JsonObject jQueryParams = new JsonObject();
        if (limit != 0) jQueryParams.addProperty("limit", limit);
        if (limit != 0) jQueryParams.addProperty("offset", offset);
        if (options != null) jQueryParams.addProperty("options", options);        
                
        //System.out.println("Request All entities");
        try {
            return pretyGson.toJson(fiwareClient.listEntityTypes(jQueryParams));
            //System.out.println(gson.toJson(entities));
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        
        return pretyGson.toJson(new ArrayList<>());
    }
    
    @GET
    @Path("types/{entityType}")
    public String retrieveEntityType(
            @PathParam("entityType") String entityType) {
        //System.out.println("retrieveEntityType");       
                
        try {
            return pretyGson.toJson(fiwareClient.retrieveEntityType(entityType));
            //System.out.println(gson.toJson(entities));
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        
        return pretyGson.toJson(new ArrayList<>());
    }
    
    
    /* ---------------------------- SUBSCRIPTIONS --------------------------- */
    @GET
    @Path("subscriptions")
    public String listSubscriptions(
            @QueryParam("limit") int limit,
            @QueryParam("offset") int offset,
            @QueryParam("options") String options) {
        //System.out.println("listEntityTypes");
        JsonObject jQueryParams = new JsonObject();
        if (limit != 0) jQueryParams.addProperty("limit", limit);
        if (limit != 0) jQueryParams.addProperty("offset", offset);
        if (options != null) jQueryParams.addProperty("options", options);        
                
        //System.out.println("Request All entities");
        try {
            return pretyGson.toJson(fiwareClient.listSubscriptions(jQueryParams));
            //System.out.println(gson.toJson(entities));
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        
        return pretyGson.toJson(new ArrayList<>());
    }
        
    @POST
    @Path("subscriptions")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSubscription(
            String content,
            @Context HttpHeaders headers) {
        //System.out.println("updateAppendEntityAttributes");
        String contentType = headers.getHeaderString("Content-type");        
        try {
            return Response.ok().header("Location",fiwareClient.createSubscription(contentType, content)).build();
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        return Response.status(0).build();
    }
    
    @GET
    @Path("subscriptions/{subscriptionId}")
    public String retrieveSubscription(
            @PathParam("subscriptionId") String subscriptionId
            ) throws UnsupportedEncodingException, URISyntaxException {
        //System.out.println("retrieveSubscription");
        
        JsonObject jresp = null;
        
        try {
            jresp = fiwareClient.retrieveSubscription(subscriptionId);
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        return pretyGson.toJson(jresp);
    }
    
    @PATCH
    @Path("subscriptions/{subscriptionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateSubscription(
            String content,
            @PathParam("subscriptionId") String subscriptionId
            ) throws UnsupportedEncodingException, URISyntaxException {
        //System.out.println("updateSubscription");
        try {
            return Response.status(fiwareClient.updateSubscription(subscriptionId, content)).build();
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        return Response.status(0).build();
    }
    
    @DELETE
    @Path("subscriptions/{subscriptionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteSubscription(
            String content,
            @PathParam("subscriptionId") String subscriptionId
            ) throws UnsupportedEncodingException, URISyntaxException {
        //System.out.println("deleteSubscription");
        try {
            return Response.status(fiwareClient.deleteSubscription(subscriptionId, content)).build();
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        return Response.status(0).build();
    }
    
    /* ---------------------------- REGISTRATION ---------------------------- */
    // Orion Broker does not support this yet
    /*@GET
    @Path("registrations")
    public String listRegistrations(
            @QueryParam("limit") int limit,
            @QueryParam("offset") int offset,
            @QueryParam("options") String options) {
        System.out.println("listRegistrations");
        JsonObject jQueryParams = new JsonObject();
        if (limit != 0) jQueryParams.addProperty("limit", limit);
        if (limit != 0) jQueryParams.addProperty("offset", offset);
        if (options != null) jQueryParams.addProperty("options", options);        
                
        //System.out.println("Request All entities");
        try {
            return pretyGson.toJson(fiwareClient.listRegistrations(jQueryParams));
            //System.out.println(gson.toJson(entities));
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        
        return pretyGson.toJson(new ArrayList<>());
    }
    
    
    @POST
    @Path("registrations")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRegistration(
            String content,
            @Context HttpHeaders headers) {
        System.out.println("createRegistration");
        String contentType = headers.getHeaderString("Content-type");        
        try {
            return Response.ok().header("Location",fiwareClient.createRegistration(contentType, content)).build();
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        return Response.status(0).build();
    }*/
    
        
    
    
    
    
    /* ---------------------------- NOTIFICATIONS --------------------------- */
    @GET
    @Path("fiware_notification")
    public Response getNotifications(
            @Context UriInfo uriInfo) {
        System.out.println("=====> getNotifications");
        System.out.println("requestURL: "+uriInfo.getRequestUri().getQuery());
        return Response.status(200).build();
    }
    
    @PUT
    @Path("fiware_notification")
    public Response putNotifications(
            @Context UriInfo uriInfo) {
        System.out.println("=====> putNotifications");
        System.out.println("requestURL: "+uriInfo.getRequestUri().getQuery());
        return Response.status(200).build();
    }
    
    @POST
    @Path("fiware_notification")
    public Response postNotifications(
            String content,
            @Context HttpHeaders headers,
            @Context UriInfo uriInfo) {
        System.out.println("=====> postNotifications");
        System.out.println("requestURL: "+uriInfo.getRequestUri().getQuery());
        String contentType = headers.getHeaderString("Content-type");
        System.out.println("Content-type: "+contentType);
        JsonObject json = new JsonParser().parse(content).getAsJsonObject();
        System.out.println("Content:\n"+pretyGson.toJson(json));        
        return Response.status(200).build();
    }
    
    @DELETE
    @Path("fiware_notification")
    public Response deleteNotifications(
            @Context UriInfo uriInfo) {
        System.out.println("=====> deleteNotifications");
        System.out.println("requestURL: "+uriInfo.getRequestUri().getQuery());
        return Response.status(200).build();
    }
    
    @PATCH
    @Path("fiware_notification")
    public Response patchNotifications(
            @Context UriInfo uriInfo) {
        System.out.println("=====> patchNotifications");
        System.out.println("requestURL: "+uriInfo.getRequestUri().getQuery());
        return Response.status(200).build();
    }
    
    
    /* ---------------------------------------------------------------------- */
    @Override
    public void update(Observable o, Object arg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
}
