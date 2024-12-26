package com.microprice;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.integration.MicropriceIntegrationTestUtil;

public class ExchangeA_TCPServer {
	
	static final Map<Integer, Double> microPriceMap = new HashMap<>();

	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length == 0) {
			System.out.println("check args. Exiting...");
			System.exit(100);
		}
		ServerSocket serverSocket = new ServerSocket(Integer.valueOf(args[0]));
		System.out.println("Listening for micro-price service clients...");
	    Socket clientSocket = serverSocket.accept();
	    String clientSocketIP = clientSocket.getInetAddress().toString();
	    int clientSocketPort = clientSocket.getPort();
	    System.out.println("[IP: " + clientSocketIP + " ,Port: " + clientSocketPort +"]  " + "Client Connection Successful!");
	    
	    boolean testOnly = false;
	    int testSampleSize = 0;
	    int messageCount = 0;
	    if (args.length > 1) {
	    	testOnly = true;
	    	testSampleSize = Integer.valueOf(args[1]);
	    }
	    List<String> messagesList = testOnly ? new ArrayList<>(testSampleSize) : null;
	    DataInputStream micrpriceStream = new DataInputStream(clientSocket.getInputStream());
	    
		while (true) {
            int securityId = micrpriceStream.readInt();
            double microPrice  = Math.round(100.00 * micrpriceStream.readLong()/1000_000_000)/100.00;
            
            System.out.println("securityId: "+ securityId + "; microPrice: "+microPrice);
            if (testOnly) messagesList.add(securityId + String.valueOf(microPrice));
            microPriceMap.put(securityId, microPrice);
            if (testOnly && ++messageCount == testSampleSize) {
            	MicropriceIntegrationTestUtil.logMessages(messagesList, MicropriceIntegrationTestUtil.ExchangeA_TCPServer);
            	Thread.currentThread().sleep(20L);
            	break;
            }
		}
		System.out.println("Exiting...");
	}

}
