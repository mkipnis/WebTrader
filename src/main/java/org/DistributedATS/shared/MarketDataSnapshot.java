package org.DistributedATS.shared;

import java.io.Serializable;
import java.util.HashMap;

public class MarketDataSnapshot implements Serializable {
	
	public MarketDataSnapshot()
	{
		
	}
	
	public void setLastTradedPrice( Double lastTradedPrice )
	{
		this.lastTradedPrice = lastTradedPrice/PriceLevel.TICK_SIZE;
	}
	
	public Double getLastTradedPrice()
	{
		return this.lastTradedPrice;
	}
	
	public void setVolume( Double volume )
	{
		this.volume = volume;
	}
	
	public Double getVolume()
	{
		return this.volume;
	}
	
	public void setOpenPrice( Double openPrice )
	{	
		this.openPrice = openPrice/PriceLevel.TICK_SIZE;
	}
	
	public Double getOpenPrice()
	{
		return this.openPrice;
	}
	
	public void insertBid( Integer level, PriceLevel priceLevel )
	{
		bidSide.put(level, priceLevel);
	}
	
	public void insertAsk( Integer level, PriceLevel priceLevel )
	{
		askSide.put(level, priceLevel);
	}
	
	public HashMap<Integer, PriceLevel> getBidSide()
	{
		return this.bidSide;
	}
	
	public HashMap<Integer, PriceLevel> getAskSide()
	{
		return this.askSide;
	}
	
	public void setMarketDataSequenceNumber( long marketDataSequenceNumber )
	{
		this.marketDataSequenceNumber = marketDataSequenceNumber;
	}
	
	public long getMarketDataSequenceNumber()
	{
		return this.marketDataSequenceNumber;
	}
	
	private long marketDataSequenceNumber;
	
	private Double lastTradedPrice = 0.0;
	private Double volume = 0.0;
	private Double openPrice = 0.0;
	private HashMap<Integer, PriceLevel> bidSide = new HashMap<Integer, PriceLevel>();
	private HashMap<Integer, PriceLevel> askSide = new HashMap<Integer, PriceLevel>();	

}
