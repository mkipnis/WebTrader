/*
   Copyright (C) 2021 Mike Kipnis

   This file is part of DistributedATS, a free-software/open-source project
   that integrates QuickFIX and LiquiBook over OpenDDS. This project simplifies
   the process of having multiple FIX gateways communicating with multiple
   matching engines in realtime.
   
   Permission is hereby granted, free of charge, to any person obtaining a copy
   of this software and associated documentation files (the "Software"), to deal
   in the Software without restriction, including without limitation the rights
   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
   copies of the Software, and to permit persons to whom the Software is
   furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in all
   copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
   SOFTWARE.
*/

package org.DistributedATS.client;

import java.util.HashMap;

import org.DistributedATS.shared.FIXUserSession;
import org.DistributedATS.shared.Instrument;
import org.DistributedATS.shared.MarketDataSnapshot;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.layout.PortalLayout;

public class PriceDepthPortalLayout extends PortalLayout {
	
	public PriceDepthPortalLayout( WebTrader webTrader )
	{
		this.webTrader = webTrader;
		
	    setHeight100();
	    setWidth("95%");
	}
	
	 public Canvas getDropPortlet(Canvas dragTarget, Integer colNum,
             Integer rowNum, Integer dropPosition) {
		 if (dragTarget instanceof ListGrid) {

			 Record[] records = ((ListGrid)dragTarget).getDragData();

			 String exchangeField = records[0].getAttribute(MarketDataCanvas.EXCHANGE_FIELD);
			 String symbolField = records[0].getAttribute(MarketDataCanvas.SYMBOL_FIELD);

			 Double lastPrice = records[0].getAttributeAsDouble( MarketDataCanvas.LAST_TRADED_PRICE);
			 
			 if ( lastPrice == null || lastPrice.equals(0.0))
				 lastPrice = records[0].getAttributeAsDouble( MarketDataCanvas.OPEN_PRICE);
				 
			 Instrument instrument = new Instrument(exchangeField, symbolField);

			 //webTrader.setActiveInstrument(instrument);
			 //webTrader.getTicketCanvas().populateTicket(lastPrice, 100);

			 PriceDepthPortlet currentPriceDepthPortlet = priceDepthCanvasMap.get(instrument);

			 if (currentPriceDepthPortlet != null) {
				 priceDepthCanvasMap.remove(instrument);
				 super.removePortlet(currentPriceDepthPortlet);
			 }
			 
			 Instrument instrumentWithRefData = WebTrader.getInstance().getInstrumentWithRefData(instrument);

			 PriceDepthPortlet portlet = new PriceDepthPortlet(instrumentWithRefData);

			 priceDepthCanvasMap.put(instrumentWithRefData, portlet);
			 
		      WebTrader.getInstance().getTicketCanvas().setDisabled(false);

			 return portlet;
		 } else {
			 return super.getDropPortlet(dragTarget, colNum, rowNum, dropPosition);
		 }
	 }
	 
	 public void updatePriceDepth( FIXUserSession fixSessionData )
	 {
		 for (Instrument instrument : priceDepthCanvasMap.keySet()) {
             PriceDepthCanvas priceDepthCanvas =
                 priceDepthCanvasMap.get(instrument).getPriceDepthCanvas();

             MarketDataSnapshot marketDataSnapshot =
            		 fixSessionData.instrumentMarketDataSnapshot.get(instrument);

             if (marketDataSnapshot != null) {
               priceDepthCanvas.updatePriceDepth(instrument, marketDataSnapshot);
             }
           }
	 }
	
	  private HashMap<Instrument, PriceDepthPortlet> priceDepthCanvasMap =
		      new HashMap<Instrument, PriceDepthPortlet>();
	  
	  private WebTrader webTrader;


}
