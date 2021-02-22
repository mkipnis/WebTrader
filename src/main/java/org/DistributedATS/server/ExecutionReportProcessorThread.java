package org.DistributedATS.server;

import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import org.DistributedATS.shared.ExecutionReport;
import org.DistributedATS.shared.Instrument;
import org.DistributedATS.shared.Order;
import org.DistributedATS.shared.OrderKey;
import quickfix.FieldNotFound;
import quickfix.Message;

public class ExecutionReportProcessorThread implements Runnable {

  BlockingQueue<FIXMessageBlock> executionReportQueue =
      new LinkedBlockingDeque<FIXMessageBlock>();

  private OrderMan orderMan = null;
  private PositionMan positionMan = null;

  private long sequenceNumber =
      -1; // for client side to query for execution reports, since the last one,
          // client will pass last sequence number it received

  ExecutionReportProcessorThread(OrderMan orderMan, PositionMan positionMan) {
    this.orderMan = orderMan;
    this.positionMan = positionMan;
  }

  public void enqueueExecutionReport(FIXMessageBlock executionReport) {
    try {
      executionReportQueue.put(executionReport);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    // TODO Auto-generated method stub
    try {
      while (true) {

        FIXMessageBlock executionReport = executionReportQueue.take();

        processExecutionReport(executionReport);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void processExecutionReport(FIXMessageBlock messageBlock) {
    Message message = messageBlock.fixMessage;
    FIXSessionID fixSessionID = messageBlock.fixSessionID;

    try {

      quickfix.field.OrderID fixOrderId = new quickfix.field.OrderID();
      message.getField(fixOrderId);

      quickfix.field.SendingTime sendTime = new quickfix.field.SendingTime();
      message.getHeader().getField(sendTime);

      // Date lastUpdateTime = new Date();
      // //java.util.Date.from(sendTime.getValue().atZone(ZoneId.systemDefault()).toInstant());

      quickfix.field.Side side = new quickfix.field.Side();
      message.getField(side);

      quickfix.field.OrderQty orderQuantity = new quickfix.field.OrderQty();
      message.getField(orderQuantity);

      quickfix.field.CumQty cumQty = new quickfix.field.CumQty();
      message.getField(cumQty);

      quickfix.field.LeavesQty leavesQty = new quickfix.field.LeavesQty();
      message.getField(leavesQty);

      quickfix.field.ExecID execId = new quickfix.field.ExecID();
      message.getField(execId);

      quickfix.field.Price price = new quickfix.field.Price();
      message.getField(price);

      quickfix.field.LastPx lastPrice = new quickfix.field.LastPx();
      if (message.isSetField(lastPrice))
        message.getField(lastPrice);

      quickfix.field.LastQty lastQty = new quickfix.field.LastQty();
      if (message.isSetField(lastQty))
        message.getField(lastQty);

      quickfix.field.AvgPx avgPx = new quickfix.field.AvgPx();
      message.getField(avgPx);

      quickfix.field.OrdStatus ordStatus = new quickfix.field.OrdStatus();
      message.getField(ordStatus);

      quickfix.field.TransactTime transactTime =
          new quickfix.field.TransactTime();
      message.getField(transactTime);

      Date lastUpdateTime = Date.from(
          transactTime.getValue().atZone(ZoneId.systemDefault()).toInstant());
      ;

      System.out.println(
          "Transact Times : " +
          transactTime.getValue()); // ":" + transactTime.getValue() + ":" +
                                    // lastUpdateTime + ":" + lastUpdateTime );

      String orderID = fixOrderId.getValue();
      String executionReportId = execId.getValue();

      Order order = orderMan.getOrder(fixSessionID, new OrderKey(orderID));

      if (order == null) {
        order = new Order();
        order.orderKey = new OrderKey(orderID);

        quickfix.field.Symbol symbol = new quickfix.field.Symbol();
        message.getField(symbol);

        quickfix.field.SecurityExchange securityExchange =
            new quickfix.field.SecurityExchange();
        message.getField(securityExchange);

        order.instrument =
            new Instrument(securityExchange.getValue(), symbol.getValue());

        order.status = ordStatus.getValue();
      }

      if (order.status == ExecutionReport.PENDING_NEW ||
          ordStatus.getValue() == ExecutionReport.CANCELLED)
        order.status = ordStatus.getValue();

      //
      // Lets make sure we process the latest execution report.
      // i.e. real-time fill may arrive before or during execution reports
      // published for mass status request
      if (order.filled_quantity <= cumQty.getValue()) // the latest fill
      {
        synchronized (positionMan) {
          positionMan.updatePosition(fixSessionID, order,
                                     order.filled_quantity * -1,
                                     order.filled_avg_price);

          order.price = price.getValue();
          order.quantity = orderQuantity.getValue();
          order.filled_quantity = cumQty.getValue();
          order.lastUpdateTime = lastUpdateTime;
          order.side = side.getValue();
          order.filled_avg_price = avgPx.getValue();
          order.lastExecutionReportId = executionReportId;

          if (order.status != ExecutionReport.CANCELLED)
            order.status = ordStatus.getValue(); // latest status

          positionMan.updatePosition(fixSessionID, order, order.filled_quantity,
                                     order.filled_avg_price);
        }
      }

      ExecutionReport orderExecutionReport = new ExecutionReport();
      orderExecutionReport.executionReportId = executionReportId;
      orderExecutionReport.cumFilledQty = cumQty.getValue();
      orderExecutionReport.filledAvgPx = avgPx.getValue();
      orderExecutionReport.fillPrice = lastPrice.getValue();
      orderExecutionReport.fillQty = lastQty.getValue();
      orderExecutionReport.leavedQty = leavesQty.getValue();
      orderExecutionReport.updateTime = lastUpdateTime;
      orderExecutionReport.status = ordStatus.getValue();

      System.out.println("Order Id : " + order.orderKey.getOrderKey() +
                         "| Execution Report : " + executionReportId + "|" +
                         orderExecutionReport.cumFilledQty + "|" +
                         orderExecutionReport.filledAvgPx + "|" +
                         orderExecutionReport.fillPrice + "|" +
                         orderExecutionReport.fillQty + "|" +
                         orderExecutionReport.leavedQty +
                         "| Last Update Time : " + lastUpdateTime + " | " +
                         orderExecutionReport.status);

      order.insertExecutionReport(executionReportId, orderExecutionReport,
                                  ++sequenceNumber);
      System.out.println("Execution Report Size : " +
                         order.getExecutionReports().size());

      orderMan.insertUpdateOrder(fixSessionID, order.orderKey, order);

    } catch (FieldNotFound e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } /*catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
    }*/
  }
}
