package com.microprice;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.agrona.concurrent.UnsafeBuffer;

import com.integration.MicropriceIntegrationTestUtil;

/**
 * This service provides following: 1. It provides market data update messages
 * via multicast UPD. 2. It creates mock prices (ExchangeBRandomPricer) and
 * sends UDP multicast binary messages of variable length depending on number of
 * levels available
 */
public class MarketDataFeedServer {
	static final int HEADER_SIZE = 4;
	static final int BODY_FIXED_FIELD_SIZE = 6;
	static final int EXTENDED_HEADER_SIZE = BODY_FIXED_FIELD_SIZE + HEADER_SIZE;
	static final int BODY_REPEAT_GROUP_SIZE = 14;
	static final int BODY_SIZE = BODY_FIXED_FIELD_SIZE + (BODY_REPEAT_GROUP_SIZE * 10 * 2);
	static final int MESSAGE_SIZE = HEADER_SIZE + BODY_SIZE;
	static boolean testOnly = false;
	static List<String> messagesList = null;
	static int testSampleSize = 0;

	private MulticastSocket multicastSocket;
	private BlockingQueue<PriceLevels> blockingQueue;
	private ScheduledExecutorService scheduledExecutorService;
	private ExchangeBRandomPricer pricer;
	private int multicastPort;
	private String multicastAddress;
	private String networkInterfaceName;
	private NetworkInterface networkInterface;
	private InetAddress multicastGroup;
	private DatagramPacket multicastUDPPacket;
	private int packetCount = 0;

	public MarketDataFeedServer(String multicastGroupIp, String port, String interfaceName) {
		multicastAddress = multicastGroupIp;
		multicastPort = Integer.valueOf(port);
		networkInterfaceName = interfaceName;
		blockingQueue = new LinkedBlockingQueue<>();
		pricer = new ExchangeBRandomPricer(blockingQueue);
		scheduledExecutorService = Executors.newScheduledThreadPool(1);
		this.scheduledExecutorService.scheduleAtFixedRate(() -> pricer.sendPrices(), 5L, 30L, TimeUnit.MILLISECONDS);

		try {
			multicastSocket = new MulticastSocket(multicastPort);
			networkInterface = NetworkInterface.getByName(networkInterfaceName);
			if (networkInterface != null)
				multicastSocket.setNetworkInterface(networkInterface);
			multicastGroup = InetAddress.getByName(multicastAddress);
			multicastUDPPacket = new DatagramPacket(new byte[1], 1, multicastGroup, multicastPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void multicastSend(byte[] buffer) {
		try {
			multicastUDPPacket.setData(buffer);
			multicastUDPPacket.setLength(buffer.length);
			multicastSocket.send(multicastUDPPacket);
			//multicastUDPPacket.setLength(0);
			buffer = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void processForever() throws InterruptedException {
		while (true) {
			PriceLevels priceLevels = this.blockingQueue.take();
			int packetSize = priceLevels.getLevels() * 2 * BODY_REPEAT_GROUP_SIZE + EXTENDED_HEADER_SIZE;
			UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocate(packetSize));
			multicastSend(getBytes(priceLevels, buffer, packetSize));
			priceLevels.clear();
			if (testOnly && ++packetCount == testSampleSize) {
				pricer.markCompleted();
				scheduledExecutorService.shutdown();
				scheduledExecutorService.awaitTermination(3, TimeUnit.SECONDS);
				scheduledExecutorService.shutdownNow();
				break;
			}
		}
	}

	byte[] getBytes(PriceLevels priceLevels, UnsafeBuffer buffer, int bufferLen) {
		int securityId = priceLevels.getSecurityId();
		short levels = priceLevels.getLevels();
		int offset = 0;
		buffer.putInt(offset, bufferLen-4);
		offset += 4;
		buffer.putInt(offset, securityId);
		offset += 4;
		buffer.putShort(offset, levels);
		offset += 2;
		for (int i = 0; i < levels; i++) {
			PriceLevel levelPrice = priceLevels.getPriceLevel(i);

			// BID
			buffer.putByte(offset, levelPrice.getLevel());
			offset += 1;
			buffer.putByte(offset, PriceLevel.SIDE_BID);
			offset += 1;
			buffer.putLong(offset, levelPrice.getBidPrice());
			offset += 8;
			buffer.putInt(offset, levelPrice.getBidSize());
			offset += 4;

			// ASK
			buffer.putByte(offset, levelPrice.getLevel());
			offset += 1;
			buffer.putByte(offset, PriceLevel.SIDE_ASK);
			offset += 1;
			buffer.putLong(offset, levelPrice.getAskPrice());
			offset += 8;
			buffer.putInt(offset, levelPrice.getAskSize());
			offset += 4;
			levelPrice = null;
		}
		if (testOnly) messagesList.add(Base64.getEncoder().encodeToString(buffer.byteArray()));
		return buffer.byteArray();
	}

	public static void main(String[] args) throws InterruptedException {
		if (args.length < 3) {
			System.out.println("Please provide Multicast IP, Port, InterfaceName and Exchange-A host and port as arguments. Exiting...");
			System.exit(100);
		}
		if (args.length > 3) {
			testOnly = true;
			testSampleSize = Integer.valueOf(args[3]);
		}
		messagesList = testOnly ? new ArrayList<>(testSampleSize) : null;
		MarketDataFeedServer server = new MarketDataFeedServer(args[0], args[1], args[2]);
		server.processForever();
		
		if (testOnly)
			MicropriceIntegrationTestUtil.logMessages(messagesList, MicropriceIntegrationTestUtil.ExchangeB_MDFeedServer);
		
		System.out.println("Exiting...");
	}
}
