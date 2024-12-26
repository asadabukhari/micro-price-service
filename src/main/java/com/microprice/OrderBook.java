package com.microprice;

public interface OrderBook {
	public int numLevels();
	
	public double bidPrice(int level);
	
	public int bidSize(int level);
	
	public double askPrice(int level);
	
	public int askSize(int level);
	
}
