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

import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.fields.DataSourceDateTimeField;
import com.smartgwt.client.data.fields.DataSourceFloatField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;

import org.DistributedATS.shared.ConvertUtils;
import org.DistributedATS.shared.ExecutionReport;
import org.DistributedATS.shared.Instrument;
import org.DistributedATS.shared.Order;
import org.DistributedATS.shared.PriceLevel;

public class ExecutionReportListGrid extends ListGrid {

  public static String EXEC_REPORT_TIMESTAMP_FIELD = "EXEC_TIMESTAMP_FIELD";
  public static String EXEC_REPORT_ID_FIELD = "EXEC_REPORT_ID_FIELD";
  public static String EXEC_REPORT_STATUS_FIELD = "EXEC_REPORT_STATUS_FIELD";
  public static String EXEC_REPORT_FILL_PRICE_FIELD =
      "EXEC_REPORT_FILL_PRICE_FIELD";
  public static String EXEC_REPORT_FILL_QTY_FIELD =
      "EXEC_REPORT_FILL_QTY_FIELD";
  public static String EXEC_REPORT_AVG_PRICE_FIELD =
      "EXEC_REPORT_AVG_PRICE_FIELD";
  public static String EXEC_REPORT_CUM_QTY_FIELD = "EXEC_REPORT_CUM_QTY_FIELD";

  public ExecutionReportListGrid() {
    setWidth("99%");
    setHeight("99%");
    setAutoFetchData(true);
    setAlternateRecordStyles(true);

    ListGridField executionReportIdField = new ListGridField(
        ExecutionReportListGrid.EXEC_REPORT_ID_FIELD, "ExecReportId");
    executionReportIdField.setWidth(50);

    ListGridField updateTimestampField =
        new ListGridField(ExecutionReportListGrid.EXEC_REPORT_TIMESTAMP_FIELD,
                          "LastUpdateDate(UTC)");
    updateTimestampField.setWidth(140);

    ListGridField executionStatusField = new ListGridField(
        ExecutionReportListGrid.EXEC_REPORT_STATUS_FIELD, "Status");
    executionStatusField.setWidth(60);

    ListGridField fillPriceField = new ListGridField(
        ExecutionReportListGrid.EXEC_REPORT_FILL_PRICE_FIELD, "FillPrice");
    fillPriceField.setWidth(60);

    ListGridField fillQtyField = new ListGridField(
        ExecutionReportListGrid.EXEC_REPORT_FILL_QTY_FIELD, "FillQty");
    fillQtyField.setWidth(60);

    ListGridField avgPriceField = new ListGridField(
        ExecutionReportListGrid.EXEC_REPORT_AVG_PRICE_FIELD, "AvgPrice");
    avgPriceField.setWidth(60);

    ListGridField cumQtyField = new ListGridField(
        ExecutionReportListGrid.EXEC_REPORT_CUM_QTY_FIELD, "CumQty");
    cumQtyField.setWidth(60);

    DataSource ds = new DataSource();
    ds.setClientOnly(true);

    DataSourceTextField executionReportDS =
        new DataSourceTextField(ExecutionReportListGrid.EXEC_REPORT_ID_FIELD);
    executionReportDS.setPrimaryKey(true);
    ds.addField(executionReportDS);

    DataSourceDateTimeField updateTimestampDS = new DataSourceDateTimeField(
        ExecutionReportListGrid.EXEC_REPORT_TIMESTAMP_FIELD);
    updateTimestampDS.setFormat("MM/dd/yy HH:mm:ss.SSS");
    ds.addField(updateTimestampDS);

    DataSourceFloatField fillPriceFieldDS = new DataSourceFloatField(
        ExecutionReportListGrid.EXEC_REPORT_FILL_PRICE_FIELD);
    ds.addField(fillPriceFieldDS);

    DataSourceFloatField fillQtyFieldDS = new DataSourceFloatField(
        ExecutionReportListGrid.EXEC_REPORT_FILL_QTY_FIELD);
    ds.addField(fillQtyFieldDS);

    DataSourceFloatField avgPriceFieldDS = new DataSourceFloatField(
        ExecutionReportListGrid.EXEC_REPORT_AVG_PRICE_FIELD);
    ds.addField(avgPriceFieldDS);

    DataSourceFloatField cumQtyFieldDS = new DataSourceFloatField(
        ExecutionReportListGrid.EXEC_REPORT_CUM_QTY_FIELD);
    ds.addField(cumQtyFieldDS);

    setFields(executionReportIdField, updateTimestampField,
              executionStatusField, fillPriceField, fillQtyField, avgPriceField,
              cumQtyField);
    setDataSource(ds);

    setCanResizeFields(true);

    sort(ExecutionReportListGrid.EXEC_REPORT_TIMESTAMP_FIELD,
         SortDirection.DESCENDING);
  }

  public void updateExecutionReports(Order order) {
    DataSource ds = getDataSource();

    for (ListGridRecord record : this.getRecords()) {
      ds.removeData(record);
    }

    this.currentOrder = order;

    for (String execReportId : order.getExecutionReports().keySet()) {
      ExecutionReport execReport =
          order.getExecutionReports().get(execReportId);

      ListGridRecord record = new ListGridRecord();
      
      Instrument instrument_with_ref_data = WebTrader.getInstance().getInstrumentWithRefData(order.instrument);

      record.setAttribute(ExecutionReportListGrid.EXEC_REPORT_ID_FIELD,
                          execReport.executionReportId);
      record.setAttribute(ExecutionReportListGrid.EXEC_REPORT_TIMESTAMP_FIELD,
                          execReport.updateTime);
      record.setAttribute(ExecutionReportListGrid.EXEC_REPORT_STATUS_FIELD,
                          ExecutionReport.getStatusText(execReport.status));
      record.setAttribute(ExecutionReportListGrid.EXEC_REPORT_FILL_PRICE_FIELD,
    		  ConvertUtils.getDisplayPrice(instrument_with_ref_data, (int)execReport.fillPrice));
      record.setAttribute(ExecutionReportListGrid.EXEC_REPORT_FILL_QTY_FIELD,
                          execReport.fillQty); 
      record.setAttribute(ExecutionReportListGrid.EXEC_REPORT_AVG_PRICE_FIELD,
    		  ConvertUtils.getDisplayPrice(instrument_with_ref_data, (int)execReport.filledAvgPx));
      record.setAttribute(ExecutionReportListGrid.EXEC_REPORT_CUM_QTY_FIELD,
                          execReport.cumFilledQty);

      ds.addData(record);
    }

    fetchData();
  }

  public Order getCurrentOrder() { return this.currentOrder; }

  private Order currentOrder = null;
}
