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
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.DragDataAction;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.TransferImgButton;
import com.smartgwt.client.widgets.TransferImgButton.TransferImg;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.HStack;
import com.smartgwt.client.widgets.layout.VStack;
import java.util.ArrayList;
import org.DistributedATS.shared.Instrument;

public class InstrumentPicker extends Window {

  public static String INSTRUMENT_NAME_FIELD = "INSTRUMENT_NAME_FIELD";
  public static String EXCHANGE_NAME_FIELD = "EXCHANGE_NAME";
  public static String SYMBOL_FIELD = "SYMBOL_NAME";

  public InstrumentPicker(final MarketDataCanvas canvas) {
    setTitle("Instrument Picker");
    HStack hStack = new HStack(10);
    hStack.setHeight(300);

    final ListGrid instrumentListGrid1 = new ListGrid();
    instrumentListGrid1.setWidth(250);
    instrumentListGrid1.setHeight(550);
    instrumentListGrid1.setID("instrumentListGrid1");
    instrumentListGrid1.setShowAllRecords(true);
    instrumentListGrid1.setCanReorderRecords(true);
    instrumentListGrid1.setCanDragRecordsOut(true);
    instrumentListGrid1.setCanAcceptDroppedRecords(true);
    instrumentListGrid1.setDragDataAction(DragDataAction.MOVE);

    ListGridField instrumentNameField =
        new ListGridField(INSTRUMENT_NAME_FIELD, INSTRUMENT_NAME_FIELD);
    instrumentNameField.setAlign(Alignment.LEFT);

    ListGridField exchangeNameField =
        new ListGridField(EXCHANGE_NAME_FIELD, EXCHANGE_NAME_FIELD);
    exchangeNameField.setAlign(Alignment.LEFT);

    ListGridField symbolField = new ListGridField(SYMBOL_FIELD, SYMBOL_FIELD);
    symbolField.setAlign(Alignment.LEFT);

    int user_securities_size = 0;

    final ArrayList<Instrument> userSecurities =
        WebTrader.getInstance().getActiveSecurityList();
    final ArrayList<Instrument> allSecurities =
        WebTrader.getInstance().getCompleteSecurityList();

    if (userSecurities != null)
      user_securities_size = userSecurities.size();

    ListGridRecord[] listGridRecordsAllSecurities =
        new ListGridRecord[allSecurities.size() - user_securities_size];

    int index = 0;
    for (Instrument instrument : allSecurities) {
      if (userSecurities.contains(instrument))
        continue;

      listGridRecordsAllSecurities[index] = new ListGridRecord();
      listGridRecordsAllSecurities[index].setAttribute(
          INSTRUMENT_NAME_FIELD, instrument.getInstrumentName());
      listGridRecordsAllSecurities[index].setAttribute(
          EXCHANGE_NAME_FIELD, instrument.getSecurityExchange());
      listGridRecordsAllSecurities[index].setAttribute(SYMBOL_FIELD,
                                                       instrument.getSymbol());

      index++;
    }

    instrumentListGrid1.setFields(instrumentNameField, exchangeNameField,
                                  symbolField);
    instrumentListGrid1.setData(listGridRecordsAllSecurities);
    hStack.addMember(instrumentListGrid1);

    final ListGrid instrumentListGrid2 = new ListGrid();
    instrumentListGrid2.setWidth(250);
    instrumentListGrid2.setHeight(550);
    instrumentListGrid2.setLeft(350);
    instrumentListGrid2.setID("instrumentListGrid2");
    instrumentListGrid2.setShowAllRecords(true);
    instrumentListGrid2.setEmptyMessage("Drop Rows Here");
    instrumentListGrid2.setCanReorderFields(true);
    instrumentListGrid2.setCanDragRecordsOut(true);
    instrumentListGrid2.setCanAcceptDroppedRecords(true);
    instrumentListGrid2.setDragDataAction(DragDataAction.MOVE);

    if (userSecurities != null && userSecurities.size() > 0) {

      ListGridRecord[] listGridRecordsUserSecurities =
          new ListGridRecord[userSecurities.size()];

      index = 0;
      for (Instrument instrument : userSecurities) {
        listGridRecordsUserSecurities[index] = new ListGridRecord();
        listGridRecordsUserSecurities[index].setAttribute(
            INSTRUMENT_NAME_FIELD, instrument.getInstrumentName());
        listGridRecordsUserSecurities[index].setAttribute(
            EXCHANGE_NAME_FIELD, instrument.getSecurityExchange());
        listGridRecordsUserSecurities[index].setAttribute(
            SYMBOL_FIELD, instrument.getSymbol());

        index++;
      }

      instrumentListGrid2.setData(listGridRecordsUserSecurities);
    }

    instrumentListGrid2.setFields(instrumentNameField, exchangeNameField,
                                  symbolField);

    VStack vStack = new VStack(3);
    vStack.setWidth(20);
    vStack.setLayoutAlign(VerticalAlignment.CENTER);
    vStack.setHeight(74);

    Button right = new Button(">");
    right.addClickHandler(
        new com.smartgwt.client.widgets.events.ClickHandler() {
          public void onClick(
              com.smartgwt.client.widgets.events.ClickEvent event) {
            instrumentListGrid2.transferSelectedData(instrumentListGrid1);
          }
        });

    Button left = new Button("<");
    left.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
      public void onClick(com.smartgwt.client.widgets.events.ClickEvent event) {
        instrumentListGrid1.transferSelectedData(instrumentListGrid2);
      }
    });
    vStack.addMember(right);
    vStack.addMember(left);

    hStack.addMember(vStack);

    hStack.addMember(instrumentListGrid2);

    // hStack.draw();

    addItem(hStack);

    final Window dialogWindow = this;

    Button done = new Button("Done");
    done.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
      public void onClick(com.smartgwt.client.widgets.events.ClickEvent event) {

        ListGridRecord[] selectedRows = instrumentListGrid2.getRecords();

        userSecurities.clear();
        canvas.clearGrids();

        for (ListGridRecord listGridRecord : selectedRows) {
          String exchangeName =
              listGridRecord.getAttribute(EXCHANGE_NAME_FIELD);
          String symbol = listGridRecord.getAttribute(SYMBOL_FIELD);

          Instrument instrument = new Instrument(exchangeName, symbol);

          WebTrader.getInstance().insertActiveInstrument(instrument);
          canvas.insertSymbol(instrument);

          dialogWindow.close();
        }
      }
    });

    HLayout buttonLayout = new HLayout();

    buttonLayout.setWidth100();
    buttonLayout.setHeight100();
    buttonLayout.setLayoutMargin(6);
    buttonLayout.setMembersMargin(6);
    buttonLayout.setAlign(Alignment.CENTER);

    buttonLayout.addMember(done);

    addItem(buttonLayout);
  }
}
