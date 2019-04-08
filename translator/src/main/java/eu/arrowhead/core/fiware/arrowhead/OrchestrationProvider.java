package eu.arrowhead.core.fiware.arrowhead;

/**
 *
 * @author Pablo Puñal Pereira <pablo.punal@thingwave.eu>
 */
public class OrchestrationProvider {
    private int id = 0;
    private String systemName = "";
    private String address = "";
    private int port = 0;
    
    public OrchestrationProvider() {}
    
    public int getId() {
        return id;
    }
    
    public String getSystemName() {
        return systemName;
    }
    
    public String getAddress() {
        return address;
    }
    
    public int getPort() {
        return port;
    }
}
