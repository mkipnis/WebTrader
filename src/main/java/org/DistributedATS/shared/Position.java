package org.DistributedATS.shared;

import java.io.Serializable;

public class Position implements Serializable {
	
	public Double buy_amt = 0.0;
	public Double sell_amt = 0.0;
	public Double buy_avg_price = 0.0;
	public Double sell_avg_price = 0.0;
	
	public Double getPosition()
	{
		return (this.buy_amt - this.sell_amt);
	}
	
	public Double getVWAP()
	{
		if ( this.buy_amt-this.sell_amt != 0 )
		{
			return (this.buy_amt*this.buy_avg_price - this.sell_amt*this.sell_avg_price)/(this.buy_amt-this.sell_amt);
		} else {
			return 0.0;
		}
	};

}
