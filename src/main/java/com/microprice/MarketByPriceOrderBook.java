package com.microprice;

import java.util.ArrayList;
import java.util.List;

public class MarketByPriceOrderBook implements OrderBook{
	List<Order> bids = null;
	List<Order> asks = null;
	
	public MarketByPriceOrderBook() {
		bids = new ArrayList<>();
		asks = new ArrayList<>();
	}
	
	public void appendBid(Order bid) {
		bids.add(bid);
	}
	
	public void appendAsk(Order ask) {
		asks.add(ask);
	}
	
	public void clear() {
		if (bids != null) bids.clear();
		if (asks != null) asks.clear();
		bids = asks = null;
	}

	@Override
	public int numLevels() {
		return 0;
	}

	@Override
	public double bidPrice(int level) {
		return bids.get(level).getPrice();
	}

	@Override
	public int bidSize(int level) {
		return bids.get(level).getSize();
	}

	@Override
	public double askPrice(int level) {
		return asks.get(level).getPrice();
	}

	@Override
	public int askSize(int level) {
		return asks.get(level).getSize();
	}
}
