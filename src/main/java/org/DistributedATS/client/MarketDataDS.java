package org.DistributedATS.client;

import com.smartgwt.client.data.DataSource;

public class MarketDataDS extends DataSource {

  public static MarketDataDS getInstance() {
    if (instance == null) {
      instance = new MarketDataDS("supplyItemLocalDS");
    }
    return instance;
  }

  public MarketDataDS(String id) { setID(id); }

  private static MarketDataDS instance = null;
}
