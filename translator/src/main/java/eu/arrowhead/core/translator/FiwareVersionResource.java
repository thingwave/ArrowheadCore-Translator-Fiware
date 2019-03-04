package eu.arrowhead.core.translator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.util.Observable;
import java.util.Observer;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * REST resource for the Translator Core System.
 */

@Path("version")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class FiwareVersionResource implements Observer{
    
    private final Gson gson = new Gson();
    private final Gson pretyGson = new GsonBuilder().setPrettyPrinting().create();
	
    @GET
    public String getIt() {
        JsonObject jresp = new JsonObject();
        JsonObject jSoft = new JsonObject();
        jSoft.addProperty("version", "1.0.0-beta");
        jSoft.addProperty("uptime", "0d, 0h, 0m, 0s");
        jSoft.addProperty("release_date", "Mon Feb 18 10:00:00 UTC 2019");
        jSoft.addProperty("doc", "https://github.com/arrowhead-f/core-java/tree/master/documentation");
        jresp.add("AH-FIWARE", jSoft);
        return pretyGson.toJson(jresp);
    }
        
    @Override
    public void update(Observable o, Object arg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
}
