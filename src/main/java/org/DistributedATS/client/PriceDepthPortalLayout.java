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

			 webTrader.setActiveInstrument(instrument);
			 webTrader.getTicketCanvas().populateTicket(lastPrice, 100);

			 PriceDepthPortlet currentPriceDepthPortlet = priceDepthCanvasMap.get(instrument);

			 if (currentPriceDepthPortlet != null) {
				 priceDepthCanvasMap.remove(instrument);
				 super.removePortlet(currentPriceDepthPortlet);
			 }

			 PriceDepthPortlet portlet = new PriceDepthPortlet(instrument);

			 priceDepthCanvasMap.put(instrument, portlet);
			 
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
               priceDepthCanvas.updatePriceDepth(marketDataSnapshot);
             }
           }
	 }
	
	  private HashMap<Instrument, PriceDepthPortlet> priceDepthCanvasMap =
		      new HashMap<Instrument, PriceDepthPortlet>();
	  
	  private WebTrader webTrader;


}
