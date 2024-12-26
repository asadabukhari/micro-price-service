package com.microprice;

public class PriceLevel {
	public static final byte SIDE_BID = 0;
	public static final byte SIDE_ASK = 1;
	
	public static final int ONE_BILLION = 1000_000_000;
	private byte level;
	private long bidPrice;
	private long askPrice;
	private int bidSize;
	private int askSize;
	
	public PriceLevel(byte level, double bidPrice, double askPrice, int bidSize, int askSize) {
		this.level = level;
		this.bidPrice = (long) (bidPrice * ONE_BILLION); 
		this.bidSize = bidSize;
		this.askPrice = (long) (askPrice * ONE_BILLION); 
		this.askSize = askSize;
	}
	
	public byte getLevel() {
		return level;
	}
	
	public long getBidPrice() {
		return bidPrice;
	}
	
	public long getAskPrice() {
		return askPrice;
	}
	
	public int getBidSize() {
		return bidSize;
	}
	
	public int getAskSize() {
		return askSize;
	}

	@Override
	public String toString() {
		return String.format("[%-1d:%-10d:%-4d:%12d:%-4d]", level,bidPrice,bidSize,askPrice,askSize);
	}
}
