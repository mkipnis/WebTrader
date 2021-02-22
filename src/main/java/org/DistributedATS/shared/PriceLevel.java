package org.DistributedATS.shared;

import java.io.Serializable;

public class PriceLevel implements Serializable {
	
	public static int TICK_SIZE = 100;
	
	public PriceLevel()
	{
	}
	
	public PriceLevel( char side, Double price, Double size )
	{
		this.side = side;
		this.price = price/PriceLevel.TICK_SIZE;
		this.size = size;
	}
	
	public char getSide()
	{
		return side;
	}
	
	public Double getPrice()
	{
		return price;
	}
	
	public double getSize()
	{
		return size;
	}
	
	private Double price = new Double(0);
	private Double size = new Double(0); 
	private char side;

}
