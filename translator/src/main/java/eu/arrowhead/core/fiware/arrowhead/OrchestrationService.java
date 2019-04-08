/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.core.fiware.arrowhead;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 *
 * @author Pablo Puñal Pereira <pablo.punal@thingwave.eu>
 */
public class OrchestrationService {
    private ServiceInfo service;
    private OrchestrationProvider provider;
    private String serviceURI;
    private ArrayList<String> warnings;
    
    
    public URL getServiceURL() throws MalformedURLException {
        return new URL("http", provider.getAddress(), provider.getPort(), serviceURI);
    }
    
    public OrchestrationProvider getProvider() {
        return provider;
    }
}
