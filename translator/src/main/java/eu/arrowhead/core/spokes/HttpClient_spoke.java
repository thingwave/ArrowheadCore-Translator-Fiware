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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import eu.arrowhead.core.translator.BaseContext;

public class HttpClient_spoke implements BaseSpokeConsumer {

	BaseSpoke nextSpoke;
	String serviceAddress = ""; //"http://127.0.0.1:5692/";
	
	public HttpClient_spoke(String serviceAddress) {
//		super("");

		System.out.println("address: " + serviceAddress);
		this.serviceAddress = serviceAddress;
		
	}
	
	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void in(BaseContext context) {
		// TODO perhaps modify this to a thread pool in order to track how many workers are running.
		new Thread(new Worker(context)).start();	
		
	}

	@Override
	public void setNextSpoke(Object nextSpoke) {
		// TODO Auto-generated method stub
		this.nextSpoke = (BaseSpoke) nextSpoke;
	}

	
	class Worker implements Runnable {
		
		BaseContext context = null;
		
		public Worker(BaseContext paramContext) {
			this.context = paramContext;
		}
		
		@Override
		public void run() {
			
			System.out.println("HttpClient Spoke sending Request");
			// get the requested host, if the port is not specified, the constructor
			// sets it to -1
			
			//Added path to access resources, not just root "/"
			String myurl = HttpClient_spoke.this.serviceAddress + "/" +this.context.getPath();
			System.out.println("To address:" + myurl);
			StringBuilder result = new StringBuilder();
			URL url;
			HttpURLConnection conn;
			boolean payloadExpected = false;
			try {
				url = new URL(myurl);
			
				// create the requestLine
				conn = (HttpURLConnection) url.openConnection();
						
				if (this.context.getMethod().equals("get")) {
					conn.setRequestMethod("GET");
					payloadExpected = false;
				} else if (this.context.getMethod().equals("post")) {
					conn.setRequestMethod("POST");
					payloadExpected = true;
				} else if (this.context.getMethod().equals("put")) {
					conn.setRequestMethod("PUT");
					payloadExpected = true;
				} else if (this.context.getMethod().equals("delete")) {
					conn.setRequestMethod("DELETE");
					payloadExpected = true;
				}
				
				//if there is a payload then add that to the request
				if(payloadExpected) {
					// 
					// create the content
					conn.setDoOutput(true);
					conn.setRequestProperty("content-type", "application/xml");
					OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
					writer.write(this.context.getContent());
					writer.flush();

										
				}
				
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line;
				while ((line = rd.readLine()) != null) { result.append(line); }
				rd.close();
				context.setContent(result.toString());
				
				
			} catch (ProtocolException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			// get the mapping to http for the incoming request
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
