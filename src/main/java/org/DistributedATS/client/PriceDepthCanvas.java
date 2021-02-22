package org.DistributedATS.client;

import com.google.gwt.i18n.client.NumberFormat;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.fields.DataSourceFloatField;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import java.util.ArrayList;
import org.DistributedATS.shared.MarketDataSnapshot;
import org.DistributedATS.shared.PriceLevel;

public class PriceDepthCanvas extends Canvas {

  public static String PRICE_LEVEL_INDEX_FIELD = "PRICE_LEVEL_INDEX_FIELD";
  public static String BID_PRICE_FIELD = "BID_PRICE_FIELD";
  public static String BID_SIZE_FIELD = "BID_SIZE_FIELD";
  public static String ASK_PRICE_FIELD = "ASK_PRICE_FIELD";
  public static String ASK_SIZE_FIELD = "ASK_SIZE_FIELD";

  final ListGrid listGrid = new ListGrid();

  public PriceDepthCanvas() {
    setWidth100();
    setHeight100();

    listGrid.setWidth100();
    listGrid.setHeight100();

    listGrid.setShowRecordComponents(true);
    listGrid.setShowRecordComponentsByCell(true);
    listGrid.setAlternateRecordStyles(true);

    listGrid.setAutoFetchData(true);

    ListGridField priceLevelIndexField =
        new ListGridField(PriceDepthCanvas.PRICE_LEVEL_INDEX_FIELD,
                          PriceDepthCanvas.PRICE_LEVEL_INDEX_FIELD);
    ListGridField bidPriceField = new ListGridPriceField(
        PriceDepthCanvas.BID_PRICE_FIELD, PriceDepthCanvas.BID_PRICE_FIELD);
    ListGridField askPriceField = new ListGridPriceField(
        PriceDepthCanvas.ASK_PRICE_FIELD, PriceDepthCanvas.ASK_PRICE_FIELD);
    ListGridField bidSizeField = new ListGridField(
        PriceDepthCanvas.BID_SIZE_FIELD, PriceDepthCanvas.BID_SIZE_FIELD);
    ListGridField askSizeField = new ListGridField(
        PriceDepthCanvas.ASK_SIZE_FIELD, PriceDepthCanvas.ASK_SIZE_FIELD);

    priceLevelIndexField.setHidden(true);

    DataSource ds = new DataSource();
    ds.setClientOnly(true);

    DataSourceIntegerField priceLevelIndexDS =
        new DataSourceIntegerField(PriceDepthCanvas.PRICE_LEVEL_INDEX_FIELD);
    priceLevelIndexDS.setPrimaryKey(true);
    ds.addField(priceLevelIndexDS);

    DataSourceFloatField bidPriceDS =
        new DataSourceFloatField(PriceDepthCanvas.BID_PRICE_FIELD);
    ds.addField(bidPriceDS);

    DataSourceFloatField askPriceDS =
        new DataSourceFloatField(PriceDepthCanvas.ASK_PRICE_FIELD);
    ds.addField(askPriceDS);

    DataSourceFloatField bidSizeDS =
        new DataSourceFloatField(PriceDepthCanvas.BID_SIZE_FIELD);
    ds.addField(bidSizeDS);

    DataSourceFloatField askSizeDS =
        new DataSourceFloatField(PriceDepthCanvas.ASK_SIZE_FIELD);
    ds.addField(askSizeDS);

    listGrid.setFields(priceLevelIndexField, bidPriceField, askPriceField,
                       bidSizeField, askSizeField);
    listGrid.setDataSource(ds);

    addChild(listGrid);
  }

  public void updatePriceDepth(MarketDataSnapshot marketDataSnapshot) {
    DataSource ds = listGrid.getDataSource();

    for (ListGridRecord record : currentRecords) {
      ds.removeData(record);
    }

    currentRecords.clear();

    for (int level = 0; level < 5; level++) {
      ListGridRecord record = new ListGridRecord();

      PriceLevel bid = marketDataSnapshot.getBidSide().get(level);
      PriceLevel ask = marketDataSnapshot.getAskSide().get(level);

      record.setAttribute(PRICE_LEVEL_INDEX_FIELD, level);

      if (bid != null) {
        if (bid.getSize() > 0) {
          record.setAttribute(BID_PRICE_FIELD, bid.getPrice());
          record.setAttribute(BID_SIZE_FIELD, bid.getSize());
        } else {
          record.setAttribute(BID_PRICE_FIELD, "");
          record.setAttribute(BID_SIZE_FIELD, "");
        }
      }

      if (ask != null) {
        if (ask.getSize() > 0) {
          record.setAttribute(ASK_PRICE_FIELD, ask.getPrice());
          record.setAttribute(ASK_SIZE_FIELD, ask.getSize());
        } else {
          record.setAttribute(ASK_PRICE_FIELD, "");
          record.setAttribute(ASK_SIZE_FIELD, "");
        }
      }

      ds.addData(record);

      currentRecords.add(record);
    }
  }

  ArrayList<ListGridRecord> currentRecords = new ArrayList<ListGridRecord>();
}