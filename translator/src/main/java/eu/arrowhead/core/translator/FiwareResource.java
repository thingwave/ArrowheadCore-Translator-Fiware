package eu.arrowhead.core.translator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.arrowhead.core.fiware.client.FiwareClient;
import eu.arrowhead.core.fiware.common.FiwareTools;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

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

/**
 * REST resource for the Translator Core System.
 */

@Path("v2")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class FiwareResource implements Observer{
    
    private final GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping();
    private final Gson gson = gsonBuilder.create();
    private final Gson pretyGson = gsonBuilder.setPrettyPrinting().create();
    private final FiwareClient fiwareClient;
    
    public FiwareResource() {
        System.out.println("FIWARE PLUGING start!");
        fiwareClient = new FiwareClient("http://localhost:1026");
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
        System.out.println("listEntities");
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
        
        ArrayList<JsonObject> entities = new ArrayList<>();
        
        //System.out.println("Request All entities");
        try {
            entities = fiwareClient.listEntities(jQueryParams);
            //System.out.println(gson.toJson(entities));
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        
        System.out.println("GET entities - Transparent request/response");
        return pretyGson.toJson(entities);
    }
    
    @POST
    @Path("entities")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createEntity(
            String content,
            @QueryParam("options") String options) {
        System.out.println("createEntity");
        if (options != null)
            System.out.println("Complete support for options: "+options);        
        try {
            return Response.status(fiwareClient.createEntity(content)).build();
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
        System.out.println("retrieveEntity");
        
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
        System.out.println("retrieveEntityAttributes");
        
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
            String content,
            @PathParam("entityId") String entityId,
            @QueryParam("type") String type,
            @QueryParam("options") String options) {
        System.out.println("updateAppendEntityAttributes");
        JsonObject jQueryParams = new JsonObject();
        if (type != null) jQueryParams.addProperty("type", type);
        if (type != null) jQueryParams.addProperty("options", options);
        JsonObject jContent = new JsonParser().parse(content).getAsJsonObject();        
        try {
            return Response.status(fiwareClient.updateAppendEntityAttributes(entityId, jQueryParams, jContent)).build();
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
        System.out.println("updateExistingEntityAttributes");
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
        System.out.println("replaceAllEntityAttributes");
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
        System.out.println("getAttributeData");
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
        System.out.println("updateAttributeData");
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
        System.out.println("removeASingleAttribute");
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
        System.out.println("getAttributeValue");
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
        System.out.println("updateAttributeValue");
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
        System.out.println("listEntityTypes");
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
        System.out.println("retrieveEntityType");       
                
        try {
            return pretyGson.toJson(fiwareClient.retrieveEntityType(entityType));
            //System.out.println(gson.toJson(entities));
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        
        return pretyGson.toJson(new ArrayList<>());
    }
    
    
    
    
    /* ---------------------------------------------------------------------- */
    @Override
    public void update(Observable o, Object arg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
}
