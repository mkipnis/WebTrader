package org.DistributedATS.shared;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import org.DistributedATS.client.OrderHistoryCanvas;

public class Order implements Serializable, Comparable<Order> {
	
	
	public Order()
	{		
	}
	
	public String getSide()
	{
		if ( side == '1')
			return "Buy";
		else 
			return "Sell";
	};
	
	public void insertExecutionReport( String executionReportID, ExecutionReport executionReport, long sequenceNumber )
	{
		this.sequenceNumber = sequenceNumber;
		executionReports.put(executionReportID, executionReport);
	}
	
	public HashMap<String, ExecutionReport> getExecutionReports()
	{
		return executionReports;
	}
	
	public Instrument instrument;
	public double price;
	public double quantity;
	public double filled_quantity;
	public double filled_avg_price;
	public char side;
	public char orderType;
	public char status;
	public String lastExecutionReportId;
	public Date lastUpdateTime;
	
	public long sequenceNumber; // reference number between client and a server, so that client get only latest updated(updates since last sequnce)
	
	private HashMap<String, ExecutionReport> executionReports = new  HashMap<String, ExecutionReport>();

	public OrderKey orderKey;	

	@Override
	public int compareTo(Order o) {
		// TODO Auto-generated method stub
		return (int)( this.sequenceNumber - o.sequenceNumber );
	}

}
