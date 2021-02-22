package org.DistributedATS.shared;

import java.io.Serializable;
import java.util.ArrayList;

public class SessionStateRequest implements Serializable {

	public String token = "";
	public int stateMask;
	public long maxOrderSequenceNumber;
	public long marketDataSequenceNumber;
	
	public ArrayList<Instrument> activeSecurityList = new ArrayList<Instrument>();
}
