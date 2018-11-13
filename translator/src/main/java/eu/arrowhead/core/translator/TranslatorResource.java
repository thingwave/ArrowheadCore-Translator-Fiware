package eu.arrowhead.core.translator;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Map.Entry;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST resource for the Translator Core System.
 */
//XXX: Change to JSON instead of XML
@Path("translator")
@Produces(MediaType.APPLICATION_XML)
@Singleton
public class TranslatorResource implements Observer{
	
	Map<Integer, TranslatorHub> hubs = new HashMap<Integer, TranslatorHub>();
	
	/**
	 * Simple test method to see if the http server where this resource is registered works or not.
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt() {
		return "This is the Translator Arrowhead Core System";
	}
  
	/**
	 * This method initiates the creation of a new translation hub, if none exists already, between two systems.
	 * 
	 * @return Endpoint where the consumer system can get connect to consume the service in its protocol
	 */
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	public Response postTranslator(TranslatorSetup setup) {
		Response response;
		
		if (setup.getConsumerAddress().equals("127.0.0.1") || setup.getConsumerAddress().equals("localhost") || setup.getProviderAddress().equals("127.0.0.1") || setup.getProviderAddress().equals("localhost")) {
		System.out.println("Not valid ip address, we need absolut address, not relative (as 127.0.0.1 or localhost)");
		response = Response.serverError().build();
	}
	else {
		//--------- Check if a hub which satisfies the  request already exists ----------
		//make a fingerprint(translatorId) based on service provider name and service consumer name combined and hashed.
		//combination of providername and consumername will be a fully unique pairing. THIS MAY CHANGE IN THE FUTURE AND DO IT BASED ON TYPE + ADDRESS
		int translatorId = (setup.getProviderName() + setup.getConsumerName()).hashCode();
		
		if(!hubs.isEmpty() && hubs.containsKey(translatorId)) {
			TranslatorHub existingHub = hubs.get(translatorId);
			response = Response.ok("<translationendpoint><id>" + existingHub.getTranslatorId() + "</id><ip>"+ existingHub.getPSpokeIp() +"</ip><port>"+ existingHub.getPSpokePort() +"</port></translationendpoint>").build();
		} 
		else {
		
			TranslatorHub hub = new TranslatorHub(this);
			System.out.println("post to Translator received: ClientSpoke type:" + setup.getProviderType() + " ServerSpoke type: " + setup.getConsumerType());
			
			//cSpoke is the spoke connected to the service provider endpoint
			String cSpoke_ProviderName = 	setup.getProviderName().substring(0).toLowerCase();
			String cSpoke_ProviderType = 	setup.getProviderType().substring(0).toLowerCase();
			String cSpoke_ProviderAddress = setup.getProviderAddress().substring(0).toLowerCase();
			
			//pSpoke is the spoke connected to the service consumer endpoint
			String pSpoke_ConsumerName = 	setup.getConsumerName().substring(0).toLowerCase();
			String pSpoke_ConsumerType = 	setup.getConsumerType().substring(0).toLowerCase();
			String pSpoke_ConsumerAddress = setup.getConsumerAddress().substring(0).toLowerCase();
			
			hub.setPSpoke_ConsumerName(pSpoke_ConsumerName);
			hub.setpSpoke_ConsumerType(pSpoke_ConsumerType);
			hub.setpSpoke_ConsumerAddress(pSpoke_ConsumerAddress);
			hub.setCSpoke_ProviderName(cSpoke_ProviderName);
			hub.setcSpoke_ProviderType(cSpoke_ProviderType);
			hub.setCSpoke_ProviderAddress(cSpoke_ProviderAddress);//hub.setServiceEndpoint("coap://127.0.0.1:5692/");
			
			hub.setTranslatorId(translatorId);
			int error = 0;
			try {
				hub.online();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				error = 1;
			}
			
			if(error==0) {
				hubs.put(translatorId, hub);
	
				response = Response.ok("<translationendpoint><id>" + translatorId + "</id><ip>"+ hub.getPSpokeIp() +"</ip><port>"+ hub.getPSpokePort() +"</port></translationendpoint>").build();
				} else {
					response = Response.serverError().build();
				}
			}
		}
		return response;
	
	}
	
	/**
	 * Public function to check all active hubs
	 * 
	 * @return All active hubs
	 */
	@Path("all")
	@GET
    public Response getTranslatorList() {
		Response response;
		
		String hubResponse = "<translatorList>";
		if (hubs.isEmpty()) {
			//List must be empty if there is no tag, can not have literal value
		}
		else {
			hubResponse += "\n";
			for (Entry<Integer, TranslatorHub> entry : hubs.entrySet()) {
				hubResponse += "<translatorId>" + entry.getKey() + "</translatorId><translatorAddress>" + entry.getValue().getPSpokeAddress()+"</translatorAddress>+\n";
			}
		}
		hubResponse +="</translatorList>";
		
		response = Response.ok(hubResponse).build();// No need to specify XML Mediatype as the runtime will set it with @Produces
		return response;
	
	}
	
	/**
	 * Public function to check for a specific hub provided his translatorId
	 * 
	 * @return Address of matching translator hub
	 */
	@Path("{translatorId}")
	@GET
	public Response getTranslator( @PathParam("translatorId") int translatorid) {
		Response response;
		TranslatorHub hub = null;
		
		hub = hubs.get(translatorid);
		String hubResponse = "<translatorQueried>\n";
		if(hub != null) {
			hubResponse += "<translatorId>" + translatorid + "</translatorId><translatorAddress>"+ hub.getPSpokeAddress() +"</translatorAddress>\n";
		} 
		else {
			hubResponse += "<translatorId>" + translatorid + "</translatorId><error>" + "Hub does not exist" + "</error>\n";
		}
		hubResponse += "</translatorQueried>";
		
		response = Response.ok(hubResponse).build();
		return response;
	
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		hubs.remove((int)arg1);
		System.out.println("Cleanup hub: " + (int)arg1);
		}
  

}
