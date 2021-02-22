package org.DistributedATS.shared;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class ExecutionReport implements Serializable {
	
	public static char NEW = '0';
	public static char PARTIALLY_FILLED = '1';
	public static char FILLED = '2';
	public static char PENDING_CANCEL = '6';
	public static char CANCELLED = '4';
	public static char PENDING_NEW = 'A';
	public static char REJECTED = '8';
	
	

	static public HashMap<Character, String> statusReportMap = new  HashMap<Character, String>();

	static {
		statusReportMap.put(ExecutionReport.PENDING_NEW, "Pending New");
		statusReportMap.put(ExecutionReport.NEW, "New");
		statusReportMap.put(ExecutionReport.PARTIALLY_FILLED, "Partially Filled");
		statusReportMap.put(ExecutionReport.FILLED, "Filled");
		statusReportMap.put(ExecutionReport.PENDING_CANCEL, "Pending Cancel");
		statusReportMap.put(ExecutionReport.CANCELLED, "Cancelled");
		statusReportMap.put(ExecutionReport.REJECTED, "Rejected");
	}

	
	static public String getStatusText( char status )
	{
		String orderStatus = statusReportMap.get( status );
		
		if ( orderStatus == null )
			return "Other";
		else
			return orderStatus;
	}

	
	public String executionReportId;
	public double cumFilledQty;
	public double filledAvgPx; 
	public double fillPrice;
	public double fillQty;
	public double leavedQty;
	public Date updateTime;
	public char status;
	
}
