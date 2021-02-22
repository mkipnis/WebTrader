package org.DistributedATS.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import quickfix.SessionID;

public class FIXUserSession implements Serializable {

	public static int STATE_UNABLE_TO_CREATE_SESSION = -1;
	public static int STATE_PENDING_LOGON = 0;
	public static int STATE_SUCCESSFUL_LOGIN = 1;
	//public static int STATE_INVALID_USERNAME_OR_PASSWORD = 2;
	public static int STATE_LOGGED_OUT = 3;
	
	public static int LOGON_STATE_BIT = 0;
	public static int SECURITY_LIST_BIT = 1;
	public static int MARKET_DATA_BIT = 2;
	public static int ORDERS_DATA_BIT = 4;
	
	public FIXUserSession()
	{
		
	};
	
	public String username;
	public String token;
	public String sessionStateText;
	public int sessionState;
	
	public long marketDataSequenceNumber = -1;
	
	public int stateMask;
	
	//public HashMap<Instrument, HashMap< Integer, PriceLevel > > priceDepthMap = new  HashMap<Instrument, HashMap< Integer, PriceLevel > >(); 
	public HashMap<OrderKey, Order> orders = new HashMap<OrderKey, Order>();
	public ArrayList<Instrument> activeSecurityList = new ArrayList<Instrument>();
	public HashMap<Instrument, MarketDataSnapshot > instrumentMarketDataSnapshot = new HashMap<Instrument, MarketDataSnapshot > ();
	public HashMap<Instrument, Position> positionsMap = new HashMap<Instrument, Position> ();
	
	
}
