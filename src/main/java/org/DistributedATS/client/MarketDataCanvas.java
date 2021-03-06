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

import com.google.gwt.core.client.EntryPoint;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.data.ResultSet;
import com.smartgwt.client.data.events.DataChangedEvent;
import com.smartgwt.client.data.events.DataChangedHandler;
import com.smartgwt.client.data.fields.DataSourceFloatField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.DragDataAction;
import com.smartgwt.client.types.ListGridComponent;
import com.smartgwt.client.types.ListGridEditEvent;
import com.smartgwt.client.types.Side;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import java.util.ArrayList;
import java.util.HashMap;
import org.DistributedATS.shared.Instrument;
import org.DistributedATS.shared.MarketDataSnapshot;
import org.DistributedATS.shared.Position;
import org.DistributedATS.shared.PriceLevel;

public class MarketDataCanvas extends Canvas {

  public static String INSTRUMENT_FIELD = "INSTRUMENT_FIELD";
  public static String MARKET_FIELD = "MARKET_FIELD";
  public static String BID_PRICE_FIELD = "BID_PRICE_FIELD";
  public static String BID_SIZE_FIELD = "BID_SIZE_FIELD";
  public static String ASK_PRICE_FIELD = "ASK_PRICE_FIELD";
  public static String ASK_SIZE_FIELD = "ASK_SIZE_FIELD";
  public static String LAST_TRADED_PRICE = "LAST_TRADED_PRICE";
  public static String VOLUME = "VOLUME";
  public static String POSITION = "POSITION";
  public static String VWAP = "VWAP";
  public static String PNL = "PNL";
  public static String OPEN_PRICE = "OPEN_PRICE";
  public static String EXCHANGE_FIELD = "EXCHANGE_FIELD";
  public static String SYMBOL_FIELD = "SYMBOL_FIELD";
  // public static String MARKET_DATA_STATE = "MARKET_DATA_STATE";
  public static String PRICE_CHANGE = "PRICE_CHANGE";
  
  public static String TICKS_FIELD = "TICKS_FIELD";
  
  public static String CUSIP_FIELD = "CUSIP_FIELD";
  public static String MATURITY_FIELD = "MATURITY_FIELD";
  
  public static String MATURITY_DATE_FIELD = "MATURITY_DATE_FIELD";

  public static int BID_CHANGE_BIT = 0;
  public static int ASK_CHANAGE_BIT = 1;
  public static int TRADE_BIT = 2;

  public HashMap<String, MarketDataListGrid> marketDataGrids =
      new HashMap<String, MarketDataListGrid>();

  final TabSet topTabSet = new TabSet();

  public MarketDataCanvas() {
	  
      WebTraderSectionCanvas webTraderSectionCanvas = new WebTraderSectionCanvas("Market Data");
		
    topTabSet.setTabBarPosition(Side.TOP);
    topTabSet.setTabBarAlign(Side.LEFT);
    topTabSet.setWidth("100%");
    topTabSet.setHeight("100%");

    MarketDataListGrid listGridAll = new MarketDataListGrid(this);

    marketDataGrids.put("All", listGridAll);

    Tab tTab1 = new Tab("All");
    tTab1.setPane(listGridAll);

    topTabSet.addTab(tTab1);
    
    webTraderSectionCanvas.setSectionStackCanvas(topTabSet);
    

    addChild(webTraderSectionCanvas);
  }

  public void clearGrids() {
    for (MarketDataListGrid listGrid : marketDataGrids.values()) {
      listGrid.clearGrid();
    }
  }

  public void insertSymbol(Instrument instrument) {
    MarketDataListGrid listGridAll = marketDataGrids.get("All");

    listGridAll.insertSymbol(instrument);

    String exchangeName = instrument.getSecurityExchange();

    MarketDataListGrid exchangeGrid = marketDataGrids.get(exchangeName);

    if (exchangeGrid == null) {
      exchangeGrid = new MarketDataListGrid(this);
      marketDataGrids.put(exchangeName, exchangeGrid);
      Tab tTab1 = new Tab(exchangeName);
      tTab1.setPane(exchangeGrid);

      topTabSet.addTab(tTab1);
    }

    exchangeGrid.insertSymbol(instrument);
  }

  public void updateTopLevelMarketData(
      HashMap<Instrument, MarketDataSnapshot> marketDataSnapshots) {
    for (MarketDataListGrid marketDataListGrid : marketDataGrids.values()) {
      marketDataListGrid.updateTopLevelMarketData(marketDataSnapshots);
    }
  }

  public void updatePositions(HashMap<Instrument, Position> positionData) {
    for (MarketDataListGrid marketDataListGrid : marketDataGrids.values()) {
      marketDataListGrid.updatePositions(positionData);
    }
  }
}
