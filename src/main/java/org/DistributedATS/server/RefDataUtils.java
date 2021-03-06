package org.DistributedATS.server;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.DistributedATS.shared.ConvertUtils;
import org.DistributedATS.shared.Instrument;
import org.json.JSONObject;

public class RefDataUtils {
	
	public static boolean poulateRefData( Instrument instrument, String ref_data_text )
	{
		 try {
			 
	          SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

			 
       	      // TODO: deal with unset data 
       		  JSONObject ref_data = new JSONObject(ref_data_text);
         
       		  instrument.setCusip( ref_data.getString("cusip") );
       		  instrument.setIssueDate( ConvertUtils.dateToInt(
       				  formatter.parse( ref_data.getString("issue_date") ) ) );
       		  instrument.setMaturityDate( ConvertUtils.dateToInt(
       				  formatter.parse( ref_data.getString("maturity_date") ) ) );
  
       		  instrument.setTickSize( ref_data.getInt("tick_size")  );
       	  } catch (Exception exp )
       	  {
       		  System.out.println("Exception while processing Ref Data" + exp.toString() );
       		  return false;
       	  }
		 
		 return true;
	}

}
