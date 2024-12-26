package com.microprice;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.agrona.concurrent.UnsafeBuffer;

import com.integration.MicropriceIntegrationTestUtil;

/*
 * 1. receives the prices via Market Data Feed updates from Exchange B (MarketDataFeedServer).
 * 2. Works as a gateway to incoming binary data and translate to a object (OrderBook).
 * 3. Creates micro-price using incoming 10 levels of bids/asks but specifically using top of the book prices
 * 4. Send the micro-price to Exchange A via TCP socket call
 */
public class MicropriceMDHandler {
	
	public static final int ONE_BILLION = 1000_000_000;
	private static final int MULTICAST_PACKET_SIZE = 290;
	private MulticastSocket multicastSocket;
	private int multicastPort;
	private InetAddress multicastGroup;
	private DatagramPacket multicastPacket;
	
	private String exchangeA_Host;
	private int exchangeA_Port;
	private Socket exchangeA_tcpSocket;
	private DataOutputStream exchangeA_dataOutputStream;
	
	public MicropriceMDHandler(String multicastGroupIp, String multicastPort, String exchangeAHost, String exchangeAPort) {
        try {
        	this.multicastPort = Integer.valueOf(multicastPort);
        	multicastSocket = new MulticastSocket(this.multicastPort);
            multicastGroup = InetAddress.getByName(multicastGroupIp);
			multicastSocket.joinGroup(multicastGroup);
			multicastPacket = new DatagramPacket(new byte[MULTICAST_PACKET_SIZE], MULTICAST_PACKET_SIZE);
			
			exchangeA_Host = exchangeAHost;
	        exchangeA_Port = Integer.valueOf(exchangeAPort);
			exchangeA_tcpSocket = new Socket(exchangeA_Host, exchangeA_Port);
			exchangeA_dataOutputStream = new DataOutputStream(this.exchangeA_tcpSocket.getOutputStream());
			System.out.println("Connected to Exchange A TCP server: " + exchangeA_Host + " on port " + exchangeA_Port);
		} catch (IOException e) {
			e.printStackTrace();
		}
        System.out.println("Joined multicast group: " + multicastGroup);
	}
	
	public OrderBook translateBytesToOrderBook(UnsafeBuffer buffer) {
		int offset = 4;
        int securityId = buffer.getInt(offset);
        short levels = buffer.getShort(offset += 4);
        offset += 2;
        MarketByPriceOrderBook orderBook = new MarketByPriceOrderBook();
        for (int i = 0; i < levels; i++) {
			//BID
			//int bidLevel = buffer.getByte(offset); 
			byte bidSide = buffer.getByte(offset += 1); 
			long bidPrice = buffer.getLong(offset += 1); 
			int bidSize = buffer.getInt(offset += 8);
			Order bid = new Order(securityId, Math.round(100.00 * bidPrice/ONE_BILLION)/100.00, bidSize, bidSide);
			orderBook.appendBid(bid);
			
			//ASK
			//int askLevel = buffer.getByte(offset += 4);
			offset += 4;
			byte askSide = buffer.getByte(offset += 1);
			long askPrice = buffer.getLong(offset += 1);
			int askSize = buffer.getInt(offset += 8);
			offset += 4;
			Order ask = new Order(securityId, Math.round(100.00 * askPrice/ONE_BILLION)/100.00, askSize, askSide);
			orderBook.appendAsk(ask);
		}
        return orderBook;
	}
	
	public UnsafeBuffer performMulticastReceive() throws IOException {
		multicastSocket.receive(multicastPacket);
        return new UnsafeBuffer(multicastPacket.getData());
	}
	
	void streamMircoPrice(int securityId, double microPrice) throws IOException {
		exchangeA_dataOutputStream.writeInt(securityId);
		exchangeA_dataOutputStream.writeLong((long) (ONE_BILLION * microPrice));
		exchangeA_dataOutputStream.flush();
	}
	
	public static void main(String[] args) {
		try {
			if (args.length < 4) {
				System.out.println("Needs multicast-feed-server-ip, port, exchangeA host and port as 4 arguments. Exiting...");
				System.exit(100);
			}
			MicropriceMDHandler marketDataHandler = new MicropriceMDHandler(args[0], args[1], args[2], args[3]);
			boolean testOnly = false;
			int testSampleSize = 0;
			int messageCount = 0;
			if (args.length > 4) {
				testOnly = true;
				testSampleSize = Integer.valueOf(args[4]);
			}
			
			List<String> exchangeAMessagesList = testOnly ? new ArrayList<>(testSampleSize) : null;
			List<String> mdMessagesList = testOnly ? new ArrayList<>(testSampleSize) : null;
			MicroPriceCalculator microPriceCalculator = new MicroPriceCalculator();
            while (true) {
            	UnsafeBuffer buffer = marketDataHandler.performMulticastReceive();
            	int securityId = buffer.getInt(4);
            	OrderBook orderBook = marketDataHandler.translateBytesToOrderBook(buffer);
            	double microPrice = microPriceCalculator.calculateMicroPrice(orderBook);
            	marketDataHandler.streamMircoPrice(securityId, microPrice);
            	((MarketByPriceOrderBook)(orderBook)).clear();
            	if (testOnly) {
            		//at index 0, we can get the size of the body + 4 for header
            		byte[] messageBytes = Arrays.copyOf(buffer.byteArray(), buffer.getInt(0) + 4);
            		mdMessagesList.add(Base64.getEncoder().encodeToString(messageBytes));
            		exchangeAMessagesList.add(securityId + String.valueOf(microPrice));
            		if (++messageCount == testSampleSize) break;
            	}
            }
            if (testOnly) {
            	MicropriceIntegrationTestUtil.logMessages(mdMessagesList, MicropriceIntegrationTestUtil.ExchangeB_MDFeedHandler);
            	MicropriceIntegrationTestUtil.logMessages(exchangeAMessagesList, MicropriceIntegrationTestUtil.ExchangeB_TCPData);
            }
			Thread.currentThread().sleep(12000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
		System.out.println("Exiting...");
		
	}

}
