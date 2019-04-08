/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.core.fiware;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.messages.ServiceQueryForm;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.fiware.client.FiwareClient;
import eu.arrowhead.core.fiware.common.FiwareTools;
import eu.arrowhead.core.fiware.common.SenML;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
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
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author Pablo Puñal Pereira <pablo.punal@thingwave.eu>
 */

/**
 * REST resource for the Fiware Plugin.
 */

@Path("plugin")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PluginResource implements Observer {
    private final GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping();
    private final Gson gson = gsonBuilder.create();
    private final Gson pretyGson = gsonBuilder.setPrettyPrinting().create();
    private final FiwareClient fiwareClient;
    
    private static final DatabaseManager dm = DatabaseManager.getInstance();
    
    public PluginResource() {
        System.out.println("Plugin Resource start!");
        fiwareClient = new FiwareClient("http://localhost:1026");        
    }
    
    
    @GET
    @Path("service/{entityId}/{serviceName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEntityValue(
            @PathParam("entityId") String entityId,
            @PathParam("serviceName") String serviceName
            ) throws UnsupportedEncodingException, URISyntaxException {
        //System.out.println("getEntityValue "+entityId+" "+serviceName);
        
        
        try {
            String value = fiwareClient.getAttributeValue(entityId, serviceName);
            if (!value.isEmpty()) {
                SenML senML = new SenML();
                senML.addElement(
                        new SenML.SenMLElement()
                        .setName(serviceName)
                        .setBaseTime(System.currentTimeMillis())
                        .setStringValue(value)
                        .setTime(0)
                );
                return Response.status(200).entity(senML.toPrettyJSON()).build();
            }
                       
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.getLocalizedMessage());
        }
        return Response.status(204).build();
    }
    
    
    @PUT // Create Entity if doesn't exist
    @Path("service/{entityId}/{serviceName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createEntity(
            String content,
            @PathParam("entityId") String entityId,
            @PathParam("serviceName") String serviceName){
        int status = 404;
        JsonObject jEntity = new JsonObject();
        jEntity.addProperty("id", entityId);
        jEntity.addProperty("type", serviceName);
        
        try {
            status = fiwareClient.createEntity(jEntity);
            SenML senML = new SenML();
            senML.fromJSON(content);        
            JsonObject jContent = new JsonObject();
            JsonObject jValue = new JsonObject();

            if (senML.getLastest().getValue() != null) {
                jValue.addProperty("type", "Number");
                jValue.addProperty("value", senML.getLastest().getValue());
            } else if (senML.getLastest().getStringValue() != null) {
                jValue.addProperty("type", "Text");
                jValue.addProperty("value", senML.getLastest().getStringValue());
            }

            jContent.add(serviceName, jValue);
            status = fiwareClient.updateAppendEntityAttributes(entityId, jContent);
            
        } catch (IOException ex) {
            return Response.status(500).build();
        } catch (URISyntaxException ex) {
            return Response.status(204).build();
        }
        
        return Response.status(status).build();
    }
    
    @POST // Update Value
    @Path("service/{entityId}/{serviceName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateEntity(
            String content,
            @PathParam("entityId") String entityId,
            @PathParam("serviceName") String serviceName){
        
        int status = 404;
        
        SenML senML = new SenML();
        senML.fromJSON(content);        
        JsonObject jContent = new JsonObject();
        JsonObject jValue = new JsonObject();
        
        if (senML.getLastest().getValue() != null) {
            jValue.addProperty("type", "Number");
            jValue.addProperty("value", senML.getLastest().getValue());
        } else if (senML.getLastest().getStringValue() != null) {
            jValue.addProperty("type", "Text");
            jValue.addProperty("value", senML.getLastest().getStringValue());
        }
        
        jContent.add(serviceName, jValue);
        
        try {
            status = fiwareClient.updateAppendEntityAttributes(entityId, jContent);
            //System.out.println("Status: "+status);
        } catch (IOException ex) {
            return Response.status(500).build();
        } catch (URISyntaxException ex) {
            return Response.status(204).build();
        }
        
        return Response.status(status).build();
    }
    
    @DELETE
    @Path("service/{entityId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deteleEntity(
            @PathParam("entityId") String entityId){
        int status = 404;
        
        try {
            status = fiwareClient.removeEntity(entityId);
        } catch (URISyntaxException ex) {
            status = 204;
        } catch (IOException ex) {
            status = 500;
        }
        
        return Response.status(status).build();
    }
    
    
    /* ---------------------------------------------------------------------- */
    @Override
    public void update(Observable o, Object arg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
