package org.DistributedATS.client;

import com.smartgwt.client.widgets.layout.Portlet;
import org.DistributedATS.shared.Instrument;

public class PriceDepthPortlet extends Portlet {

  public PriceDepthPortlet(Instrument instrument) {
    setTitle(instrument.getInstrumentName());
    priceDepthCanvas = new PriceDepthCanvas();
    addItem(priceDepthCanvas);
  }

  public PriceDepthCanvas getPriceDepthCanvas() { return priceDepthCanvas; };

  private PriceDepthCanvas priceDepthCanvas = null;
}
