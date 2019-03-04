/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.core.fiware.common;

/**
 *
 * @author Pablo Puñal Pereira <pablo.punal@thingwave.eu>
 */
public class ServiceURL {
    private final ServiceUrlName serviceUrlName;
    private final String url;
    
    public ServiceURL(ServiceUrlName serviceUrlName, String url) {
        this.serviceUrlName = serviceUrlName;
        this.url = url;
    }
    
    public ServiceURL(String serviceUrlName, String url) {
        this.serviceUrlName = ServiceUrlName.valueOf(serviceUrlName);
        this.url = url;
    }
    
    public ServiceUrlName getUrlName() {
        return serviceUrlName;
    }
    
    public String getUrl() {
        return url;
    }
    
    
    public enum ServiceUrlName {
        entities_url        (0),
        types_url           (1),
        subscriptions_url   (2),
        registrations_url   (3);

        public final int value;

        ServiceUrlName(int value) {
            this.value = value;
        }

        public static ServiceUrlName valueOf(final int value) {
            switch (value) {
                case 0: return entities_url;
                case 1: return types_url;
                case 2: return subscriptions_url;
                case 3: return registrations_url;
                default: throw new IllegalArgumentException("Unknown ServiceUrlName " + value);
            }
        }
    }
}
