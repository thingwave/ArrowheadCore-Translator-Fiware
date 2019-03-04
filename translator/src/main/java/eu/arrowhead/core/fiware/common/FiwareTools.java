/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.core.fiware.common;

import com.google.gson.JsonObject;

/**
 *
 * @author Pablo Puñal Pereira <pablo.punal@thingwave.eu>
 */
public class FiwareTools {
    public static JsonObject parseQueryParams(String query) {
        JsonObject json = new JsonObject();
        if (query == null) return json;
        
        String[] queryParams = query.split("&");
        for (String queryParam: queryParams) {
            int idx = queryParam.indexOf("=");
                json.addProperty(
                    queryParam.substring(0, idx),
                    queryParam.substring(idx+1)
                );
        }
        
        return json;
    }
}
