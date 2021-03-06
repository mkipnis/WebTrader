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

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.fields.DataSourceDateTimeField;
import com.smartgwt.client.data.fields.DataSourceFloatField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;

import java.util.ArrayList;
import java.util.HashMap;
import org.DistributedATS.shared.ExecutionReport;
import org.DistributedATS.shared.FIXUserSession;
import org.DistributedATS.shared.Order;
import org.DistributedATS.shared.OrderKey;
import org.DistributedATS.shared.PriceLevel;

public class OrderHistoryCanvas extends Canvas {

  ExecutionReportListGrid executionReportListGrid =
      new ExecutionReportListGrid();
  OrderHistoryListGrid orderHistoryListGrid =
      new OrderHistoryListGrid(executionReportListGrid);

  public OrderHistoryCanvas() {
      
	WebTraderSectionCanvas orderHistorySectionCanvas = new WebTraderSectionCanvas("Order History");
	orderHistorySectionCanvas.setSectionStackCanvas(orderHistoryListGrid);
	  
    Canvas orderHistoryCanvas = new Canvas();
    orderHistoryCanvas.addChild(orderHistorySectionCanvas);
    orderHistoryCanvas.setWidth("70%");
    orderHistoryCanvas.setHeight100();
    orderHistoryCanvas.setShowResizeBar(true);
    
    
	WebTraderSectionCanvas executionReportSectionCanvas = new WebTraderSectionCanvas("Execution Reports");
	executionReportSectionCanvas.setSectionStackCanvas(executionReportListGrid);

    Canvas executionReportCanvas = new Canvas();
    executionReportCanvas.addChild(executionReportSectionCanvas);
    executionReportCanvas.setWidth("30%");
    executionReportCanvas.setHeight100();
    executionReportCanvas.setShowResizeBar(true);

    HLayout orderHistoryLayout = new HLayout();

    orderHistoryLayout.addMember(orderHistoryCanvas);
    orderHistoryLayout.addMember(executionReportCanvas);

    orderHistoryLayout.setWidth100();
    orderHistoryLayout.setHeight100();
    orderHistoryLayout.setShowResizeBar(true);

    this.setMargin(10);

    addChild(orderHistoryLayout);
  }

  public long insertUpdateOrders(HashMap<OrderKey, Order> orders) {
    long max_order_number_sequence = -1;

    Order executionReportTrailOrder =
        this.executionReportListGrid
            .getCurrentOrder(); // to automatically update execution report
                                // trail

    for (OrderKey orderKey : orders.keySet()) {
      Order order = orders.get(orderKey);

      if (order.sequenceNumber >= max_order_number_sequence)
        max_order_number_sequence = order.sequenceNumber;

      orderHistoryListGrid.insertUpdateOrder(order);

      // To automatically report execution trail
      if (executionReportTrailOrder != null &&
          executionReportTrailOrder.orderKey.equals(orderKey)) {
        this.executionReportListGrid.updateExecutionReports(order);
      }
    };

    return max_order_number_sequence;
  }
}
