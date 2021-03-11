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

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.TitleOrientation;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.FormItemValueFormatter;
import com.smartgwt.client.widgets.form.events.ItemChangeEvent;
import com.smartgwt.client.widgets.form.events.ItemChangedEvent;
import com.smartgwt.client.widgets.form.events.ItemChangedHandler;
import com.smartgwt.client.widgets.form.fields.BlurbItem;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.SpinnerItem;
import com.smartgwt.client.widgets.form.fields.SubmitItem;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import org.DistributedATS.shared.FIXUserSession;
import org.DistributedATS.shared.Order;

public class TicketCanvas extends Canvas {

  WebTrader webTrader;

  DynamicForm ticketForm = new DynamicForm();

  BlurbItem instrumentLabel = new BlurbItem();

  public class BuySellClickHandler
      implements com.smartgwt.client.widgets.form.fields.events.ClickHandler {

    public BuySellClickHandler() {}

    @Override
    public void onClick(ClickEvent event) {

      // TODO Auto-generated method stub
    	SpinnerItemWebTrader priceItem = (SpinnerItemWebTrader)ticketForm.getItem("Price");
      SpinnerItem quantityItem = (SpinnerItem)ticketForm.getItem("Quantity");

      Integer price_in_ticks = priceItem.getValueInTicks();
      Integer quantity = ((Integer)quantityItem.getValue());

      final FIXUserSession fixUserSession =
          UserSessionSingleton.getInstance().getFIXUserSession();

      webTrader.getFIXService().submitOrder(
          fixUserSession.username, fixUserSession.token,
          event.getItem().getName(), webTrader.getActiveInstrument(),
          price_in_ticks, quantity,
          new AsyncCallback<Order>() {
            @Override
            public void onFailure(Throwable caught) {
              // TODO Auto-generated method stub
            }

            @Override
            public void onSuccess(Order order) {
              // TODO Auto-generated method stub
              webTrader.orderHistoryCanvas.orderHistoryListGrid
                  .insertUpdateOrder(order);
            }
          });
    }
  }

  public void populateTicket(Integer price, Integer quantity) {
	SpinnerItemWebTrader priceItem = (SpinnerItemWebTrader)ticketForm.getItem("Price");
    SpinnerItem quantityItem = (SpinnerItem)ticketForm.getItem("Quantity");

    instrumentLabel.setValue(
        "<font color='blue'>" +
        webTrader.getActiveInstrument().getInstrumentName() + "</font color>");

    priceItem.setValue( price, webTrader.getActiveInstrument() );
    //priceItem.setValue("100-25");

    quantityItem.setValue(quantity);    
  }

  public class CancelAllClickHandler
      implements com.smartgwt.client.widgets.form.fields.events.ClickHandler {

    public CancelAllClickHandler() {}

    @Override
    public void onClick(ClickEvent event) {
      // TODO Auto-generated method stub

      final FIXUserSession fixUserSession =
          UserSessionSingleton.getInstance().getFIXUserSession();

      webTrader.getFIXService().cancelAllOrders(
          fixUserSession.username, fixUserSession.token,
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

    DynamicForm ticketForm;
  }

  public TicketCanvas(WebTrader webTrader) {

	WebTraderSectionCanvas webTraderSectionCanvas = new WebTraderSectionCanvas("Ticket");
      
    this.webTrader = webTrader;

    ticketForm.setNumCols(10);
    ticketForm.setMargin(10);

    instrumentLabel.setDefaultValue(
        "<font color='black'>Select Instrument on the market data panel.</font>");

    SpinnerItemWebTrader priceSpinnerItem = new SpinnerItemWebTrader();
    priceSpinnerItem.setName("Price");
    priceSpinnerItem.setDefaultValue(1);
    priceSpinnerItem.setRowSpan(2);
    //priceSpinnerItem.setColSpan(2);
    priceSpinnerItem.setTitleOrientation(TitleOrientation.TOP);
  //  priceSpinnerItem.setMin(0.05);
  //  priceSpinnerItem.setStep(0.05);
    priceSpinnerItem.setStartRow(false);
    priceSpinnerItem.setEndRow(false);
   

    SpinnerItem quantitySpinnerItem = new SpinnerItem();
    quantitySpinnerItem.setName("Quantity");
    quantitySpinnerItem.setDefaultValue(100);
    quantitySpinnerItem.setMin(1);
   quantitySpinnerItem.setStep(10);
    // quantitySpinnerItem.setWrapTitle(true);
    quantitySpinnerItem.setRowSpan(2);
    quantitySpinnerItem.setTitleOrientation(TitleOrientation.TOP);
    quantitySpinnerItem.setStartRow(false);
    quantitySpinnerItem.setEndRow(true);
    
 
    
  

    SubmitItem buyButton = new SubmitItem();
    buyButton.setTitle("Buy");
    buyButton.setName("Buy");
    buyButton.setStartRow(true);
    buyButton.setEndRow(false);
    buyButton.setHeight(40);
    buyButton.setWidth(40);
    buyButton.setAlign(Alignment.RIGHT);

    SubmitItem sellButton = new SubmitItem();
    sellButton.setTitle("Sell");
    sellButton.setName("Sell");
    sellButton.setHeight(40);
    sellButton.setWidth(40);
    sellButton.setStartRow(false);
    sellButton.setEndRow(false);

    SubmitItem cancelAllButton = new SubmitItem();
    cancelAllButton.setTitle("Cancel All");
    cancelAllButton.setName("CancelAll");
    cancelAllButton.setHeight(40);
    cancelAllButton.setWidth(80);
    cancelAllButton.setAlign(Alignment.RIGHT);
    cancelAllButton.setStartRow(false);
    cancelAllButton.setEndRow(true);

    buyButton.addClickHandler(new BuySellClickHandler());
    sellButton.addClickHandler(new BuySellClickHandler());
    cancelAllButton.addClickHandler(new CancelAllClickHandler());

    ticketForm.setFields(instrumentLabel, priceSpinnerItem, quantitySpinnerItem,
                         buyButton, sellButton, cancelAllButton);
    ticketForm.draw();
    
    ticketForm.addItemChangedHandler(new ItemChangedHandler() {
        @Override
        public void onItemChanged(ItemChangedEvent itemChangedEvent) {
          //  SC.say("Spinner value changed! (ItemChangedEvent)");
        }
    });
   
    
    webTraderSectionCanvas.setSectionStackCanvas(ticketForm);
    
    addChild(webTraderSectionCanvas);
  }
}
