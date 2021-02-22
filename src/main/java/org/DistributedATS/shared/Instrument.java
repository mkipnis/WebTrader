package org.DistributedATS.shared;

import java.io.Serializable;

public class Instrument implements Serializable {

	public Instrument()
	{
		
	}
	
	public Instrument( String securityExchange, String symbol )
	{
		this.securityExchange = securityExchange;
		this.symbol = symbol;
	}
	
	
	@Override
	public boolean equals(Object o) 
	{
		if (this == o) return true;        
		if (o == null || getClass() != o.getClass()) 
			return false;
		
		Instrument instrument = (Instrument)o;
		if ( instrument.securityExchange.equals(this.securityExchange) && instrument.symbol.equals(this.symbol) )
			return true;
		
		return false;
	}
	
	@Override
    public int hashCode(){
        int hashcode = symbol.hashCode();
        hashcode += securityExchange.hashCode();
     
        return hashcode;
    }
	
	public String getSymbol()
	{
		return symbol;
	}
	
	public String getSecurityExchange()
	{
		return securityExchange;
	}
	
	public String getInstrumentName()
	{
		return securityExchange+":"+symbol;
	}
	
	private String symbol;
	private String securityExchange;
	
}
