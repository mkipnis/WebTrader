package org.DistributedATS.server;



import java.util.List;

import java.util.concurrent.CopyOnWriteArrayList;

import org.DistributedATS.shared.Instrument;

import quickfix.ApplicationExtended;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.Message.Header;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;

import quickfix.field.MsgType;
import quickfix.field.SecurityExchange;
import quickfix.field.Symbol;

public class FIXApplication implements ApplicationExtended {

  FIXClientContextListener contextListener;

  public FIXApplication(FIXClientContextListener contextListener) {
    this.contextListener = contextListener;
  }

  @Override
  public void fromAdmin(Message message, SessionID sessionID)
      throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
             RejectLogon {
    // TODO Auto-generated method stub
    Header header = message.getHeader();
    quickfix.field.MsgType msgType = new MsgType();
    header.getField(msgType);

    Session session = Session.lookupSession(sessionID);

    if (session != null) {
      if (msgType.getValue().equals(MsgType.LOGOUT)) {
        quickfix.field.Text text = new quickfix.field.Text();

        message.getField(text);

        System.out.println(
            "Logout Message : " + message +
            " - Session Token : " + sessionID.getSessionQualifier());

        this.contextListener.setLogoutState(sessionID, text.getValue());

        this.contextListener.m_initiator.removeDynamicSession(sessionID);
      }
    }
  }

  @Override
  public void fromApp(Message message, SessionID sessionID)
      throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
             UnsupportedMessageType {
    // TODO Auto-generated method stub

    try {
      Header header = message.getHeader();
      quickfix.field.MsgType msgType = new MsgType();
      header.getField(msgType);

      if (msgType.getValue().equals(MsgType.SECURITY_LIST)) {
        quickfix.field.NoRelatedSym numberOfInstruments =
            new quickfix.field.NoRelatedSym();
        message.getField(numberOfInstruments);
        securtiesList.clear();

        for (int symbol_index = 0;
             symbol_index < numberOfInstruments.getValue(); ++symbol_index) {
          quickfix.FieldMap group = message.getGroup(
              symbol_index + 1, numberOfInstruments.getField());

          quickfix.field.Symbol symbol = new Symbol();
          group.getField(symbol);

          quickfix.field.SecurityExchange securityExchange =
              new SecurityExchange();
          group.getField(securityExchange);
          securtiesList.add(
              new Instrument(securityExchange.getValue(), symbol.getValue()));
        }
      } else if (msgType.getValue().equals(
                     MsgType.MARKET_DATA_SNAPSHOT_FULL_REFRESH) ||
                 msgType.getValue().equals(
                     MsgType.MARKET_DATA_INCREMENTAL_REFRESH)) {
        FIXMessageBlock block =
            new FIXMessageBlock((FIXSessionID)sessionID, message);

        contextListener.marketDataProcessor.enqueueMarketDataMessage(block);
      } else if (msgType.getValue().equals(MsgType.EXECUTION_REPORT)) {
        FIXMessageBlock fixedMessageBlock =
            new FIXMessageBlock((FIXSessionID)sessionID, message);

        contextListener.executionReportProcessorThread.enqueueExecutionReport(
            fixedMessageBlock);
      }

    } catch (FieldNotFound e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } /*catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
    }*/
  }

  @Override
  public void onCreate(SessionID arg0) {
    // TODO Auto-generated method stub
  }

  @Override
  public void onLogon(SessionID sessionID) {
    // TODO Auto-generated method stub
    System.out.println("Successfull Logon : " + sessionID.getSenderCompID() +
                       ":" + sessionID.toString());
    Session session = Session.lookupSession(sessionID);

    try {
		Thread.sleep(1000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    if (session != null) {
      String requestID = FIXServiceInterfaceImpl.getNextRequestID(sessionID);

      quickfix.fix44.SecurityListRequest securitiesListRequest =
          new quickfix.fix44.SecurityListRequest(
              new quickfix.field.SecurityReqID(requestID),
              new quickfix.field.SecurityListRequestType(
                  quickfix.field.SecurityListRequestType.ALL_SECURITIES));

      session.send(securitiesListRequest);
    }
  }

  @Override
  public void onLogout(SessionID sessionID) {
    // TODO Auto-generated method stub

    FIXSessionID fixSessionID = (FIXSessionID)sessionID;
    
    this.contextListener.setLogoutState(sessionID, null);
    
    // In order to prevent auto reconnection in case of re-logins
    fixSessionID.setCanLogon(false);
  }

  @Override
  public void toAdmin(Message message, SessionID sessionID) {
    // TODO Auto-generated method stub
    try {
      Header header = message.getHeader();

      quickfix.field.MsgType msgType = new MsgType();

      header.getField(msgType);

      if (msgType.getValue() == MsgType.LOGON) {

        FIXSessionID fixSessionID = (FIXSessionID)sessionID;

        quickfix.field.Username userName =
            new quickfix.field.Username(sessionID.getSenderCompID());
        message.setField(userName);

        quickfix.field.Password password =
            new quickfix.field.Password(fixSessionID.getPassword());
        message.setField(password);

        System.out.println(message);
      }

    } catch (FieldNotFound e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void toApp(Message message, SessionID sessionID) throws DoNotSend {
    // TODO Auto-generated method stub
  }

  @Override
  public boolean canLogon(SessionID arg0) {
    // TODO Auto-generated method stub

    FIXSessionID fixSessionID = (FIXSessionID)arg0;
    return fixSessionID.getCanLogon();
  }

  @Override
  public void onBeforeSessionReset(SessionID arg0) {
    // TODO Auto-generated method stub
  }

  public List<Instrument> getSecurities() { return securtiesList; }

  private CopyOnWriteArrayList<Instrument> securtiesList =
      new CopyOnWriteArrayList<Instrument>();
}
