package org.DistributedATS.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import org.DistributedATS.client.FIXServiceInterface;
import org.DistributedATS.shared.ExecutionReport;
import org.DistributedATS.shared.FIXUserSession;
import org.DistributedATS.shared.Instrument;
import org.DistributedATS.shared.MarketDataSnapshot;
import org.DistributedATS.shared.Order;
import org.DistributedATS.shared.OrderKey;
import org.DistributedATS.shared.Position;
import org.DistributedATS.shared.PriceLevel;
import org.DistributedATS.shared.PriceLevelKey;
import org.DistributedATS.shared.SessionStateRequest;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionStateListener;
import quickfix.field.ClOrdID;
import quickfix.field.HandlInst;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.SecurityExchange;
import quickfix.field.SecurityListRequestType;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;

public class FIXServiceInterfaceImpl extends RemoteServiceServlet
    implements FIXServiceInterface, SessionStateListener {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private static long _order_index = 0;

  public static String getNextRequestID(SessionID sessionID) {
    String orderID = new Long(System.currentTimeMillis() / 1000).toString() +
                     ":" + sessionID.toString() + ":" + ++_order_index;

    return orderID;
  }

  @Override
  public FIXUserSession logon(String username, String password) {
    FIXClientContextListener fixClientContextListener =
        (FIXClientContextListener)getServletContext().getAttribute(
            "FIXContextListner");

    FIXUserSession fixUserSession =
        fixClientContextListener.sendLogon(username, password, this);

    return fixUserSession;
  }

  @Override
  public FIXUserSession logout(String username) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order submitOrder(String username, String token, String buy_or_sell,
                           Instrument instrument, Double price,
                           Double quantity) {
    // TODO Auto-generated method stub
    System.out.println(username + " : "
                       + " : " + buy_or_sell + " : " +
                       instrument.getInstrumentName() + " : " + price + " : " +
                       quantity);

    FIXClientContextListener fixClientContextListener =
        (FIXClientContextListener)getServletContext().getAttribute(
            "FIXContextListner");

    SessionID sessionID =
        fixClientContextListener.getSessionID(username, token);

    Session currentSession = Session.lookupSession(sessionID);

    Order order = new Order();

    Side side;

    if (buy_or_sell.compareToIgnoreCase("BUY") == 0)
      side = new Side(Side.BUY);
    else
      side = new Side(Side.SELL);

    order.side = side.getValue();

    order.orderKey = OrderKey.getNextOrderID(instrument);
    order.orderType = OrdType.LIMIT;

    quickfix.fix44.NewOrderSingle newOrderSingle =
        new quickfix.fix44.NewOrderSingle(
            new ClOrdID(order.orderKey.getOrderKey()), side, new TransactTime(),
            new OrdType(order.orderType));

    price = price * PriceLevel.TICK_SIZE;

    newOrderSingle.set(new OrderQty(quantity));
    newOrderSingle.set(new Symbol(instrument.getSymbol()));
    newOrderSingle.set(new SecurityExchange(instrument.getSecurityExchange()));
    newOrderSingle.set(new HandlInst('1'));
    newOrderSingle.set(new Price(price));

    order.price = price;
    order.quantity = quantity;
    order.instrument = instrument;
    order.status = ExecutionReport.PENDING_NEW;

    ExecutionReport execReport = new ExecutionReport();
    execReport.updateTime = new Date();
    execReport.status = ExecutionReport.PENDING_NEW;
    order.insertExecutionReport("0", execReport, 1);

    fixClientContextListener.orderMan.insertUpdateOrder(sessionID,
                                                        order.orderKey, order);

    currentSession.send(newOrderSingle);

    return order;
  }

  @Override
  public FIXUserSession
  getLastestSessionState(SessionStateRequest sessionStateRequest) {
    // TODO Auto-generated method stub

    FIXClientContextListener fixClientContextListener =
        (FIXClientContextListener)getServletContext().getAttribute(
            "FIXContextListner");

    FIXUserSession sessionState =
        fixClientContextListener.getSessionState(sessionStateRequest.token);

    // unknown token
    if (sessionState == null) {
      return null;
    }

    SessionID sessionID = fixClientContextListener.getSessionID(
        sessionState.username, sessionStateRequest.token);

    if ((sessionStateRequest.stateMask &
         (1 << FIXUserSession.LOGON_STATE_BIT)) != 0) {
      Session session = Session.lookupSession(sessionID);

      if (session.isLoggedOn()) {
        sessionState.sessionState = FIXUserSession.STATE_SUCCESSFUL_LOGIN;
      } else if (session.isLogonAlreadySent()) {
        sessionState.sessionState = FIXUserSession.STATE_PENDING_LOGON;
      } else if (session.isLogoutReceived()) {
        sessionState.sessionState = FIXUserSession.STATE_LOGGED_OUT;
      }
    }

    if ((sessionStateRequest.stateMask &
         (1 << FIXUserSession.SECURITY_LIST_BIT)) != 0) {
      sessionState.activeSecurityList = new ArrayList<Instrument>(
          fixClientContextListener.application.getSecurities());

      submitMarketDataRequest(sessionState.username, sessionState.token,
                              sessionState.activeSecurityList);
      submitMassOrderStatusRequest(sessionState.username, sessionState.token);
    }

    if ((sessionStateRequest.stateMask &
         (1 << FIXUserSession.MARKET_DATA_BIT)) != 0) {
      fixClientContextListener.marketDataProcessor.populateMarketDataSnapshots(
          sessionID, sessionStateRequest, sessionState);
    }

    if ((sessionStateRequest.stateMask &
         (1 << FIXUserSession.ORDERS_DATA_BIT)) != 0) {
      HashMap<OrderKey, Order> ordersOut = new HashMap<OrderKey, Order>();

      fixClientContextListener.populateLastestOrderState(
          sessionID, sessionStateRequest.maxOrderSequenceNumber, ordersOut);

      sessionState.orders = ordersOut;

      synchronized (fixClientContextListener.positionMan) {
        HashMap<Instrument, Position> positions =
            fixClientContextListener.positionMan.positions.get(sessionID);
        if (positions != null)
          sessionState.positionsMap.putAll(positions);
      }
    }
    
    fixClientContextListener.getSessionManagerThread().setSessionTimestamp(sessionID);
    
    sessionState.stateMask = sessionStateRequest.stateMask;

    fixClientContextListener.setSessionState(sessionStateRequest.token,
                                             sessionState);

    return sessionState;
  }

  public String submitMarketDataRequest(String username, String token,
                                        ArrayList<Instrument> instruments) {
    // TODO Auto-generated method stub

    FIXClientContextListener fixClientContextListener =
        (FIXClientContextListener)getServletContext().getAttribute(
            "FIXContextListner");

    SessionID sessionID =
        fixClientContextListener.getSessionID(username, token);

    Session session = Session.lookupSession(sessionID);

    if (session != null) {
      String requestID = getNextRequestID(sessionID);
      quickfix.field.MDReqID mdRequestID =
          new quickfix.field.MDReqID(requestID);
      quickfix.field.SubscriptionRequestType subscriptionRequestType =
          new quickfix.field.SubscriptionRequestType(
              quickfix.field.SubscriptionRequestType.SNAPSHOT_UPDATES);

      quickfix.field.MarketDepth marketDepth =
          new quickfix.field.MarketDepth(0);

      quickfix.fix44.MarketDataRequest marketDataRequest =
          new quickfix.fix44.MarketDataRequest(
              mdRequestID, subscriptionRequestType, marketDepth);

      quickfix.fix44.MarketDataRequest.NoMDEntryTypes noMDEntryTypeBid =
          new quickfix.fix44.MarketDataRequest.NoMDEntryTypes();
      quickfix.field.MDEntryType entryTypeBid =
          new quickfix.field.MDEntryType(quickfix.field.MDEntryType.BID);
      noMDEntryTypeBid.setField(entryTypeBid);
      marketDataRequest.addGroup(noMDEntryTypeBid);

      quickfix.fix44.MarketDataRequest.NoMDEntryTypes noMDEntryTypeAsk =
          new quickfix.fix44.MarketDataRequest.NoMDEntryTypes();
      quickfix.field.MDEntryType entryTypeAsk =
          new quickfix.field.MDEntryType(quickfix.field.MDEntryType.OFFER);
      noMDEntryTypeAsk.setField(entryTypeAsk);
      marketDataRequest.addGroup(noMDEntryTypeAsk);

      for (Instrument instrument : instruments) {
        quickfix.field.Symbol fixSymbol =
            new quickfix.field.Symbol(instrument.getSymbol());
        quickfix.field.SecurityExchange fixSecurityExchange =
            new quickfix.field.SecurityExchange(
                instrument.getSecurityExchange());

        quickfix.fix44.MarketDataRequest.NoRelatedSym fixSymbolGroup =
            new quickfix.fix44.MarketDataRequest.NoRelatedSym();
        fixSymbolGroup.setField(fixSymbol);
        fixSymbolGroup.setField(fixSecurityExchange);
        marketDataRequest.addGroup(fixSymbolGroup);
      }

      session.send(marketDataRequest);

      return requestID;
    }

    return null;
  }

  public String submitMassOrderStatusRequest(String username, String token) {
    // TODO Auto-generated method stub

    FIXClientContextListener fixClientContextListener =
        (FIXClientContextListener)getServletContext().getAttribute(
            "FIXContextListner");

    SessionID sessionID =
        fixClientContextListener.getSessionID(username, token);

    Session session = Session.lookupSession(sessionID);

    if (session != null) {
      String requestID = getNextRequestID(sessionID);
      ;

      quickfix.field.MassStatusReqID massStatusRequestID =
          new quickfix.field.MassStatusReqID(requestID);

      quickfix.fix44.OrderMassStatusRequest orderMassStatusRequest =
          new quickfix.fix44.OrderMassStatusRequest(
              massStatusRequestID,
              new quickfix.field.MassStatusReqType(
                  quickfix.field.MassStatusReqType.STATUS_FOR_ALL_ORDERS));

      session.send(orderMassStatusRequest);

      return requestID;
    }

    return null;
  }

  @Override
  public String cancelOrder(String username, String token, Order orderIn) {
    // TODO Auto-generated method stub
    FIXClientContextListener fixClientContextListener =
        (FIXClientContextListener)getServletContext().getAttribute(
            "FIXContextListner");

    SessionID sessionID =
        fixClientContextListener.getSessionID(username, token);

    Session session = Session.lookupSession(sessionID);

    if (session != null) {
      String cancelRequestID = getNextRequestID(sessionID);
      ;

      quickfix.field.OrigClOrdID origClOrdID =
          new quickfix.field.OrigClOrdID(orderIn.orderKey.getOrderKey());
      quickfix.field.ClOrdID clOrdID =
          new quickfix.field.ClOrdID(cancelRequestID);
      quickfix.field.Side side = new quickfix.field.Side(orderIn.side);
      quickfix.field.TransactTime transactTime =
          new quickfix.field.TransactTime();

      quickfix.fix44.OrderCancelRequest orderCancelRequest =
          new quickfix.fix44.OrderCancelRequest(origClOrdID, clOrdID, side,
                                                transactTime);

      quickfix.field.Symbol symbol =
          new quickfix.field.Symbol(orderIn.instrument.getSymbol());
      orderCancelRequest.setField(symbol);
      quickfix.field.SecurityExchange securityExchange =
          new quickfix.field.SecurityExchange(
              orderIn.instrument.getSecurityExchange());
      orderCancelRequest.setField(securityExchange);

      session.send(orderCancelRequest);

      return cancelRequestID;
    }

    return null;
  }

  @Override
  public String cancelAllOrders(String username, String token) {
    // TODO Auto-generated method stub

    FIXClientContextListener fixClientContextListener =
        (FIXClientContextListener)getServletContext().getAttribute(
            "FIXContextListner");

    SessionID sessionID =
        fixClientContextListener.getSessionID(username, token);

    Session session = Session.lookupSession(sessionID);

    if (session != null) {
      String cancelRequestID = getNextRequestID(sessionID);
      quickfix.field.ClOrdID clOrdID =
          new quickfix.field.ClOrdID(cancelRequestID);
      quickfix.field.MassCancelRequestType massCancelRequestType =
          new quickfix.field.MassCancelRequestType(
              quickfix.field.MassCancelRequestType.CANCEL_ALL_ORDERS);

      quickfix.field.TransactTime transactTime =
          new quickfix.field.TransactTime();

      quickfix.fix44.OrderMassCancelRequest massCancelRequest =
          new quickfix.fix44.OrderMassCancelRequest(
              clOrdID, massCancelRequestType, transactTime);

      session.send(massCancelRequest);
    }

    return null;
  }

  @Override
  public void onConnect() {
    // TODO Auto-generated method stub
  }

  @Override
  public void onDisconnect() {
    // TODO Auto-generated method stub
  }

  @Override
  public void onHeartBeatTimeout() {
    // TODO Auto-generated method stub
  }

  @Override
  public void onLogon() {
    // TODO Auto-generated method stub
  }

  @Override
  public void onLogout() {
    // TODO Auto-generated method stub
  }

  @Override
  public void onMissedHeartBeat() {
    // TODO Auto-generated method stub
  }

  @Override
  public void onRefresh() {
    // TODO Auto-generated method stub
  }

  @Override
  public void onReset() {
    // TODO Auto-generated method stub
  }
}
