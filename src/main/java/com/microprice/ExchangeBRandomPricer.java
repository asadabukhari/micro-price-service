package com.microprice;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class ExchangeBRandomPricer {
	static final short DEFAULT_LEVEL_COUNT = 10;
	
	/** provides a range of min/max for the prices of securities used */
	private static Map<String, double[]> securityYearlyPriceRange = new HashMap<>();
	private BlockingQueue<PriceLevels> blockingQueue;
	private double defaultSpread = 0.0;
	private Random random = null;
	private boolean completed = false;
	
	public ExchangeBRandomPricer(BlockingQueue<PriceLevels> blockingQueue) {
		this.blockingQueue = blockingQueue;
		random = new Random();
		defaultSpread = 0.1;
		securityYearlyPriceRange.put("AAPL", new double[] {164.08, 255.00});
		securityYearlyPriceRange.put("NVDA", new double[] {47.32, 152.00});
		securityYearlyPriceRange.put("AMZN", new double[] {212.73, 226.00});
		securityYearlyPriceRange.put("TSLA", new double[] {417.64, 447.00});
		securityYearlyPriceRange.put("CME",  new double[] {190.73, 249.00});
		securityYearlyPriceRange.put("MSFT", new double[] {366.50, 460.00});
	}
	
	public void sendPrices() {
		try {
			if (completed) return;
			blockingQueue.put(createMockPrices("AAPL"));
			blockingQueue.put(createMockPrices("NVDA"));
			blockingQueue.put(createMockPrices("TSLA"));
			blockingQueue.put(createMockPrices("AMZN"));
			blockingQueue.put(createMockPrices("MSFT"));
			blockingQueue.put(createMockPrices("CME"));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void markCompleted() {
		this.completed = true;
	}

	private double getPriceForTicker(String ticker) {
		double[] range = securityYearlyPriceRange.get(ticker);
		double min = range[0], max = range[1];
		//creates random price within range(min, max)
		double refereceRandomPrice = min + (max - min) * random.nextDouble();
		return Math.round(refereceRandomPrice * 100)/100.00;
	}
	
	private PriceLevels createMockPrices(String ticker) {
		//start at top of the book (highest bid and lowest ask)
		double bid = getPriceForTicker(ticker);
		double ask = Math.round(100 *(bid + defaultSpread))/100.00;
		int bidSize = ThreadLocalRandom.current().nextInt(1, 5) * 100;
		int askSize = ThreadLocalRandom.current().nextInt(1, 5) * 100;
		int securityId = ticker.hashCode();
		//randomized level size (up to max 10)
		short levels = bidSize == 200 ? DEFAULT_LEVEL_COUNT : (short) (bidSize/100);
		PriceLevels priceLevels = new PriceLevels(levels, securityId, new PriceLevel((byte)0, bid, ask, bidSize, askSize));
		
		for (byte i = 1; i < levels; i++) {
			bid = Math.round(100 * (bid - 0.01))/100.00; ask = Math.round(100 *(ask + 0.01))/100.00;
			bidSize += bidSize > 600 ? -300 : 100; 
			askSize += askSize > 600 ? -300 : 100;
			priceLevels.append(new PriceLevel(i, bid, ask, bidSize, askSize));
		}
		return priceLevels;
	}
}
