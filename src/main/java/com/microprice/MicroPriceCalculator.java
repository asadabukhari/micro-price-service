package com.microprice;



public class MicroPriceCalculator {
	
	public MicroPriceCalculator() {
	}

	public double calculateMicroPrice(OrderBook orderBook) {
		if (orderBook == null) return 0.0;
		double bestBidPrice = orderBook.bidPrice(0), bestAskPrice = orderBook.askPrice(0);
		int bidSize = orderBook.bidSize(0), askSize = orderBook.askSize(0);
		//VWAP price
		return Math.round(100.00 * (bidSize * bestBidPrice + askSize * bestAskPrice)/(bidSize + askSize))/100.00;
	}

}
