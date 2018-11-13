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

package eu.arrowhead.core.translator;


import eu.arrowhead.core.spokes.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.util.SubnetUtils;


public class TranslatorHub extends Observable {//implements Runnable {

	public static Properties properties;
	public boolean noactivity = false;
	private int translatorId = 0;
	
	BaseSpokeProvider pSpoke;
	private String  pSpoke_ConsumerName = null;
	private String  pSpoke_ConsumerType = null;
	private String  pSpoke_ConsumerAddress = null;
	private String	pSpokeAddress = null;
	private String	pSpokeIp = null;
	private String	pSpokePort = null;
//	private String 	pSpokePath;		//translationPath;// 	= "*";

	BaseSpokeConsumer cSpoke;
	private String  cSpoke_ProviderName = null;
	private String  cSpoke_ProviderType = null;
	private String  cSpoke_ProviderAddress = null;
//	private String  cSpoke_ProviderPath;
	
	public TranslatorHub(Observer observer) {
		//loadProperties("translator.properties");
		this.addObserver(observer);	
		
		SpokeActivityMonitor activityMonitor = new SpokeActivityMonitor();
		new Thread(activityMonitor).start();
		
	}
	

	class SpokeActivityMonitor implements Runnable {
		
		int counter = 0;
		
		public SpokeActivityMonitor() {
			
		}
		
		@Override
		public void run() {
			while(true) {
				// Start a timer
				try {
					Thread.sleep(60000);//60,000 ms = 1minute
					//Thread.sleep(10000); //10,000 ms = 10 seconds for testing
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if ( (cSpoke.getLastActivity() > 0) || (pSpoke.getLastActivity() > 0) ) {
					cSpoke.clearActivity();
					pSpoke.clearActivity();
					counter = 0;
				} else if (counter < 1){// else if (activity grace period not over)
					counter++;//count missed activity
				} else {// close the hub and remove from list in translator service
					noactivity = true;
					
					pSpoke.close();
					cSpoke.close();
					
					//request translation service to release reference to the hub.
					TranslatorHub.this.setChanged();
					TranslatorHub.this.notifyObservers(TranslatorHub.this.getTranslatorId());
					
					return;
				}
			}
		}
	}
	
	
	private static String findOutgoingIpForGivenAdress(String remoteIP) {

        if(System.getProperty("os.name").contains("Windows")) {
    	    final String COMMAND = "route print -4";
    	    List<RouteInfo> routes = new ArrayList<>();
		    try {
		        Process exec = Runtime.getRuntime().exec(COMMAND);
		        BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
	
		        System.out.println(System.getProperty("os.name"));
		        String line;
		        /* examples:
		                0.0.0.0          0.0.0.0     10.172.180.1    10.172.180.36     20
		                0.0.0.0          0.0.0.0      10.187.20.1    10.187.20.225     25
		           10.172.180.0    255.255.255.0         On-link     10.172.180.36    276
		          10.172.180.36  255.255.255.255         On-link     10.172.180.36    276
		        */
		        Pattern p = Pattern.compile("^\\s*(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+\\S+?\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+(\\d+)\\s*$");
		        while ((line = reader.readLine()) != null) {
		            Matcher match = p.matcher(line);
		            if (match.matches()) {
		                String network = match.group(1);
		                String mask = match.group(2);
		                String address = match.group(3);
		                short maskLength = 0;
		                boolean networkMatch = network.contentEquals("0.0.0.0");
		                
		                if (!networkMatch) {
		                    SubnetUtils subnet = new SubnetUtils(network, mask);
		                    SubnetUtils.SubnetInfo info = subnet.getInfo();
		                    networkMatch = info.isInRange(remoteIP);
		                    maskLength = Short.valueOf(info.getCidrSignature().split("/")[1]);
		                }
		                
		                if (networkMatch) {
		                    short metric = Short.valueOf(match.group(4));
		                    routes.add(new RouteInfo(address, (short)0, maskLength, metric));
		                }
		                
		            }
		        }
		        Collections.sort(routes);
		        for (RouteInfo route : routes) {
		        }
		        
		        if (!routes.isEmpty()) return routes.get(0).source;
	
	
		    } catch (Exception ex) {
		        ex.printStackTrace();
		    }
        } else if (System.getProperty("os.name").contains("Linux")) {

    	    List<RouteInfo> routes = new ArrayList<>();
		    try {
	        	//ipv6 ^(.+)/(\d+)\s+(.+)\s(\d+)\s+(\d+)\s+(\d)\s+(.+)$
		    	//ipv4 ^\s+inet\s\addr:(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})\s+Bcast:(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})\s+Mask:(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})$
		    	//linux route get command parsing: ipv4 ^.*via.*\s+dev\s+.*\s+src\s((?:[0-9\.]{1,3})+)
		    	//linux route get comand parsing: ipv6 ^.*\sfrom\s::\svia.*\sdev\s.*\ssrc\s((?:[:]{1,2}|[0-9|a|b|c|d|e|f]{1,4})+)
	        	//final String COMMAND = "/sbin/ifconfig";
		    	final String COMMAND = "ip route get " + remoteIP;
	        	
				Process exec = Runtime.getRuntime().exec(COMMAND);
				BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));

		        System.out.println(System.getProperty("os.name"));
		        String line;
		        /* Original examples:
		         * 10.10.2.130 via 10.0.2.2 dev eth0  src 10.0.2.255
		         * Original pattern:
		         * Pattern p = Pattern.compile("^.*via.*\\s+dev\\s+.*\\s+src\\s((?:[0-9|\\.]{1,3})+)");
		         *
		         * New example:
		         * 10.10.2.130 via 130.240.172.1 dev wlp58s0 src 130.240.174.24 uid 1000
		         * Fail to match due to the end "uid 1000"
		         * New pattern:
		         * Pattern p = Pattern.compile("^.*via.*\\s+dev\\s+.*\\s+src\\s((?:[0-9|\\.]{1,3})+).*");
		         *
		         * New pattern works with the absolut address of own computer:
		         */
		        Pattern p = Pattern.compile("^.*dev\\s+.*\\s+src\\s((?:[0-9|\\.]{1,3})+).*");

		        while ((line = reader.readLine()) != null) {
		            Matcher match = p.matcher(line);
		            if (match.matches()) {

		                String address = match.group(1);
		                
		                routes.add(new RouteInfo(address, (short)0, (short)0, (short)0));//metric is always 0, because we do not extract it from the ifconfig command.
		                
		            }
		        }
		        Collections.sort(routes);
		        
		        if (!routes.isEmpty()) return routes.get(0).source;
	
	
		    } catch (Exception ex) {
		        ex.printStackTrace();
		    }
        }
	    return null;
	}
	
	private static String findOutgoingIpV6GivenAddress(String remoteIP) throws Exception {

    	System.out.println("remoteIP: " + remoteIP);
		if (remoteIP.startsWith("[")) {
			remoteIP = remoteIP.substring(1, remoteIP.length()-1);
	    	System.out.println("remoteIP: " + remoteIP);
		}
		
        if(System.getProperty("os.name").contains("Windows")) {
			NetworkInterface networkInterface;
			
			//if ipv6 then find the ipaddress of the network interface
		    String line;
		    short foundIfIndex = 0;
			final String COMMAND = "route print -6";
			List<RouteInfo> routes = new ArrayList<>();
			try {
				Process exec = Runtime.getRuntime().exec(COMMAND);
				BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
				//\s+addr:(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})\s+Bcast:(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})\s+Mask:(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})$
				Pattern p = Pattern.compile("^\\s*(\\d+)\\s+(\\d+)\\s+(.*)\\s+(.*)$");
		        while ((line = reader.readLine()) != null) {
		        	//do not check persistent routes. Only active routes
		        	if(line.startsWith("Persistent Routes")) {
		        		break;
		        	}
		        	
		            Matcher match = p.matcher(line);
		            if (match.matches()) {
	//		            	System.out.println("line match: " + line);
		                String network = match.group(3).split("/")[0];
		                //String mask = match.group(2);
		                //String address = match.group(3);
		                short maskLength = Short.valueOf(match.group(3).split("/")[1].trim());
		                
		                boolean networkMatch = ipv6InRange(network, maskLength, remoteIP);
		                
		                if (networkMatch) {
			                short interfaceIndex = Short.valueOf(match.group(1));
		                    short metric = Short.valueOf(match.group(2));
		                    routes.add(new RouteInfo("", interfaceIndex, maskLength, metric));
		                    System.out.println("added route: " + line);
		                }
		            }
		        }
		        Collections.sort(routes);
		        for (RouteInfo route : routes) {
		        }
		        
		        if (!routes.isEmpty()) {
		        	foundIfIndex = routes.get(0).ifIndex;
			
					//based o nthe network interface index get the ip address
					networkInterface = NetworkInterface.getByIndex(foundIfIndex);
				
					Enumeration<InetAddress> test = networkInterface.getInetAddresses();
					
					while (test.hasMoreElements()) {
						InetAddress inetaddress = test.nextElement();
						if (inetaddress.isLinkLocalAddress()) {
							continue;
						} else {
							if (inetaddress instanceof Inet6Address) {
								System.out.println("interface host address: " + inetaddress.getHostAddress());
								return "[" + inetaddress.getHostAddress() + "]";
							} else continue;
						}
					}
		        } else { //routes is Empty!
		        	System.out.println("No routable interface for remote IP");
					throw new Exception("No routable interface for remote IP");
		        }
			} catch (Exception ex) {
				ex.printStackTrace();
				throw ex;
			}
        } else {
        	
        	List<RouteInfo> routes = new ArrayList<>();
		    try {
	        	//ipv6 ^(.+)/(\d+)\s+(.+)\s(\d+)\s+(\d+)\s+(\d)\s+(.+)$
		    	//ipv4 ^\s+inet\s\addr:(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})\s+Bcast:(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})\s+Mask:(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})$
		    	//linux route get command parsing: ipv4^.*via.*\s+dev\s+.*\s+src\s((?:[0-9\.]{1,3})+)
		    	//linux route get comand parsing: ipv6 ^.*\sfrom\s::\svia.*\sdev\s.*\ssrc\s((?:[:]{1,2}|[0-9|a|b|c|d|e|f]{1,4})+)
		    	//new one ^.*\s+from\s+::\s+via.*\s+dev\s+.*\ssrc\s+((?:[:]{1,2}|[0-9|a|b|c|d|e|f]{1,4})+)\s+metric\s+\d+
	        	//final String COMMAND = "/sbin/ifconfig";
		    	final String COMMAND = "ip route get " + remoteIP;
		    	
		    	System.out.println("command = " + COMMAND);
	        	
				Process exec = Runtime.getRuntime().exec(COMMAND);
				BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));

		        System.out.println(System.getProperty("os.name"));
		        String line;
		        /* examples:
		         * fdfd:55::98ac from :: via fdfd:55::98ac dev usb0  src fdfd:55::80fe  metric 0
		        */
		        Pattern p = Pattern.compile("^.*\\s+from\\s+::\\s+via.*\\sdev\\s+.*\\s+src\\s+((?:[:]{1,2}|[0-9|a|b|c|d|e|f]{1,4})+)\\s+metric\\s+\\d+.*");
		        //String test = "fdfd:55::80ff from :: via fdfd:55::80ff dev usb0  src fdfd:55::80fe  metric 0";
		        while ((line = reader.readLine()) != null) {
		        	System.out.println("result of command = " + line);
		            Matcher match = p.matcher(line);
		            if (match.matches()) {

		                String address = match.group(1);
		                System.out.println("match found. address = " + address);
		                routes.add(new RouteInfo(address, (short)0, (short)0, (short)0));//metric is always 0, because we do not extract it from the ifconfig command.
		                
		            }
		        }
		        Collections.sort(routes);
		        
		        if (!routes.isEmpty()) return routes.get(0).source;
	
	
		    } catch (Exception ex) {
		        ex.printStackTrace();
		    }
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	//^\s+inet6\s+addr:\s+((?:[:]{1,2}|[0-9|a|b|c|d|e|f]{1,4})+)/(\d{1,3})\s+\Scope:Global$
//        	NetworkInterface networkInterface;
//			
//			//if ipv6 then find the ipaddress of the network interface
//		    String line;
//		    short foundIfIndex = 0;
//			final String COMMAND = "/sbin/ifconfig";
//			List<RouteInfo> routes = new ArrayList<>();
//			try {
//				Process exec = Runtime.getRuntime().exec(COMMAND);
//				BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
//				//\s+addr:(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})\s+Bcast:(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})\s+Mask:(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})$
//				Pattern p = Pattern.compile("^\\s+inet6\\s+addr:\\s+((?:[:]{1,2}|[0-9|a|b|c|d|e|f]{1,4})+)/(\\d{1,3})\\s+\\Scope:Global$");
//		        while ((line = reader.readLine()) != null) {
//		        	//do not check persistent routes. Only active routes
//		        	if(line.startsWith("Persistent Routes")) {
//		        		break;
//		        	}
//		        	
//		            Matcher match = p.matcher(line);
//		            if (match.matches()) {
//		                String network = match.group(1).trim();
//		                short maskLength = Short.valueOf(match.group(2).trim());
//		                
//		                boolean networkMatch = ipv6InRange(network, maskLength, remoteIP);
//		                
//		                if (networkMatch) {
//			                short interfaceIndex = Short.valueOf(match.group(1));
//		                    short metric = Short.valueOf(match.group(2));
//		                    routes.add(new RouteInfo("", interfaceIndex, maskLength, metric));
//		                    System.out.println("added route: " + line);
//		                }
//		            }
//		        }
//		        Collections.sort(routes);
//		        for (RouteInfo route : routes) {
//		        }
//		        
//		        if (!routes.isEmpty()) {
//		        	foundIfIndex = routes.get(0).ifIndex;
//			
//					//based o nthe network interface index get the ip address
//					networkInterface = NetworkInterface.getByIndex(foundIfIndex);
//				
//					Enumeration<InetAddress> test = networkInterface.getInetAddresses();
//					
//					while (test.hasMoreElements()) {
//						InetAddress inetaddress = test.nextElement();
//						if (inetaddress.isLinkLocalAddress()) {
//							continue;
//						} else {
//							if (inetaddress instanceof Inet6Address) {
//								System.out.println("interface host address: " + inetaddress.getHostAddress());
//								return "[" + inetaddress.getHostAddress() + "]";
//							} else continue;
//						}
//					}
//		        } else { //routes is Empty!
//		        	System.out.println("No routable interface for remote IP");
//					throw new Exception("No routable interface for remote IP");
//		        }
//			} catch (Exception ex) {
//				ex.printStackTrace();
//				throw ex;
//			}
        	
        }
		
	    return null;
	}
	
	public static boolean ipv6InRange(String localIp, int localMask, String remoteIp) {
        InetAddress remoteAddress = parseAddress(remoteIp);
        InetAddress requiredAddress = parseAddress(localIp);
        int nMaskBits = localMask;

        if (!requiredAddress.getClass().equals(remoteAddress.getClass())) {
            return false;
        }

        if (nMaskBits <= 0) {
            return remoteAddress.equals(requiredAddress);
        }

        byte[] remAddr = remoteAddress.getAddress();
        byte[] reqAddr = requiredAddress.getAddress();

        int oddBits = nMaskBits % 8;
        int nMaskBytes = nMaskBits/8 + (oddBits == 0 ? 0 : 1);
        byte[] mask = new byte[nMaskBytes];

        Arrays.fill(mask, 0, oddBits == 0 ? mask.length : mask.length - 1, (byte)0xFF);

        if (oddBits != 0) {
            int finalByte = (1 << oddBits) - 1;
            finalByte <<= 8-oddBits;
            mask[mask.length - 1] = (byte) finalByte;
        }

 //       System.out.println("Mask is " + new sun.misc.HexDumpEncoder().encode(mask));

        for (int i=0; i < mask.length; i++) {
            if ((remAddr[i] & mask[i]) != (reqAddr[i] & mask[i])) {
                return false;
            }
        }

        return true;
    }
	private static InetAddress parseAddress(String address) {
        try {
            return InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Failed to parse address" + address, e);
        }
    }
	
	
	// spoke is responsible for translating from protocol specific domain to a generic domain
	// hub is used for chaining spokes in series.
	// the chain must start with a baseprovider and finish with a baseconsumer
	// each spoke in the chain has a generic interface
	
	
	public void online() throws Exception {
		//find the network interface or the ipaddress (IPv4)
		String source = findOutgoingIpForGivenAdress(this.getpSpoke_ConsumerAddress());
		if(source == null) {
			source = findOutgoingIpV6GivenAddress(this.getpSpoke_ConsumerAddress());
		}
		
		
		System.out.println("interface host address: " + source);
		

		try {

			System.out.println("go online: ConsumerSpoke: " + cSpoke_ProviderName + " ProviderSpoke: " + pSpoke_ConsumerName);
			
			if ( pSpoke_ConsumerType.contains("coap") ) {
				//pSpoke = new CoapServer_spoke(properties.getProperty("translator.interface.ipaddress"));
				pSpoke = new CoapServer_spoke(source);
			} else if (pSpoke_ConsumerType.contains("http")) {
				//HttpServer_spoke httpserver = new HttpServer_spoke(translationPort, translationPath);
				//pSpoke = new HttpServer_spoke(properties.getProperty("translator.interface.ipaddress"), "/*");
				pSpoke = new HttpServer_spoke(source, "/*");
//			}  else if (pSpoke_ConsumerType.contains("ua")) {
			}  else if (pSpoke_ConsumerType.contains("opc")) {
				//pSpoke = new UaServer_spoke(properties.getProperty("translator.interface.ipaddress"), "/*");
				//pSpoke = new UaServer_spoke(source, "/*");
			} else if (pSpoke_ConsumerType.contains("mqtt")) {
				String brokerUri = "tcp://localhost:1883";//TODO: get these values from ORCHESTRaTOR OR METADATA
				int maxPublishDelay = 1000; //1 second
				boolean MqttServer_spoke = true;
				//home/garden/fountain
				String serviceProviderPath = "/home/garden/fountain";
				//pSpoke = new MqttServer_spoke(brokerUri, serviceProviderPath, maxPublishDelay, MqttServer_spoke);
			} else {
			
				System.exit(1);
			}
			
			if(cSpoke_ProviderType.contains("coap")) {
				cSpoke = new CoapClient_spoke(cSpoke_ProviderAddress);
			} else if(cSpoke_ProviderType.contains("http")) {
				cSpoke = new HttpClient_spoke(cSpoke_ProviderAddress);
			} else if(cSpoke_ProviderType.contains("mqtt")) {
//				String brokerUri = "tcp://localhost:1883";//TODO: get these values from ORCHESTRaTOR OR METADATA
				//cSpoke = new MqttClient_spoke(cSpoke_ProviderAddress);
//			} else if(cSpoke_ProviderType.contains("ua")) {
			} else if(cSpoke_ProviderType.contains("opc")) {
				//cSpoke = new UaClient_spoke(cSpoke_ProviderAddress);
			} else {
				System.exit(1);
			}			
			
			// link the spoke connections 
			pSpoke.setNextSpoke(cSpoke);
			cSpoke.setNextSpoke(pSpoke);

			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					System.out.println("===END===");
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
				}
			});
			
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	public int getTranslatorId() {
		return translatorId;
	}

	public void setTranslatorId(int translatorId) {
		this.translatorId = translatorId;
	}

	public String getPSpoke_ConsumerName() {
		return pSpoke_ConsumerName;
	}

	public void setPSpoke_ConsumerName(String pSpoke_ConsumerName) {
		this.pSpoke_ConsumerName = pSpoke_ConsumerName;
	}

	public String getCSpoke_ProviderName() {
		return cSpoke_ProviderName;
	}

	public void setCSpoke_ProviderName(String cSpoke_ProviderName) {
		this.cSpoke_ProviderName = cSpoke_ProviderName;
	}
	
	//TODO must be composed as url = scheme://ip:port/path or scheme://ip:port/topic or XMPP??? or OPC-UA??? 
	public String getPSpokeAddress() {
		if (pSpokeAddress == null) {
			String address = this.pSpoke.getAddress();
			this.setPSpokeAddress(address);
		}
		return pSpokeAddress;
	}	
	private void setPSpokeAddress(String pSpokeAddress) {
		this.pSpokeAddress = pSpokeAddress;
	}
	// 
	public String getPSpokeIp() {
		if (pSpokeIp == null) {
			String temp = this.pSpoke.getAddress();
			temp = temp.substring(temp.indexOf("//") + 2);
			if(temp.startsWith("[")) {
				temp = temp.substring(0, temp.indexOf("]") + 1);
			} else {
				temp = temp.substring(0, temp.indexOf(":"));
			}
			String pSpokeIp = temp;
			this.setPSpokeIp(pSpokeIp);
		}
		return pSpokeIp;
	}	
	private void setPSpokeIp(String pSpokeIp) {
		this.pSpokeIp = pSpokeIp;
	}
	// 
	public String getPSpokePort() {
		if (pSpokePort == null) {
			String temp = this.pSpoke.getAddress();
			temp = temp.substring(temp.indexOf("//") + 2);
			if(temp.startsWith("[")) {
				temp = temp.substring(temp.indexOf("]") + 2);
				temp = temp.substring(0, temp.indexOf("/"));
			} else {
				temp = temp.substring(temp.indexOf(":") + 1);
			}
			
			String pSpokePort = temp;
			
			this.setPSpokePort(pSpokePort);
		}
		return pSpokePort;
	}
	
	private void setPSpokePort(String pSpokePort) {
		this.pSpokePort = pSpokePort;
	}

//	public String getPSpokePath() {
//		return pSpokePath;
//	}
//
//	public void setPSpokePath(String pSpokePath) {
//		this.pSpokePath = pSpokePath;
//	}

	public String getCSpoke_ProviderAddress() {
		return cSpoke_ProviderAddress;
	}

	public void setCSpoke_ProviderAddress(String cSpoke_ProviderAddress) {
		this.cSpoke_ProviderAddress = cSpoke_ProviderAddress;
	}
	
	public String getpSpoke_ConsumerAddress() {
		return pSpoke_ConsumerAddress;
	}

	public void setpSpoke_ConsumerAddress(String pSpoke_ConsumerAddress) {
		this.pSpoke_ConsumerAddress = pSpoke_ConsumerAddress;
	}
	
	public String getcSpoke_ProviderType() {
		return cSpoke_ProviderType;
	}

	public void setcSpoke_ProviderType(String cSpoke_ProviderType) {
		this.cSpoke_ProviderType = cSpoke_ProviderType;
	}
	
	public String getpSpoke_ConsumerType() {
		return pSpoke_ConsumerType;
	}

	public void setpSpoke_ConsumerType(String pSpoke_ConsumerType) {
		this.pSpoke_ConsumerType = pSpoke_ConsumerType;
	}
	
	public void closeProvider(String msg) {
		pSpoke.close();
	}
	
	public void closeConsumer(String msg) {
		cSpoke.close();
	}

	/** 
	 * Reads the properties from the file .properties
	 *
	 */
	private static boolean loadProperties(String propertiesFileName) {
		boolean result = false;
		/* Setting up Globals */
		String fileName = propertiesFileName;
		
		/* Read the input properties file and set the properties */
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(fileName));
			properties = props;
			result = true;
		} catch (IOException e) {
			//LOG.severe("Failed to read property file " + fileName + ". Reason: " + e.getMessage());
			System.out.println("Failed to read property file " + fileName + ". Reason: " + e.getMessage());
			//System.exit(-1);
		}
		return result;
	}
	
}

class RouteInfo implements Comparable<RouteInfo> {
    public final String source;
    public final short ifIndex;
    public final short maskLength;
    public final short metric;

    public RouteInfo(String src, short interfaceIndex, short mask, short mtr) {
        source = src;
        ifIndex = interfaceIndex;
        maskLength = mask;
        metric = mtr;
    }

    @Override
    public int compareTo(RouteInfo ri) {
        if (ri.maskLength != maskLength)
            return ri.maskLength - maskLength;
        return metric - ri.metric;
    }
}
