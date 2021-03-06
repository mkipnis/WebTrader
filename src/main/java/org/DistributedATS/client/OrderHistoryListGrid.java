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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.fields.DataSourceDateTimeField;
import com.smartgwt.client.data.fields.DataSourceFloatField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import java.util.HashMap;

import org.DistributedATS.shared.ConvertUtils;
import org.DistributedATS.shared.ExecutionReport;
import org.DistributedATS.shared.FIXUserSession;
import org.DistributedATS.shared.Instrument;
import org.DistributedATS.shared.Order;
import org.DistributedATS.shared.OrderKey;
import org.DistributedATS.shared.PriceLevel;

public class OrderHistoryListGrid extends ListGrid {

  private HLayout rollOverCanvas;
  private ListGridRecord rollOverRecord;

  public static String ORDER_ID = "ORDER_ID_FIELD";
  public static String INSTRUMENT_FIELD = "INSTRUMENT_FIELD";
  public static String CUSIP_FIELD = "CUSIP_FIELD";
  public static String SIDE_FIELD = "SIDE_FIELD";
  public static String PRICE_FIELD = "PRICE_FIELD";
  public static String QTY_FIELD = "QTY_FIELD";
  public static String FILLED_QTY_FIELD = "FILLED_FIELD";
  public static String FILLED_PRICE_FIELD = "FILLED_PRICE_FIELD";
  public static String LAST_TIMESTAMP_FIELD = "LAST_TIMESTAMP_FIELD";
  public static String LAST_EXEC_REPORT_ID_FIELD = "LAST_EXEC_REPORT_ID_FIELD";
  public static String ORDER_STATUS_FIELD = "ORDER_STATUS_FIELD";
  public static String ACTION_FIELD = "ACTION_FIELD";

  public OrderHistoryListGrid(
      final ExecutionReportListGrid executionReportListGrid) {
    setWidth("99%");
    setHeight("99%");
    setAutoFetchData(true);

    setShowRollOverCanvas(true);
    setShowRollUnderCanvas(false);
    setAlternateRecordStyles(true);

    ListGridField orderIdField =
        new ListGridField(OrderHistoryListGrid.ORDER_ID, "OrderId");
    orderIdField.setWidth(150);

    ListGridField instrumentField =
        new ListGridField(OrderHistoryListGrid.INSTRUMENT_FIELD, "Instrument");
    instrumentField.setWidth(150);
    
    ListGridField cusipField =
            new ListGridField(OrderHistoryListGrid.CUSIP_FIELD, "Cusip");
    cusipField.setWidth(150);

    ListGridField sideField =
        new ListGridField(OrderHistoryListGrid.SIDE_FIELD, "Side");
    sideField.setWidth(40);

    ListGridField priceField =
        new ListGridPriceField(OrderHistoryListGrid.PRICE_FIELD, "Price");
    priceField.setWidth(70);

    ListGridField quantityField =
        new ListGridField(OrderHistoryListGrid.QTY_FIELD, "Quantity");
    quantityField.setWidth(70);

    ListGridField filledQuantityField =
        new ListGridField(OrderHistoryListGrid.FILLED_QTY_FIELD, "FilledQty");
    filledQuantityField.setWidth(120);

    ListGridField filledPriceField = new ListGridPriceField(
        OrderHistoryListGrid.FILLED_PRICE_FIELD, "FilledPrice");
    filledPriceField.setWidth(120);

    ListGridField orderStatusField = new ListGridField(
        OrderHistoryListGrid.ORDER_STATUS_FIELD, "OrderStatus");
    orderStatusField.setWidth(100);

    ListGridField lastUpdateTimestampField = new ListGridField(
        OrderHistoryListGrid.LAST_TIMESTAMP_FIELD, "LastUpdateDate(UTC)");
    lastUpdateTimestampField.setFormat("MM/dd/yy HH:mm:ss.SSS");
    lastUpdateTimestampField.setWidth(180);

    ListGridField lastExecutionReportIdField = new ListGridField(
        OrderHistoryListGrid.LAST_EXEC_REPORT_ID_FIELD, "LastExecReportId");
    lastExecutionReportIdField.setWidth(120);

    DataSource ds = new DataSource();
    ds.setClientOnly(true);

    DataSourceTextField orderIdDS =
        new DataSourceTextField(OrderHistoryListGrid.ORDER_ID);
    orderIdDS.setPrimaryKey(true);
    ds.addField(orderIdDS);

    DataSourceTextField instrumentDS =
        new DataSourceTextField(OrderHistoryListGrid.INSTRUMENT_FIELD);
    ds.addField(instrumentDS);
    
    DataSourceTextField cusipDS =
            new DataSourceTextField(OrderHistoryListGrid.CUSIP_FIELD);
        ds.addField(cusipDS);


    DataSourceTextField sideDS =
        new DataSourceTextField(OrderHistoryListGrid.SIDE_FIELD);
    ds.addField(sideDS);

    DataSourceTextField priceFieldDS =
        new DataSourceTextField(OrderHistoryListGrid.PRICE_FIELD);
    ds.addField(priceFieldDS);

    DataSourceFloatField quantityFieldDS =
        new DataSourceFloatField(OrderHistoryListGrid.QTY_FIELD);
    ds.addField(quantityFieldDS);

    DataSourceFloatField filledQuantityDS =
        new DataSourceFloatField(OrderHistoryListGrid.FILLED_QTY_FIELD);
    ds.addField(filledQuantityDS);

    DataSourceTextField filledPriceDS =
        new DataSourceTextField(OrderHistoryListGrid.FILLED_PRICE_FIELD);
    ds.addField(filledPriceDS);

    DataSourceDateTimeField lastUpdateTimestampDS =
        new DataSourceDateTimeField(OrderHistoryListGrid.LAST_TIMESTAMP_FIELD);
    lastUpdateTimestampDS.setFormat("MM/dd/yy HH:mm:ss.SSS");
    ds.addField(lastUpdateTimestampDS);

    DataSourceDateTimeField lastExecutionReportDS = new DataSourceDateTimeField(
        OrderHistoryListGrid.LAST_EXEC_REPORT_ID_FIELD);
    ds.addField(lastExecutionReportDS);

    DataSourceTextField orderStatusFieldDS =
        new DataSourceTextField(OrderHistoryListGrid.ORDER_STATUS_FIELD);
    ds.addField(orderStatusFieldDS);

    setFields(orderIdField, instrumentField, cusipField, sideField, priceField,
              quantityField, filledQuantityField, filledPriceField,
              orderStatusField, lastUpdateTimestampField,
              lastExecutionReportIdField);
    setDataSource(ds);

    setCanResizeFields(true);

    sort(OrderHistoryListGrid.LAST_TIMESTAMP_FIELD, SortDirection.DESCENDING);

    addRecordClickHandler(new RecordClickHandler() {
      public void onRecordClick(RecordClickEvent event) {

        ListGridRecord record = event.getRecord();

        String orderId = record.getAttribute(OrderHistoryListGrid.ORDER_ID);
        Order order = orderMap.get(new OrderKey(orderId));

        executionReportListGrid.updateExecutionReports(order);
      }
    });
  }

  @Override
  protected String getCellCSSText(ListGridRecord record, int rowNum,
                                  int colNum) {

    String columnName = getFieldName(colNum);
    
    String status=record.getAttribute(OrderHistoryListGrid.ORDER_STATUS_FIELD);

    if (status.equals("Cancelled"))
    	return "color:grey;";
    
    String css = "";
    if (!status.equals("Filled"))
    	css = "font-weight:bold;";

    if (columnName.equals(OrderHistoryListGrid.INSTRUMENT_FIELD)) {
      return css+"color:blue";

    } else {
      return css+super.getBaseStyle(record, rowNum, colNum);
    }
  }

  @Override
  protected Canvas getRollOverCanvas(Integer rowNum, Integer colNum) {
    rollOverRecord = this.getRecord(rowNum);

    // SC.say(
    // rollOverRecord.getAttribute(OrderHistoryCanvas.ORDER_STATUS_FIELD) );

    if (!rollOverRecord.getAttribute(OrderHistoryListGrid.ORDER_STATUS_FIELD)
             .equals("Filled") &&
        !rollOverRecord.getAttribute(OrderHistoryListGrid.ORDER_STATUS_FIELD)
             .equals("Cancelled")) {
      if (rollOverCanvas == null) {
        rollOverCanvas = new HLayout(3);
        rollOverCanvas.setSnapTo("R");
        rollOverCanvas.setWidth(50);
        rollOverCanvas.setHeight(22);
      }

      Button cancelButton = new Button();
      cancelButton.setID("CancelButton");
      cancelButton.setShowDown(false);
      cancelButton.setShowRollOver(false);
      cancelButton.setLayoutAlign(Alignment.CENTER);
      cancelButton.setTitle("Cancel");
      cancelButton.setHeight(20);
      cancelButton.setWidth(50);

      cancelButton.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {

          String orderId =
              rollOverRecord.getAttribute(OrderHistoryListGrid.ORDER_ID);
          Order order = orderMap.get(new OrderKey(orderId));

          FIXUserSession fixUserSession =
              UserSessionSingleton.getInstance().getFIXUserSession();

          WebTrader.getInstance().getFIXService().cancelOrder(
              fixUserSession.username, fixUserSession.token, order,
              new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable caught) {
                  // TODO Auto-generated method stub
                }

                @Override
                public void onSuccess(String result) {
                  // TODO Auto-generated method stub
                }
              });
        }
      });
      if (rollOverCanvas.getMember("CancelButton") == null)
        rollOverCanvas.addMember(cancelButton);
    } else {
      Canvas cancelButton = rollOverCanvas.getMember("CancelButton");

      if (cancelButton != null)
        rollOverCanvas.removeMember(cancelButton);
    }

    return rollOverCanvas;
  }

  public void insertUpdateOrder(Order orderIn) {
    Order order = orderMap.get(orderIn.orderKey);

    DataSource ds = getDataSource();
    
    Instrument instrument_with_ref_data = WebTrader.getInstance().getInstrumentWithRefData(orderIn.instrument);
    orderIn.instrument = instrument_with_ref_data;


    if (order == null) {
      ListGridRecord record = new ListGridRecord();

      record.setAttribute(OrderHistoryListGrid.INSTRUMENT_FIELD,
                          orderIn.instrument.getInstrumentName());
      record.setAttribute(OrderHistoryListGrid.CUSIP_FIELD,
              orderIn.instrument.getCusip());
      record.setAttribute(OrderHistoryListGrid.ORDER_ID,
                          orderIn.orderKey.getOrderKey());
      record.setAttribute(OrderHistoryListGrid.SIDE_FIELD, orderIn.getSide());
      record.setAttribute(OrderHistoryListGrid.PRICE_FIELD,
    		  	ConvertUtils.getDisplayPrice(instrument_with_ref_data, (int)orderIn.price));
      record.setAttribute(OrderHistoryListGrid.QTY_FIELD, orderIn.quantity);
      record.setAttribute(OrderHistoryListGrid.FILLED_QTY_FIELD,
                          orderIn.filled_quantity);
      record.setAttribute(OrderHistoryListGrid.LAST_TIMESTAMP_FIELD,
                          orderIn.lastUpdateTime);
      record.setAttribute(OrderHistoryListGrid.LAST_EXEC_REPORT_ID_FIELD,
                          orderIn.lastExecutionReportId);
      record.setAttribute(OrderHistoryListGrid.ORDER_STATUS_FIELD,
                          ExecutionReport.getStatusText(orderIn.status));

      record.setAttribute(OrderHistoryListGrid.ACTION_FIELD, "Cancel");

      ds.addData(record);

      orderMap.put(orderIn.orderKey, orderIn);

      orderListGridMap.put(orderIn.orderKey, record);

      if (WebTrader.getInstance().insertActiveInstrument(orderIn.instrument)) {
        WebTrader.getInstance().marketDataCanvas.insertSymbol(
            orderIn.instrument);
      };

    } else {

      orderMap.put(orderIn.orderKey, orderIn);

      ListGridRecord listGridRecord = orderListGridMap.get(orderIn.orderKey);

      listGridRecord.setAttribute(OrderHistoryListGrid.QTY_FIELD,
                                  orderIn.quantity);
      listGridRecord.setAttribute(OrderHistoryListGrid.FILLED_QTY_FIELD,
                                  orderIn.filled_quantity);
      listGridRecord.setAttribute(OrderHistoryListGrid.FILLED_PRICE_FIELD,
    		  						ConvertUtils.getDisplayPrice(instrument_with_ref_data, (int)orderIn.filled_avg_price ) );
      listGridRecord.setAttribute(OrderHistoryListGrid.LAST_TIMESTAMP_FIELD,
                                  orderIn.lastUpdateTime);
      listGridRecord.setAttribute(
          OrderHistoryListGrid.LAST_EXEC_REPORT_ID_FIELD,
          orderIn.lastExecutionReportId);
      listGridRecord.setAttribute(
          OrderHistoryListGrid.ORDER_STATUS_FIELD,
          ExecutionReport.getStatusText(orderIn.status));

      updateData(listGridRecord);
    }

    fetchData();
  }

  private HashMap<OrderKey, Order> orderMap = new HashMap<OrderKey, Order>();
  private HashMap<OrderKey, ListGridRecord> orderListGridMap =
      new HashMap<OrderKey, ListGridRecord>();
}