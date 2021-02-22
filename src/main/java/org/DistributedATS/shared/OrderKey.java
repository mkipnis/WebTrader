package org.DistributedATS.shared;

import java.io.Serializable;

public class OrderKey implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static long _order_index = 0;
	
	public static OrderKey getNextOrderID(Instrument instrument)
	{
		return new OrderKey(instrument);
	}

	public OrderKey()
	{
	};
	
	public OrderKey( String orderID )
	{
		this.orderID = orderID;
	};
	
	private OrderKey( Instrument instrument )
	{
		orderID = new Long(System.currentTimeMillis()/1000).toString() + ":" + instrument.getInstrumentName() + ":" + ++_order_index;
	}
	
	public String getOrderKey()
	{
		return orderID;
	}
	
	@Override
	public boolean equals(Object o) 
	{
		if (this == o) return true;        
		if (o == null || getClass() != o.getClass()) 
			return false;
		
		OrderKey orderKey = (OrderKey)o;
		if ( orderKey.orderID.equals(this.orderID)  )
			return true;
		
		return false;
	}
	
	@Override
    public int hashCode(){
		
        int hashcode = orderID.hashCode();
     
        return hashcode;
    }
	
	private String orderID;
	
}
