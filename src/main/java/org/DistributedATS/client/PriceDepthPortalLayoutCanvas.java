package org.DistributedATS.client;

import com.smartgwt.client.widgets.Canvas;

public class PriceDepthPortalLayoutCanvas extends Canvas {
	
	public PriceDepthPortalLayoutCanvas( PriceDepthPortalLayout priceDepthCanvas )
	{     
	      WebTraderSectionCanvas webTraderSectionCanvas = new WebTraderSectionCanvas("Price Depth");
	      webTraderSectionCanvas.setSectionStackCanvas(priceDepthCanvas);
	      
	      addChild(webTraderSectionCanvas);
	}
	

}
