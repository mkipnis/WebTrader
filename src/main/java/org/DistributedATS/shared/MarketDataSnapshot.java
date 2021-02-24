/*
   Copyright (C) 2021 Mike Kipnis

   This file is part of DistributedATS, a free-software/open-source project
   that integrates QuickFIX and LiquiBook over OpenDDS. This project simplifies
   the process of having multiple FIX gateways communicating with multiple
   matching engines in realtime.
   
   Permission is hereby granted, free of charge, to any person obtaining a copy
   of this software and associated documentation files (the "Software"), to deal
   in the Software without restriction, including without limitation the rights
   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
   copies of the Software, and to permit persons to whom the Software is
   furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in all
   copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
   SOFTWARE.
*/

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
