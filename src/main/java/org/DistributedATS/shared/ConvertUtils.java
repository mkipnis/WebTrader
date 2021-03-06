package org.DistributedATS.shared;

import java.util.Date;

public class ConvertUtils {
	
	public static Integer dateToInt( Date date )
	{
		Integer output = ((date.getYear()+1900)*10000) + (date.getMonth()+1)*100 + date.getDate(); 
		return output;
	}
	
	public static Date intToDate( Integer intDate )
	{
		Integer year = new Integer(intDate/10000);
		Integer month = (intDate - (year*10000))/100;
		Integer date = intDate - ((year * 10000) + (month *100));
		
		
		return new Date( year-1900, month-1, date );
	}
	
	static public String getDisplayPrice(Instrument instrument_with_ref_data, Integer price_in_tix )
	{

		if ( instrument_with_ref_data.getTickSize() % 256 == 0 )
		{
			return formatPrice32(price_in_tix, instrument_with_ref_data.getTickSize());
			
		} else {
			
			Double price_in_dec = price_in_tix/(double)instrument_with_ref_data.getTickSize();
			return price_in_dec.toString();
			 
		}
		
	}
	
	static private String formatPrice32( Integer price_in_ticks, Integer tick_size )
	{
		double decimal_price = price_in_ticks/(double)tick_size;
		
		int d_handle = (int)decimal_price;
		
		double fraction_1 = decimal_price - (float)d_handle;
		
		int f_handle = (int)(fraction_1*32.0f);
		
		int fraction_2 = (int)((fraction_1-f_handle/32.0f)*tick_size);
		
		String str_handle = "";
		if (f_handle < 10)
		{
			str_handle += "0";
			str_handle +=  new Integer(f_handle).toString();
		} else
			str_handle +=  new Integer(f_handle).toString();
		
		String fraction_2_str = "";
		
		if ( fraction_2 == 4 )
			fraction_2_str = "+";
		else
			fraction_2_str = Integer.valueOf(fraction_2).toString();
		
		String result = (new Integer(d_handle)).toString() + "-" +  str_handle + fraction_2_str;
		
        return result;
	}
	
	
	static public Integer simplePrice32Parser( String price32 )
	{
		if( price32.contains("-") )
		{
			String[] split = price32.split("-");
		
			double whole_number = new Double(split[0]);
			double fraction = 0.0;
			
			int eight = 0;
			
			if ( split[1].length() == 3 ) // 8th
			{
 
				char eight_char = split[1].charAt(2);
				
				if ( eight_char == '+' )
					eight = 4;
				else
					eight = new Integer(String.valueOf(eight_char)).intValue();
				
			} else {
				fraction = Double.valueOf(split[1]);
				
				fraction /= 100.0*32;
			}
			
			return new Double((whole_number+fraction+eight/256)*256).intValue();
			
		} else {
			double whole_number = new Double(price32);
			
			return new Double(whole_number*256).intValue();
		}
		
		
	}

}
