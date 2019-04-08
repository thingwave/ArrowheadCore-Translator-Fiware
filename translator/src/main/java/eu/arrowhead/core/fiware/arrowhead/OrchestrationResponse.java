/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.core.fiware.arrowhead;

import java.util.ArrayList;

/**
 *
 * @author Pablo Puñal Pereira <pablo.punal@thingwave.eu>
 */
public class OrchestrationResponse {
    ArrayList<OrchestrationService> response;
    
    public OrchestrationResponse() {
        response = new ArrayList();
    }
    
    public ArrayList<OrchestrationService> getAllServices() {
        return response;
    }
    
    public boolean isEmpty() {
        return response.isEmpty();
    }
    
    public int size() {
        return response.size();
    }
    
    public OrchestrationService getService(int index) {
        return response.get(index);
    }
}
