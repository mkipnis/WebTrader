package org.DistributedATS.shared;

import java.io.Serializable;

import quickfix.SessionID;

public class PriceLevelKey implements Serializable {
	
	
	public PriceLevelKey()
	{
		
	}
	
	public PriceLevelKey( Instrument instrument, char bid_or_ask, int priceLevel )
	{
		this.instrument = instrument;
		this.bid_or_ask = bid_or_ask;
		this.priceLevel = priceLevel;
	}
	
	public Instrument getInstrument()
	{
		return this.instrument;
	}
	
	public int getPriceLevel()
	{
		return this.priceLevel;
	}
	
	@Override
	public boolean equals(Object o) 
	{
		if (this == o) return true;        
		if (o == null || getClass() != o.getClass()) 
			return false;
		
		PriceLevelKey priceLevelKey = (PriceLevelKey)o;
		if ( priceLevelKey.instrument.equals(this.instrument) &&
				priceLevelKey.bid_or_ask == this.bid_or_ask &&
				priceLevelKey.priceLevel == this.priceLevel )
			
			return true;
		
		return false;
	}
	
	@Override
    public int hashCode(){
        int hashcode = instrument.hashCode();
        hashcode += bid_or_ask;
        hashcode += priceLevel;
     
        return hashcode;
    }
	

	private Instrument instrument;
	private char bid_or_ask;
	int priceLevel;

}
