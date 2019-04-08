package eu.arrowhead.core.fiware.arrowhead;

/**
 *
 * @author Pablo Puñal Pereira <pablo.punal@thingwave.eu>
 */
public class ArrowheadEntity {
    private String id;
    private String url;
    
    public ArrowheadEntity(String id, String url) {
        this.id = id;
        this.url = url;
    }
    
    public String getId() {
        return id;
    }
    
    public String getURL() {
        return url;
    }
}
