/**
 * Copyright (c) <2016> <hasder>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 	
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 * 
*/

package eu.arrowhead.core.spokes;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.CoapEndpoint.CoapEndpointBuilder;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.Exchange;
import org.eclipse.californium.core.server.resources.Resource;

import eu.arrowhead.core.translator.BaseContext;

public class CoapServer_spoke extends CoapServer implements BaseSpokeProvider{

	BaseSpoke nextSpoke;
	Map<Integer, Exchange> cachedCoapExchangeMap = new HashMap<Integer,Exchange>();
	String interfaceAddress = "";
	
	
	public CoapServer_spoke(String ipaddress, int port) {
		super(port);
		this.interfaceAddress = ipaddress;
		this.start();
	}
	
	public CoapServer_spoke(String ipaddress) {
		super();
		
		InetSocketAddress socketAddress = new InetSocketAddress(ipaddress, 0);
		this.interfaceAddress = ipaddress;
		CoapEndpointBuilder builderCoap = new CoapEndpoint.CoapEndpointBuilder();
		builderCoap.setInetSocketAddress(socketAddress);
		this.addEndpoint(builderCoap.build());
		this.start();
	}

	@Override
	protected Resource createRoot() {
		return new RootResource();
	}

	/**
	 * Represents the root of a resource tree.
	 */
	private class RootResource extends CoapResource {
		
		public RootResource() {
			super("");
		}
		
		public List<Endpoint> getEndpoints() {
			return CoapServer_spoke.this.getEndpoints();
		}
		
		@Override
		public void handleRequest(Exchange exchange) {
			exchange.sendAccept();
			activity++;
			BaseContext context = new BaseContext();

			cachedCoapExchangeMap.put(context.getKey(), exchange);
			
			context.setContent(exchange.getRequest().getPayloadString());
			context.setPath(exchange.getRequest().getOptions().getUriPathString());
			
			Code code = exchange.getRequest().getCode();
			String method = "";
			switch (code) {
				case GET:		method = "get"; break;
				case POST:		method = "post"; break;
				case PUT:		method = "put"; break;
				case DELETE: 	method = "delete"; break;
			}
			
			context.setMethod(method);
			
			//context.setContentType(exchange.getRequest().getOptions().getContentFormat());
			
			nextSpoke.in(context);
			
		}
		
		@Override
		public Resource getChild(String name) {
			//all sub-resources come back to this root resource
			return this;
		}
	}

	@Override
	public void in(BaseContext context) {
		Exchange exchange = cachedCoapExchangeMap.get(context.getKey());
		Response response = new Response(ResponseCode.CONTENT);
		response.setPayload(context.getContent());
		System.out.println("content: " + context.getContent());
		exchange.sendResponse(response);
		
	}

	@Override
	public void setNextSpoke(Object nextSpoke) {
		this.nextSpoke = (BaseSpoke)nextSpoke;
	}

	@Override
	public String getAddress() {
		// TODO Get full address of the spoke providers (scheme, url, port, path)
		return "coap://" + this.interfaceAddress + ":" + Integer.toString(this.getEndpoints().get(0).getAddress().getPort()) + "/";
	}

	@Override
	public void close() {
		this.destroy();
		
	}

	private int activity = 0;
	
	@Override
	public int getLastActivity() {
		// TODO Auto-generated method stub
		return activity;
	}

	@Override
	public void clearActivity() {
		// TODO Auto-generated method stub
		activity = 0; 
	}
	
}
