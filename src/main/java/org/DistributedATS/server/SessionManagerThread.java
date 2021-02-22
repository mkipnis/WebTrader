package org.DistributedATS.server;

import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;

import quickfix.Session;
import quickfix.SessionID;

//
// This thread disconnects FIX sessions if browser hasn't poll in a set amount of time
// TODO: remove had coded 30 seconds
//
public class SessionManagerThread implements Runnable {
	
	ConcurrentHashMap<SessionID, Timestamp> sessionPollTimestampMap = new ConcurrentHashMap<SessionID, Timestamp>();

	
	public void setSessionTimestamp(SessionID sessionID)
	{
		sessionPollTimestampMap.put( sessionID, new Timestamp(System.currentTimeMillis()) );
	};
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
	    try {
	      while (true) {

	    	  for (  SessionID sessionID : sessionPollTimestampMap.keySet() )
	    	  {
	    		  Timestamp lastTimestamp = sessionPollTimestampMap.get(sessionID);
	    		  	    		  
	    		  if ( (System.currentTimeMillis() - lastTimestamp.getTime()) > 30000 )
	    		  {
	    			    System.out.println("Browser with session id : " + sessionID + " has not polled in 30 seconds - Disconnecting");

	    			    Session session = Session.lookupSession(sessionID);
	    			    session.logout("Browser stopped polling");
	    			    sessionPollTimestampMap.remove(sessionID);
	    			    
	    		  }
	    	  }
	        
	        Thread.sleep(1000);
	      }
	    } catch (InterruptedException e) {
	      e.printStackTrace();
	    }

	}

}
