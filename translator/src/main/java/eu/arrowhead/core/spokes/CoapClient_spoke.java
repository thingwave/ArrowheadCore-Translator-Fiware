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

import java.net.UnknownHostException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import eu.arrowhead.core.translator.BaseContext;

public class CoapClient_spoke implements BaseSpokeConsumer {

	BaseSpoke nextSpoke;
	String serviceAddress = ""; //"coap://127.0.0.1:5692/";
	
	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
	
	public CoapClient_spoke(String serviceAddress) throws UnknownHostException {
//		super("");
		if(serviceAddress.startsWith("coap")) {
			this.serviceAddress = serviceAddress;
		} else {
			this.serviceAddress = "coap://" + serviceAddress;
		}
		
		CoapClient pingClient = new CoapClient(serviceAddress);
		pingClient.ping();
		
	}

	@Override
	public void in(BaseContext context) {

		//if the context has no error then
		
		//start a coap client worker
		new Thread(new Worker(context), "name").start();
		
	}

	@Override
	public void setNextSpoke(Object nextSpoke) {
		this.nextSpoke = (BaseSpoke)nextSpoke;
	}
	
		
	class Worker implements Runnable {
		
		BaseContext context = null;
		
		public Worker(BaseContext paramContext) {
			this.context = paramContext;
		}
		
		
		@Override
		public void run() {
			if(serviceAddress.endsWith("/") && context.getPath().startsWith("/")) {
				context.setPath(context.getPath().substring(1));
			}
			CoapClient client = new CoapClient(serviceAddress + context.getPath());
			
			CoapResponse response = null;
			long lStartTime = System.nanoTime();
			System.out.println(lStartTime + ": CoAP sending to: " + client.getURI());
			if(context.getMethod().equals("get")) {
				response = client.get();
			} else if (context.getMethod().equals("post")) {
				response = client.post(context.getContent(), MediaTypeRegistry.parse(context.getContentType()));
			} else if (context.getMethod().equals("put")) {
				response = client.put(context.getContent(), MediaTypeRegistry.parse(context.getContentType()));;
			} else if (context.getMethod().equals("delete")) {
				response = client.delete();
			}

			lStartTime = System.nanoTime();
			System.out.println(lStartTime + ": CoAP response recieved");
			
			if (response != null) {
				//System.out.println("Response from coap: " + response.getResponseText());
				context.setContent(response.getResponseText());
			} else {
				//TODO: need to signal an error to the next spoke
			}
			activity++;
			nextSpoke.in(context);
		}
	}

	public int activity = 0;
	
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
