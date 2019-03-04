/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.core.fiware.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import eu.arrowhead.core.fiware.common.ServiceURL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author Pablo Puñal Pereira <pablo.punal@thingwave.eu>
 */
public class FiwareClient {
    private final String orionBrokerURL;
    private final CloseableHttpClient httpClient;
    private final GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping();
    private final Gson gson = gsonBuilder.create();
    private final Gson pretyGson = gsonBuilder.setPrettyPrinting().create();
    private final JsonParser parser = new JsonParser();
    
    // Internal URLs of each service
    private String entitiesURL;
    private String typesURL;
    private String subscriptionsURL;
    private String registrationsURL;
    
    public FiwareClient(String orionBrokerURL) {
        this.orionBrokerURL = orionBrokerURL;
        httpClient = HttpClients.createDefault();
        updateServicesUrlList();
    }
    
    private int httpDELETE(String url) throws UnsupportedEncodingException, IOException {
        HttpDelete delete = new HttpDelete(url);
        
        System.out.println("DELETE: "+delete.toString());
        CloseableHttpResponse resp = httpClient.execute(delete);
        return resp.getStatusLine().getStatusCode();
    }
    
    private String httpGET(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        //System.out.println("GET: "+get.toString());
        CloseableHttpResponse resp = httpClient.execute(get);
        BufferedReader rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line = "";
        while((line = rd.readLine()) != null)
            result.append(line);
        return result.toString();
    }
    
    private int httpPATCH(String url, String content) throws UnsupportedEncodingException, IOException {
        HttpPatch patch = new HttpPatch(url);
        // Header
        patch.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        
        // Content
        StringEntity entity = new StringEntity(content);
        patch.setEntity(entity);
        
        //System.out.println("PATCH: "+post.toString());
        CloseableHttpResponse resp = httpClient.execute(patch);
        return resp.getStatusLine().getStatusCode();
    }
    
    private int httpPOST(String url, String content) throws UnsupportedEncodingException, IOException {
        HttpPost post = new HttpPost(url);
        // Header
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        
        // Content
        StringEntity entity = new StringEntity(content);
        post.setEntity(entity);
        
        //System.out.println("POST: "+post.toString());
        CloseableHttpResponse resp = httpClient.execute(post);
        return resp.getStatusLine().getStatusCode();
    }
    
    private int httpPUT(String url, String contentType, String content) throws UnsupportedEncodingException, IOException {
        HttpPut put = new HttpPut(url);
        // Header
        put.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
        
        // Content
        StringEntity entity = new StringEntity(content);
        put.setEntity(entity);
        
        //System.out.println("PATCH: "+post.toString());
        CloseableHttpResponse resp = httpClient.execute(put);
        return resp.getStatusLine().getStatusCode();
    }
    
    private void updateServicesUrlList() {
        System.out.println("Updating Services URL from OrionBroker...");
        entitiesURL = "";
        typesURL = "";
        subscriptionsURL = "";
        registrationsURL = "";
        try {
            ArrayList<ServiceURL> services = getAllServices();
            services.forEach((service) -> {
                switch(service.getUrlName()) {
                    case entities_url:
                        entitiesURL = service.getUrl();
                        break;
                    case types_url:
                        typesURL = service.getUrl();
                        break;
                    case subscriptions_url:
                        subscriptionsURL = service.getUrl();
                        break;
                    case registrations_url:
                        registrationsURL = service.getUrl();
                        break;
                    default:
                                //new Exception("Unsuported serviceUrlName "+service.getUrlName())
                        break;
                }
            });
        } catch (IOException ex) {
            System.out.println("Error "+ex);
        }
        System.out.println("Services:");
        System.out.println("entitiesURL: "+entitiesURL);
        System.out.println("typesURL: "+typesURL);
        System.out.println("subscriptionsURL: "+subscriptionsURL);
        System.out.println("registrationsURL: "+registrationsURL);
    }
    
    public ArrayList<ServiceURL> getAllServices() throws IOException {
        ArrayList<ServiceURL> services = new ArrayList<>();
        JsonObject json = new JsonParser().parse(httpGET(orionBrokerURL+"/version")).getAsJsonObject();
        System.out.println("Connected to Fiware Broker, info:\n"+pretyGson.toJson(json));
        json = new JsonParser().parse(httpGET(orionBrokerURL+"/v2")).getAsJsonObject();
        json.entrySet().forEach((entry) -> {
            services.add(new ServiceURL(entry.getKey(), entry.getValue().getAsString()));
        });
        return services;
    }
    
    /* ------------------------------ ENTITIES ------------------------------ */
    
    public ArrayList<JsonObject> listEntities(JsonObject queryParams) throws IOException, URISyntaxException {
        URIBuilder urlBuilder = new URIBuilder(orionBrokerURL+entitiesURL);
        
        // Add queries and remove empty queries
        if (queryParams != null) {
            Set<Map.Entry<String, JsonElement>> entries = queryParams.entrySet();
            for (Map.Entry<String, JsonElement> entry: entries) {
                System.out.println(entry.getKey()+" = "+entry.getValue());
                if (!entry.getValue().getAsString().isEmpty())
                    urlBuilder.addParameter(entry.getKey(), entry.getValue().getAsString());
            }
        }
        return new Gson().fromJson(
                httpGET(urlBuilder.build().toString()),
                new TypeToken<ArrayList<JsonObject>>() {}.getType());
    }
        
    public int createEntity(String entityInfo) throws IOException {
        return httpPOST(orionBrokerURL+entitiesURL, entityInfo);
    }
    
    public JsonObject retrieveEntity(String entityId, JsonObject queryParams) throws IOException, URISyntaxException {
        URIBuilder urlBuilder = new URIBuilder(orionBrokerURL+entitiesURL+"/"+entityId);
        
        // Add queries and remove empty queries
        if (queryParams != null) {
            Set<Map.Entry<String, JsonElement>> entries = queryParams.entrySet();
            for (Map.Entry<String, JsonElement> entry: entries) {
                System.out.println(entry.getKey()+" = "+entry.getValue());
                if (!entry.getValue().getAsString().isEmpty())
                    urlBuilder.addParameter(entry.getKey(), entry.getValue().getAsString());
            }
        }
        
        return new JsonParser().parse(httpGET(urlBuilder.build().toString())).getAsJsonObject();
    }
    
    public JsonObject retrieveEntityAttributes(String entityId, JsonObject queryParams) throws IOException, URISyntaxException {
        URIBuilder urlBuilder = new URIBuilder(orionBrokerURL+entitiesURL+"/"+entityId+"/attrs");
        
        // Add queries and remove empty queries
        if (queryParams != null) {
            Set<Map.Entry<String, JsonElement>> entries = queryParams.entrySet();
            for (Map.Entry<String, JsonElement> entry: entries) {
                System.out.println(entry.getKey()+" = "+entry.getValue());
                if (!entry.getValue().getAsString().isEmpty())
                    urlBuilder.addParameter(entry.getKey(), entry.getValue().getAsString());
            }
        }
        
        return new JsonParser().parse(httpGET(urlBuilder.build().toString())).getAsJsonObject();
    }
    
    
    public int updateAppendEntityAttributes(String entityId, JsonObject queryParams, JsonObject content) throws URISyntaxException, IOException {
        URIBuilder urlBuilder = new URIBuilder(orionBrokerURL+entitiesURL+"/"+entityId+"/attrs");
        
        // Add queries and remove empty queries
        if (queryParams != null) {
            Set<Map.Entry<String, JsonElement>> entries = queryParams.entrySet();
            for (Map.Entry<String, JsonElement> entry: entries) {
                System.out.println(entry.getKey()+" = "+entry.getValue());
                if (!entry.getValue().getAsString().isEmpty())
                    urlBuilder.addParameter(entry.getKey(), entry.getValue().getAsString());
            }
        }
        
        return httpPOST(urlBuilder.build().toString(), gson.toJson(content));
    }
    
    
    public int updateExistingEntityAttributes(String entityId, JsonObject queryParams, JsonObject content) throws URISyntaxException, IOException {
        URIBuilder urlBuilder = new URIBuilder(orionBrokerURL+entitiesURL+"/"+entityId+"/attrs");
        
        // Add queries and remove empty queries
        if (queryParams != null) {
            Set<Map.Entry<String, JsonElement>> entries = queryParams.entrySet();
            for (Map.Entry<String, JsonElement> entry: entries) {
                System.out.println(entry.getKey()+" = "+entry.getValue());
                if (!entry.getValue().getAsString().isEmpty())
                    urlBuilder.addParameter(entry.getKey(), entry.getValue().getAsString());
            }
        }
        
        return httpPATCH(urlBuilder.build().toString(), gson.toJson(content));
    }
    
    public int replaceAllEntityAttributes(String entityId, JsonObject queryParams, String contentType, String content) throws URISyntaxException, IOException {
        URIBuilder urlBuilder = new URIBuilder(orionBrokerURL+entitiesURL+"/"+entityId+"/attrs");
        
        // Add queries and remove empty queries
        if (queryParams != null) {
            Set<Map.Entry<String, JsonElement>> entries = queryParams.entrySet();
            for (Map.Entry<String, JsonElement> entry: entries) {
                System.out.println(entry.getKey()+" = "+entry.getValue());
                if (!entry.getValue().getAsString().isEmpty())
                    urlBuilder.addParameter(entry.getKey(), entry.getValue().getAsString());
            }
        }
        
        return httpPUT(urlBuilder.build().toString(), contentType, content);
    }
    
    public int removeEntity(String entityId, JsonObject queryParams) throws URISyntaxException, IOException {
        URIBuilder urlBuilder = new URIBuilder(orionBrokerURL+entitiesURL+"/"+entityId);
        
        // Add queries and remove empty queries
        if (queryParams != null) {
            Set<Map.Entry<String, JsonElement>> entries = queryParams.entrySet();
            for (Map.Entry<String, JsonElement> entry: entries) {
                System.out.println(entry.getKey()+" = "+entry.getValue());
                if (!entry.getValue().getAsString().isEmpty())
                    urlBuilder.addParameter(entry.getKey(), entry.getValue().getAsString());
            }
        }
        
        return httpDELETE(urlBuilder.build().toString());
    }
    
    /* ----------------------------- ATTRIBUTES ----------------------------- */
    public JsonObject getAttributeData(String entityId, String attrName, JsonObject queryParams) throws IOException, URISyntaxException {
        URIBuilder urlBuilder = new URIBuilder(orionBrokerURL+entitiesURL+"/"+entityId+"/attrs/"+attrName);
        
        // Add queries and remove empty queries
        if (queryParams != null) {
            Set<Map.Entry<String, JsonElement>> entries = queryParams.entrySet();
            for (Map.Entry<String, JsonElement> entry: entries) {
                System.out.println(entry.getKey()+" = "+entry.getValue());
                if (!entry.getValue().getAsString().isEmpty())
                    urlBuilder.addParameter(entry.getKey(), entry.getValue().getAsString());
            }
        }
        
        return new JsonParser().parse(httpGET(urlBuilder.build().toString())).getAsJsonObject();
    }
    
    public int updateAttributeData(String entityId, String attrName, JsonObject queryParams, String contentType, String content) throws URISyntaxException, IOException {
        URIBuilder urlBuilder = new URIBuilder(orionBrokerURL+entitiesURL+"/"+entityId+"/attrs/"+attrName);
        
        // Add queries and remove empty queries
        if (queryParams != null) {
            Set<Map.Entry<String, JsonElement>> entries = queryParams.entrySet();
            for (Map.Entry<String, JsonElement> entry: entries) {
                System.out.println(entry.getKey()+" = "+entry.getValue());
                if (!entry.getValue().getAsString().isEmpty())
                    urlBuilder.addParameter(entry.getKey(), entry.getValue().getAsString());
            }
        }
        
        return httpPUT(urlBuilder.build().toString(), contentType, content);
    }
        
    public int removeASingleAttribute(String entityId, String attrName, JsonObject queryParams) throws URISyntaxException, IOException {
        URIBuilder urlBuilder = new URIBuilder(orionBrokerURL+entitiesURL+"/"+entityId+"/attrs/"+attrName);
        
        // Add queries and remove empty queries
        if (queryParams != null) {
            Set<Map.Entry<String, JsonElement>> entries = queryParams.entrySet();
            for (Map.Entry<String, JsonElement> entry: entries) {
                System.out.println(entry.getKey()+" = "+entry.getValue());
                if (!entry.getValue().getAsString().isEmpty())
                    urlBuilder.addParameter(entry.getKey(), entry.getValue().getAsString());
            }
        }
        
        return httpDELETE(urlBuilder.build().toString());
    }
    
    
    /* --------------------------- ATTRIBUTE VALUE -------------------------- */
    public String getAttributeValue(String entityId, String attrName, JsonObject queryParams) throws IOException, URISyntaxException {
        URIBuilder urlBuilder = new URIBuilder(orionBrokerURL+entitiesURL+"/"+entityId+"/attrs/"+attrName+"/value");
        
        // Add queries and remove empty queries
        if (queryParams != null) {
            Set<Map.Entry<String, JsonElement>> entries = queryParams.entrySet();
            for (Map.Entry<String, JsonElement> entry: entries) {
                System.out.println(entry.getKey()+" = "+entry.getValue());
                if (!entry.getValue().getAsString().isEmpty())
                    urlBuilder.addParameter(entry.getKey(), entry.getValue().getAsString());
            }
        }
        
        return httpGET(urlBuilder.build().toString());
    }
    
    public int updateAttributeValue(String entityId, String attrName, JsonObject queryParams, String contentType, String content) throws URISyntaxException, IOException {
        URIBuilder urlBuilder = new URIBuilder(orionBrokerURL+entitiesURL+"/"+entityId+"/attrs/"+attrName+"/value");
        
        // Add queries and remove empty queries
        if (queryParams != null) {
            Set<Map.Entry<String, JsonElement>> entries = queryParams.entrySet();
            for (Map.Entry<String, JsonElement> entry: entries) {
                System.out.println(entry.getKey()+" = "+entry.getValue());
                if (!entry.getValue().getAsString().isEmpty())
                    urlBuilder.addParameter(entry.getKey(), entry.getValue().getAsString());
            }
        }
        
        return httpPUT(urlBuilder.build().toString(), contentType, content);
    }
    
    /* -------------------------------- TYPES ------------------------------- */
    public ArrayList<JsonObject> listEntityTypes(JsonObject queryParams) throws IOException, URISyntaxException {
        URIBuilder urlBuilder = new URIBuilder(orionBrokerURL+typesURL);
        
        // Add queries and remove empty queries
        if (queryParams != null) {
            Set<Map.Entry<String, JsonElement>> entries = queryParams.entrySet();
            for (Map.Entry<String, JsonElement> entry: entries) {
                System.out.println(entry.getKey()+" = "+entry.getValue());
                if (!entry.getValue().getAsString().isEmpty())
                    urlBuilder.addParameter(entry.getKey(), entry.getValue().getAsString());
            }
        }
        
        return new Gson().fromJson(
                httpGET(urlBuilder.build().toString()),
                new TypeToken<ArrayList<JsonObject>>() {}.getType());
    }
    
    public JsonObject retrieveEntityType(String entityType) throws IOException, URISyntaxException {
        URIBuilder urlBuilder = new URIBuilder(orionBrokerURL+typesURL+"/"+entityType);        
        return new JsonParser().parse(httpGET(urlBuilder.build().toString())).getAsJsonObject();
    }
}
