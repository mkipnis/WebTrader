package org.DistributedATS.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.DistributedATS.shared.ExecutionReport;
import org.DistributedATS.shared.FIXUserSession;
import org.DistributedATS.shared.Order;
import org.DistributedATS.shared.OrderKey;
import quickfix.ConfigError;
import quickfix.DefaultSessionFactory;
import quickfix.Dictionary;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.MessageStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SessionStateListener;

public class FIXClientContextListener implements ServletContextListener {

  OrderMan orderMan = new OrderMan();
  PositionMan positionMan = new PositionMan();

  ExecutionReportProcessorThread executionReportProcessorThread =
      new ExecutionReportProcessorThread(orderMan, positionMan);
  MarketDataProcessorThread marketDataProcessor =
      new MarketDataProcessorThread();
  SessionManagerThread sessionManagerProcessor =
	      new SessionManagerThread();

  FIXApplication application = new FIXApplication(this);

  public ExecutionReportProcessorThread getExecReportProcessor() {
    return executionReportProcessorThread;
  }
  
  public SessionManagerThread getSessionManagerThread()
  {
	  return sessionManagerProcessor;
  }

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
    // TODO Auto-generated method stub
  }

  @Override
  public void contextInitialized(ServletContextEvent contextEvent) {
    // TODO Auto-generated method stub

    try {

      InputStream inputStream = null;

      Thread threadExecutionReport = new Thread(executionReportProcessorThread);
      Thread threadMarketData = new Thread(marketDataProcessor);
      Thread threadSessionManager = new Thread(sessionManagerProcessor);

      ServletContext context = contextEvent.getServletContext();
      String fixClientConfig = context.getInitParameter("fixclient.cfg");
      String fixClientConfigPath =
          context.getRealPath("") + File.separator + fixClientConfig;

      // InputStream input;
      inputStream = new FileInputStream(fixClientConfigPath);

      m_settings = new SessionSettings(inputStream);
      inputStream.close();
      
      String dataDictionary = context.getInitParameter("FIX44.xml");
      
      String dataDictionaryPath =
              context.getRealPath("") + File.separator + dataDictionary;
      
      m_settings.setString("DataDictionary", dataDictionaryPath);
      m_settings.setString("FileStorePath", "/tmp");

      MessageStoreFactory messageStoreFactory =
          new FileStoreFactory(m_settings);
      LogFactory logFactory = new ScreenLogFactory(true, true, true, true);

      DefaultSessionFactory defaultSessionFactory = new DefaultSessionFactory(
          application, messageStoreFactory, logFactory);

      m_initiator = new FIXSocketInitiator(m_settings, defaultSessionFactory);
      m_initiator.start();

      ServletContext sc = contextEvent.getServletContext();
      sc.setAttribute("FIXContextListner", this);

      SessionID defaultSessionId = new SessionID("DEFAULT", "", "");
      m_defaultDictionary = m_settings.get(defaultSessionId);

      threadExecutionReport.start();
      threadMarketData.start();
      threadSessionManager.start();

      // System.out.println("Settings : " + m_settings );
      System.out.println("FIX Gateway : " + m_settings.getString("TargetCompID"));

      m_TargetCompID = m_settings.getString("TargetCompID");
      m_BeginString = m_settings.getString("BeginString");

    } catch (IOException exception) {
      System.out.println("IO Exception : " + exception.getMessage());
    } catch (ConfigError configError) {
      System.out.println("ConfigError Exception : " + configError.getMessage());
    }
  }

  SessionSettings m_settings = null;
  Dictionary m_defaultDictionary = null;
  FIXSocketInitiator m_initiator = null;

  public FIXUserSession sendLogon(String username, String password,
                                  SessionStateListener stateListener) {
    FIXUserSession fixUserSession = new FIXUserSession();

    fixUserSession.sessionState = FIXUserSession.STATE_PENDING_LOGON;
    fixUserSession.token = UUID.randomUUID().toString();
    fixUserSession.username = username;

    FIXSessionID sessionID =
        new FIXSessionID(m_BeginString, username, password, m_TargetCompID,
                         fixUserSession.token);

    try {

      m_settings.set(sessionID, m_defaultDictionary);
      m_initiator.createDynamicSession(sessionID);

      Session session = Session.lookupSession(sessionID);

      session.addStateListener(stateListener);

    } catch (ConfigError e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      fixUserSession.sessionState =
          FIXUserSession.STATE_UNABLE_TO_CREATE_SESSION;
    }

    setSessionState(fixUserSession.token, fixUserSession);

    return fixUserSession;
  }

  public SessionID getSessionID(String senderCompID, String token) {
    return new SessionID(m_BeginString, senderCompID, m_TargetCompID, token);
  }

  public void populateLastestOrderState(SessionID sessionID,
                                        long maxOrderSequenceNumber,
                                        HashMap<OrderKey, Order> ordersOut) {
    ArrayList<Order> orders = orderMan.getSortedBySequenceOrderList(sessionID);

    if (orders == null)
      return;

    Collections.sort(orders); // Inefficient - move to order update routines

    int max_count = 0;

    if (orders != null) {
      for (Order order : orders) {
        if (order.status != ExecutionReport.PENDING_NEW &&
            order.sequenceNumber > maxOrderSequenceNumber) {
          System.out.println("Order Sequence Number : " + order.sequenceNumber +
                             " : " + maxOrderSequenceNumber);

          ordersOut.put(order.orderKey, order);

          if (max_count++ > 10)
            return;
        }
      }
    }
  }

  public FIXUserSession getSessionState(String token) {
    FIXUserSession fixUserSession = userSessionMap.get(token);

    return fixUserSession;
  }

  public void setLogoutState(SessionID sessionID, String text) {
    FIXUserSession fixUserSession =
        userSessionMap.get(sessionID.getSessionQualifier());

    fixUserSession.sessionState = FIXUserSession.STATE_LOGGED_OUT;
    
    if ( text!=null )
    	fixUserSession.sessionStateText = text;
  }

  public void setSessionState(String token, FIXUserSession fixUserSession) {
    this.userSessionMap.put(token, fixUserSession);
  }

  public void setSecurities(List<String> securitiesList) {
    securtiesList.addAll(securitiesList);
  }

  private ConcurrentHashMap<String, FIXUserSession> userSessionMap =
      new ConcurrentHashMap<String, FIXUserSession>();

  private CopyOnWriteArrayList<String> securtiesList =
      new CopyOnWriteArrayList<String>();

  private String m_TargetCompID = "";
  private String m_BeginString = "";
}
