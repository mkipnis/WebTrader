package org.DistributedATS.client;

import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.fields.DataSourceDateTimeField;
import com.smartgwt.client.data.fields.DataSourceFloatField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import org.DistributedATS.shared.ExecutionReport;
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

      record.setAttribute(ExecutionReportListGrid.EXEC_REPORT_ID_FIELD,
                          execReport.executionReportId);
      record.setAttribute(ExecutionReportListGrid.EXEC_REPORT_TIMESTAMP_FIELD,
                          execReport.updateTime);
      record.setAttribute(ExecutionReportListGrid.EXEC_REPORT_STATUS_FIELD,
                          ExecutionReport.getStatusText(execReport.status));
      record.setAttribute(ExecutionReportListGrid.EXEC_REPORT_FILL_PRICE_FIELD,
                          execReport.fillPrice / PriceLevel.TICK_SIZE);
      record.setAttribute(ExecutionReportListGrid.EXEC_REPORT_FILL_QTY_FIELD,
                          execReport.fillQty);
      record.setAttribute(ExecutionReportListGrid.EXEC_REPORT_AVG_PRICE_FIELD,
                          execReport.filledAvgPx / PriceLevel.TICK_SIZE);
      record.setAttribute(ExecutionReportListGrid.EXEC_REPORT_CUM_QTY_FIELD,
                          execReport.cumFilledQty);

      ds.addData(record);
    }

    fetchData();
  }

  public Order getCurrentOrder() { return this.currentOrder; }

  private Order currentOrder = null;
}
