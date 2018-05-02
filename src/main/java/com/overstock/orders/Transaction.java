package com.overstock.orders;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Transaction {

	@JsonProperty("prc")
	private BigDecimal price;
	
	private int qty;
	
	public Transaction() {
		
	}
	
	public int getQty() {
		return qty;
	}
	
	public void setQty(int qty) {
		this.qty = qty;
	}
	
	public BigDecimal getPrice() {
		return price;
	}
	
	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return "Transaction qty " + this.getQty() + " price " + this.price;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((price == null) ? 0 : price.hashCode());
		result = prime * result + qty;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaction other = (Transaction) obj;
		if (price == null) {
			if (other.price != null)
				return false;
		} else if (!price.equals(other.price))
			return false;
		if (qty != other.qty)
			return false;
		return true;
	}
	
	
	
	
}
