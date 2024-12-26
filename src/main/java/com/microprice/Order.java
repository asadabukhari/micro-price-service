package com.microprice;

public class Order {
	private int securityId;
	private double price;
	private int size;
	private byte side;
	
	public Order(int id, double price, int qty, byte side) {
		this.securityId = id;
		this.price = price;
		this.size = qty;
		this.side = side;
	}
	
	public int getSecurityId() {
		return securityId;
	}
	public double getPrice() {
		return price;
	}
	public int getSize() {
		return size;
	}
	public byte getSide() {
		return side;
	}
}
