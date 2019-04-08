/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.core.fiware.arrowhead;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;

/**
 *
 * @author Pablo Puñal Pereira <pablo.punal@thingwave.eu>
 */
public class ServiceInfo {
    private final String serviceDefinition;
    private ArrayList<String> interfaces;
    private JsonObject serviceMetadata;
    
    public ServiceInfo(String serviceDefinition, ArrayList<String> interfaces, JsonObject serviceMetadata) {
        this.serviceDefinition = serviceDefinition;
        this.interfaces = interfaces;
        this.serviceMetadata = serviceMetadata;
    }
    
    public ServiceInfo(String serviceDefinition) {
        this.serviceDefinition = serviceDefinition;
        interfaces = new ArrayList<>();
        serviceMetadata = new JsonObject();
    }
    
    public void addInterface(String intface) {
        this.interfaces.add(intface);
    }
    
    public void addInterfaces(ArrayList<String> interfaces) {
        this.interfaces = interfaces;
    }
    
    public void addMetadata(JsonObject serviceMetadata) {
        this.serviceMetadata = serviceMetadata;
    }
    
    public void addMetadata(String property, String Value) {
        this.serviceMetadata.addProperty(property, Value);
    }
    
    public void addMetadata(String property, Number Value) {
        this.serviceMetadata.addProperty(property, Value);
    }
    
    public void addMetadata(String property, boolean Value) {
        this.serviceMetadata.addProperty(property, Value);
    }
    
    public void addMetadata(String property, Character Value) {
        this.serviceMetadata.addProperty(property, Value);
    }
    
    public void addMetadata(String property, JsonElement Value) {
        this.serviceMetadata.add(property, Value);
    }
}
