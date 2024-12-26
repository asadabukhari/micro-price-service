package com.microprice;

import java.util.ArrayList;
import java.util.List;

public class PriceLevels {
	private int securityId;
	private short levels;
	private List<PriceLevel> priceLevels;
	
	public PriceLevels(short levels, int securityId) {
		this.levels = levels;
		this.securityId = securityId;
		priceLevels = new ArrayList<>();
	}
	
	public PriceLevels(short levels, int securityId, PriceLevel price) {
		this(levels, securityId);
		append(price);
	}
	
	public int getSecurityId() {
		return securityId;
	}
	
	public short getLevels() {
		return levels;
	}
	
	public PriceLevel getPriceLevel(int level) {
		return priceLevels.get(level);
	}
	
	public void append(PriceLevel levelPrice) {
		priceLevels.add(levelPrice);
	}
	
	public void clear() {
		if (priceLevels == null) return;
		int i = 0;
		priceLevels.clear();
		priceLevels = null;
	}
	
	@Override
	public String toString() {
		return String.valueOf(securityId) + ", "+ levels + ", " + priceLevels.toString();
	}
}
