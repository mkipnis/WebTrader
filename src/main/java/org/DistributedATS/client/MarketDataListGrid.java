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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.NumberFormat;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.fields.DataSourceDateField;
import com.smartgwt.client.data.fields.DataSourceFloatField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridComponent;
import com.smartgwt.client.util.JSOHelper;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.DistributedATS.shared.ConvertUtils;
import org.DistributedATS.shared.Instrument;
import org.DistributedATS.shared.MarketDataSnapshot;
import org.DistributedATS.shared.Position;
import org.DistributedATS.shared.PriceLevel;

public class MarketDataListGrid extends ListGrid {

  @Override
  protected String getCellCSSText(ListGridRecord record, int rowNum,
                                  int colNum) {

    String columnName = getFieldName(colNum);

    if (columnName.equals(MarketDataCanvas.INSTRUMENT_FIELD)) {
      return "color:blue";

    } else if (columnName.equals(MarketDataCanvas.PRICE_CHANGE)) {
      Float price_change =
          record.getAttributeAsFloat(MarketDataCanvas.PRICE_CHANGE);

      if (price_change > 0)
        return "font-weight:bold; color:#029117;";
      else if (price_change < 0)
        return "font-weight:bold; color:#d64949;";
      else
        return "font-weight:bold;";

    } else {
      return super.getBaseStyle(record, rowNum, colNum);
    }
  }

  public MarketDataListGrid(final MarketDataCanvas marketDataCanvas) {
    setCanDragRecordsOut(true);
    setCanDragSelect(false);
    setAlternateRecordStyles(true);

    ToolStrip gridEditControls = new ToolStrip();
    gridEditControls.setWidth100();
    gridEditControls.setHeight(24);

    LayoutSpacer spacer = new LayoutSpacer();
    spacer.setWidth("*");

    ToolStripButton editButton = new ToolStripButton("Add/Remove Instruments");
    editButton.setIcon("[SKIN]/actions/edit.png");
    editButton.setPrompt("Add/Remove securities");
    editButton.addClickHandler(
        new com.smartgwt.client.widgets.events.ClickHandler() {
          @Override
          public void onClick(
              com.smartgwt.client.widgets.events.ClickEvent event) {

            InstrumentPicker instrumentPicker =
                new InstrumentPicker(marketDataCanvas);

            instrumentPicker.setRect(100, 100, 630, 650);

            instrumentPicker.draw();
          }
        });

    gridEditControls.setMembers(spacer, editButton);

    setWidth100();
    setHeight100();

    setShowRecordComponents(true);
    setShowRecordComponentsByCell(true);

    setAutoFetchData(true);
    addRecordClickHandler(new RecordClickHandler() {
      public void onRecordClick(RecordClickEvent event) {

        WebTrader.getInstance().getTicketCanvas().setDisabled(false);

        ListGridRecord record = event.getRecord();
        final String instrumentName =
            record.getAttributeAsString(MarketDataCanvas.INSTRUMENT_FIELD);

        Integer ticketPrice = 0;
        Integer ticketSize = 100;
        
        HashMap<String, Integer> ticks = (HashMap<String, Integer>)record.getAttributeAsMap(MarketDataCanvas.TICKS_FIELD);

        if (event.getField().getName().contains("ASK")) {
          ticketPrice =
        		  ticks.get(MarketDataCanvas.ASK_PRICE_FIELD);
          ticketSize =
              record.getAttributeAsInt(MarketDataCanvas.ASK_SIZE_FIELD);
        } else if (event.getField().getName().contains("BID")) {
          ticketPrice =
        		  ticks.get(MarketDataCanvas.BID_PRICE_FIELD);
          ticketSize =
              record.getAttributeAsInt(MarketDataCanvas.BID_SIZE_FIELD);
        }

        // no market, no problem ... lets get last traded or open price if last
        // price is not available
        if (ticketPrice == null || ticketPrice == 0.0 )
        {
        	ticketPrice =
        			 ticks.get(MarketDataCanvas.LAST_TRADED_PRICE);
        	ticketSize = 100;
        }
        
        if (ticketPrice == null || ticketPrice == 0.0 )
          ticketPrice =
        		  ticks.get(MarketDataCanvas.OPEN_PRICE);

        Instrument instrument = getInstrumentNameMap().get(instrumentName);

        WebTrader.getInstance().setActiveInstrument(instrument);
        WebTrader.getInstance().getTicketCanvas().populateTicket(ticketPrice,
                                                                 ticketSize);
      }
    });

    ListGridField instrumentField =
        new ListGridField(MarketDataCanvas.INSTRUMENT_FIELD, "Instrument", 120);
    
    ListGridField bidPriceField =
        new ListGridPriceField(MarketDataCanvas.BID_PRICE_FIELD, "Bid");

    
    ListGridField askPriceField =
        new ListGridPriceField(MarketDataCanvas.ASK_PRICE_FIELD, "Ask");

    ListGridField ticksField =
            new ListGridPriceField(MarketDataCanvas.TICKS_FIELD, "ticks");
    
    ticksField.setHidden(true);

    
    ListGridField bidSizeField =
        new ListGridField(MarketDataCanvas.BID_SIZE_FIELD, "BidSize");
    ListGridField askSizeField =
        new ListGridField(MarketDataCanvas.ASK_SIZE_FIELD, "AskSize");
    ListGridField lastTradedPriceField =
        new ListGridPriceField(MarketDataCanvas.LAST_TRADED_PRICE, "LastPrice");
    ListGridField volumeField =
        new ListGridField(MarketDataCanvas.VOLUME, "Volume");
    volumeField.setCellFormatter(new CellFormatter() {  
        public String format(Object value, ListGridRecord record, int rowNum, int colNum) {  
            NumberFormat nf = NumberFormat.getFormat("0,000");  
            try {  
                return nf.format(((Number) value).longValue());  
            } catch (Exception e) {  
                return value.toString();  
            }  
        }  
    });  
    
    ListGridField cusipField =
            new ListGridField(MarketDataCanvas.CUSIP_FIELD, "Cusip");
    
    ListGridField maturityDateField =
            new ListGridField(MarketDataCanvas.MATURITY_DATE_FIELD, "MaturityDate");
    
    ListGridField positionField =
        new ListGridField(MarketDataCanvas.POSITION, "Position");
    ListGridField openPriceField =
        new ListGridPriceField(MarketDataCanvas.OPEN_PRICE, "OpenPrice");
    openPriceField.setAlign(Alignment.RIGHT);

    ListGridField priceChangeField =
        new ListGridField(MarketDataCanvas.PRICE_CHANGE, "PriceChange");
    priceChangeField.setPrompt("Price Change from open");

    ListGridField exchangeField = new ListGridField(
        MarketDataCanvas.EXCHANGE_FIELD, MarketDataCanvas.EXCHANGE_FIELD);
    ListGridField symbolField = new ListGridField(
        MarketDataCanvas.SYMBOL_FIELD, MarketDataCanvas.SYMBOL_FIELD);

    exchangeField.setHidden(true);
    symbolField.setHidden(true);

    ListGridField vwapField =
        new ListGridField(MarketDataCanvas.VWAP, MarketDataCanvas.VWAP);
    vwapField.setCellFormatter(new CellFormatter() {
        public String format(Object value, ListGridRecord record, int rowNum,
                             int colNum) {
          if (value == null)
            return null;
          try {
            NumberFormat nf = NumberFormat.getFormat("#,##0.00");
            return nf.format(((Number)value).floatValue());
          } catch (Exception e) {
            return value.toString();
          }
        }
      });
    // vwapField.setHidden(true);

    ListGridField pnlField =
        new ListGridPriceField(MarketDataCanvas.PNL, "PnL");

    pnlField.setCellFormatter(new CellFormatter() {
      public String format(Object value, ListGridRecord record, int rowNum,
                           int colNum) {
        if (value == null)
          return null;
        try {
          NumberFormat nf = NumberFormat.getFormat("#,##0.00");
          return "$" + nf.format(((Number)value).floatValue());
        } catch (Exception e) {
          return value.toString();
        }
      }
    });

    priceChangeField.setCellFormatter(new CellFormatter() {
      public String format(Object value, ListGridRecord record, int rowNum,
                           int colNum) {
        if (value == null)
          return null;
        try {
          NumberFormat nf = NumberFormat.getFormat("#,##0.00");
          return nf.format(((Number)value).floatValue());
        } catch (Exception e) {
          return value.toString();
        }
      }
    });

    DataSource ds = new DataSource();
    ds.setClientOnly(true);

    DataSourceTextField instrumentFieldDS =
        new DataSourceTextField(MarketDataCanvas.INSTRUMENT_FIELD);
    instrumentFieldDS.setPrimaryKey(true);
    ds.addField(instrumentFieldDS);

    DataSourceFloatField positionDS =
        new DataSourceFloatField(MarketDataCanvas.POSITION);
    ds.addField(positionDS);

    DataSourceFloatField vwapDS =
        new DataSourceFloatField(MarketDataCanvas.VWAP);
    ds.addField(vwapDS);

    DataSourceFloatField pnlDS = new DataSourceFloatField(MarketDataCanvas.PNL);
    ds.addField(pnlDS);

    DataSourceFloatField bidPriceDS =
        new DataSourceFloatField(MarketDataCanvas.BID_PRICE_FIELD);
    ds.addField(bidPriceDS);

    DataSourceFloatField askPriceDS =
        new DataSourceFloatField(MarketDataCanvas.ASK_PRICE_FIELD);
    ds.addField(askPriceDS);

    DataSourceFloatField bidSizeDS =
        new DataSourceFloatField(MarketDataCanvas.BID_SIZE_FIELD);
    ds.addField(bidSizeDS);

    DataSourceFloatField askSizeDS =
        new DataSourceFloatField(MarketDataCanvas.ASK_SIZE_FIELD);
    ds.addField(askSizeDS);

    DataSourceFloatField lastTradedPriceDS =
        new DataSourceFloatField(MarketDataCanvas.LAST_TRADED_PRICE);
    ds.addField(lastTradedPriceDS);

    DataSourceFloatField volumeDS =
        new DataSourceFloatField(MarketDataCanvas.VOLUME);
    ds.addField(volumeDS);

    DataSourceTextField exchangeFieldDS =
        new DataSourceTextField(MarketDataCanvas.EXCHANGE_FIELD);
    ds.addField(exchangeFieldDS);

    DataSourceTextField symbolFieldDS =
        new DataSourceTextField(MarketDataCanvas.SYMBOL_FIELD);
    ds.addField(symbolFieldDS);

    DataSourceTextField openPriceFieldDS =
        new DataSourceTextField(MarketDataCanvas.OPEN_PRICE);
    ds.addField(openPriceFieldDS);

    DataSourceFloatField priceChangeFieldDS =
        new DataSourceFloatField(MarketDataCanvas.PRICE_CHANGE);
    ds.addField(priceChangeFieldDS);
    
    DataSourceTextField cusipFieldDS =
            new DataSourceTextField(MarketDataCanvas.CUSIP_FIELD);
        ds.addField(cusipFieldDS);
        
    DataSourceDateField maturityDateFieldDS =
                 new DataSourceDateField(MarketDataCanvas.MATURITY_DATE_FIELD);
    ds.addField(maturityDateFieldDS);

    setFields(instrumentField, positionField, lastTradedPriceField,
              priceChangeField, vwapField, pnlField, bidPriceField,
              askPriceField, bidSizeField, askSizeField, volumeField,
              exchangeField, symbolField, openPriceField, cusipField,
              maturityDateField, ticksField);
    
    
    setDataSource(ds);

    setGridComponents(new Object[] {ListGridComponent.HEADER,
                                    ListGridComponent.FILTER_EDITOR,
                                    ListGridComponent.BODY, gridEditControls});
  }

  public void clearGrid() {

    DataSource ds = getDataSource();

    setAutoFetchData(false);
    for (ListGridRecord record : listGridRecordMap.values()) {
      ds.removeData(record);
    }

    setAutoFetchData(true);
    // fetchData();
    listGridRecordMap.clear();
  }

  public void insertSymbol(Instrument instrument) {
    DataSource ds = getDataSource();

    if (listGridRecordMap.get(instrument) != null)
      return;

    ListGridRecord record = new ListGridRecord();

    String instrumentName = instrument.getInstrumentName();

    instrumentNameMap.put(instrumentName, instrument);

    record.setAttribute(MarketDataCanvas.INSTRUMENT_FIELD, instrumentName);
    record.setAttribute(MarketDataCanvas.EXCHANGE_FIELD,
                        instrument.getSecurityExchange());
    record.setAttribute(MarketDataCanvas.SYMBOL_FIELD, instrument.getSymbol());
    
    // re-thing and re-factor
    Integer instrumentWithRefDataIndex = WebTrader.getInstance().getCompleteSecurityList().indexOf(instrument);
    
    Instrument instrumentWithRefData = WebTrader.getInstance().getCompleteSecurityList().get(instrumentWithRefDataIndex);
    
    record.setAttribute(MarketDataCanvas.CUSIP_FIELD, instrumentWithRefData.getCusip());
    record.setAttribute(MarketDataCanvas.MATURITY_DATE_FIELD, ConvertUtils.intToDate(instrumentWithRefData.getMaturityDate()));

    listGridRecordMap.put(instrumentWithRefData, record);

    ds.addData(record);
  }

  public void updatePositions(HashMap<Instrument, Position> positionData) {
    for (Instrument instrument : positionData.keySet()) {
      ListGridRecord listGridRecord = listGridRecordMap.get(instrument);

      if (listGridRecord != null) {
        Position position = positionData.get(instrument);

       // Double lastTradedPrice = listGridRecord.getAttributeAsDouble(
        //    MarketDataCanvas.LAST_TRADED_PRICE);

        HashMap<String, Integer> ticks = (HashMap<String, Integer>)listGridRecord.getAttributeAsMap(MarketDataCanvas.TICKS_FIELD);
        
        Integer lastTradedPriceTicks =
      		  ticks.get(MarketDataCanvas.LAST_TRADED_PRICE);
        
        if (position == null || lastTradedPriceTicks == null)
          continue;

        Instrument instrument_with_ref_data = WebTrader.getInstance().getInstrumentWithRefData(instrument);
        
        double vwap = position.getVWAP()/instrument_with_ref_data.getTickSize();
        listGridRecord.setAttribute(MarketDataCanvas.VWAP,  vwap);

        // TODO: P&L for treasuries and others
       
        Double positionAmt = position.buy_amt - position.sell_amt;
        
        double lastTradedPrice = lastTradedPriceTicks/instrument_with_ref_data.getTickSize().floatValue();

        listGridRecord.setAttribute(MarketDataCanvas.POSITION, positionAmt);
        if (positionAmt != 0)
          listGridRecord.setAttribute(MarketDataCanvas.PNL,
                                      (lastTradedPrice - vwap) * positionAmt);
        else
        	if ( instrument_with_ref_data.getTickSize()%256==0)
        	{
        		listGridRecord.setAttribute(
        				MarketDataCanvas.PNL,
        				(position.sell_amt * position.sell_avg_price -
        						position.buy_amt * position.buy_avg_price) / instrument_with_ref_data.getTickSize() / 100 /*percent of par*/);
        	} else {
           		listGridRecord.setAttribute(
        				MarketDataCanvas.PNL,
        				(position.sell_amt * position.sell_avg_price -
        						position.buy_amt * position.buy_avg_price) / instrument_with_ref_data.getTickSize() );
        	}
             

        updateData(listGridRecord);
      }
    }

    fetchData();
  }
  
  private void insertPriceInTicksButSetVisiblePriceToHumanReadable(
		  String list_grid_field,
		  ListGridRecord listGridRecord,
		  Instrument instrument,
		  HashMap<String, Integer> ticks_map,
		  Integer price_in_ticks)
  {
	  
	  String price_str = "";
	 
	  
	  listGridRecord.setAttribute(list_grid_field, ConvertUtils.getDisplayPrice(instrument, price_in_ticks));
	  
	  ticks_map.put( list_grid_field, price_in_ticks);
  }
  

  public void updateTopLevelMarketData(
      HashMap<Instrument, MarketDataSnapshot> marketDataSnapshots) {

    updated_count = 0;

    for (Instrument instrument : listGridRecordMap.keySet()) {
      ListGridRecord listGridRecord = listGridRecordMap.get(instrument);
      MarketDataSnapshot marketDataSnapshot =
          marketDataSnapshots.get(instrument);

      if (marketDataSnapshot == null)
        continue;

      PriceLevel bestBid = marketDataSnapshot.getBidSide().get(0);
      PriceLevel bestAsk = marketDataSnapshot.getAskSide().get(0);
      
      HashMap<String, Integer> ticks = new HashMap<String, Integer>();

      if (bestBid != null && bestBid.getSize() != 0) {
    	  
    	insertPriceInTicksButSetVisiblePriceToHumanReadable(MarketDataCanvas.BID_PRICE_FIELD, 
    			listGridRecord, instrument, ticks, bestBid.getPrice());
    	  
        //listGridRecord.setAttribute(MarketDataCanvas.BID_PRICE_FIELD,
        //                            bestBid.getPrice());
        listGridRecord.setAttribute(MarketDataCanvas.BID_SIZE_FIELD,
                                    bestBid.getSize());
      } else {
        listGridRecord.setAttribute(MarketDataCanvas.BID_PRICE_FIELD, "");
        listGridRecord.setAttribute(MarketDataCanvas.BID_SIZE_FIELD, "");
      }

      if (bestAsk != null && bestAsk.getSize() != 0.0 ) {
    	  
      	insertPriceInTicksButSetVisiblePriceToHumanReadable(MarketDataCanvas.ASK_PRICE_FIELD, 
    			listGridRecord, instrument, ticks, bestAsk.getPrice());
       // listGridRecord.setAttribute(MarketDataCanvas.ASK_PRICE_FIELD,
        //                            bestAsk.getPrice());
        listGridRecord.setAttribute(MarketDataCanvas.ASK_SIZE_FIELD,
                                    bestAsk.getSize());
      } else {
        listGridRecord.setAttribute(MarketDataCanvas.ASK_PRICE_FIELD, "");
        listGridRecord.setAttribute(MarketDataCanvas.ASK_SIZE_FIELD, "");
      }

    	insertPriceInTicksButSetVisiblePriceToHumanReadable(MarketDataCanvas.LAST_TRADED_PRICE, 
  			listGridRecord, instrument, ticks,  marketDataSnapshot.getLastTradedPrice());
    	
      //listGridRecord.setAttribute(MarketDataCanvas.LAST_TRADED_PRICE,
      //                            marketDataSnapshot.getLastTradedPrice());
      listGridRecord.setAttribute(MarketDataCanvas.VOLUME,
                                  marketDataSnapshot.getVolume());
      
  	insertPriceInTicksButSetVisiblePriceToHumanReadable(MarketDataCanvas.OPEN_PRICE, 
			listGridRecord, instrument, ticks,  marketDataSnapshot.getOpenPrice());
      
     // listGridRecord.setAttribute(MarketDataCanvas.OPEN_PRICE,
     //                             marketDataSnapshot.getOpenPrice());

      if (marketDataSnapshot.getVolume() != 0)
      {
    	  	insertPriceInTicksButSetVisiblePriceToHumanReadable(MarketDataCanvas.PRICE_CHANGE, 
    				listGridRecord, instrument, ticks,  marketDataSnapshot.getLastTradedPrice() -
    													marketDataSnapshot.getOpenPrice());
    	  	
        //listGridRecord.setAttribute(MarketDataCanvas.PRICE_CHANGE,
        //                            marketDataSnapshot.getLastTradedPrice() -
        //                                marketDataSnapshot.getOpenPrice());
      } else {
  	  	insertPriceInTicksButSetVisiblePriceToHumanReadable(MarketDataCanvas.PRICE_CHANGE, 
  				listGridRecord, instrument, ticks, 0);
      }
      
      listGridRecord.setAttribute(MarketDataCanvas.TICKS_FIELD, ticks);
      

      updateData(listGridRecord, new DSCallback() {
        @Override
        public void execute(DSResponse dsResponse, Object data,
                            DSRequest dsRequest) {
          // TODO Auto-generated method stub

          /*JavaScriptObject jsObject = (JavaScriptObject)data;

          LinkedHashMap<String, String> javaObj =
              (LinkedHashMap)JSOHelper.convertToJava(jsObject);

          String exchange =
              (String)javaObj.get(MarketDataCanvas.EXCHANGE_FIELD);
          String symbol = (String)javaObj.get(MarketDataCanvas.SYMBOL_FIELD);

          Instrument instrument = new Instrument(exchange, symbol);

          WebTrader.getInstance().insertActiveInstrument(instrument);*/
        }
      });
    }

    fetchData(null, new DSCallback() {
      @Override
      public void execute(DSResponse dsResponse, Object data,
                          DSRequest dsRequest) {
        // TODO Auto-generated method stub
        /*if ( ++updated_count == listGridRecordMap.keySet().size() )
        {

        }*/
      }
    });
  }

  public HashMap<String, Instrument> getInstrumentNameMap() {
    return instrumentNameMap;
  }

  public HashMap<Instrument, ListGridRecord> getListGridRecordMap() {
    return listGridRecordMap;
  }

  int updated_count = 0;

  private HashMap<String, Instrument> instrumentNameMap =
      new HashMap<String, Instrument>();
  private HashMap<Instrument, ListGridRecord> listGridRecordMap =
      new HashMap<Instrument, ListGridRecord>();
  // private HashMap<Instrument, Double> lastTradedPrices = new
  // HashMap<Instrument, Double>();
}
