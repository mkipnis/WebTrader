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

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.PortalLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import java.util.ArrayList;
import java.util.HashMap;
import org.DistributedATS.shared.FIXUserSession;
import org.DistributedATS.shared.Instrument;
import org.DistributedATS.shared.MarketDataSnapshot;
import org.DistributedATS.shared.SessionStateRequest;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class WebTrader implements EntryPoint {
  /**
   * The message displayed to the user when the server cannot be reached or
   * returns an error.
   */
  private static final String SERVER_ERROR =
      "An error   ccurred while "
      + "attempting to contact the server. Please check your network "
      + "connection and try again.";

  private final FIXServiceInterfaceAsync fixService =
      GWT.create(FIXServiceInterface.class);

  public final LogonCanvas logonCanvas = new LogonCanvas(this);
  public final TicketCanvas ticketCanvas = new TicketCanvas(this);
  public final MarketDataCanvas marketDataCanvas = new MarketDataCanvas();
  public final PriceDepthCanvas priceDepthCanvas = new PriceDepthCanvas();
  public final OrderHistoryCanvas orderHistoryCanvas = new OrderHistoryCanvas();
  private PriceDepthPortalLayoutCanvas priceDepthPoralCanvas;

  private PriceDepthPortalLayout priceDepthPortalLayout = null;
  
  private static WebTrader instance = null;

  public static WebTrader getInstance() { return instance; }

  Instrument activeInstrument = null;

  @Override
  public void onModuleLoad() {
	  
    // TODO Auto-generated method stub
    Canvas.resizeControls(3);
    Canvas.resizeFonts(3);
    
    this.instance = this;

    RootPanel.get("logonCanvas").add(logonCanvas);

    VLayout mainLayout = new VLayout();
    mainLayout.setWidth100();
    mainLayout.setHeight100();

    HLayout marketDataAndTradeEntryLayout = new HLayout();
    VLayout vLayout = new VLayout();
    vLayout.setWidth("70%");

    marketDataCanvas.setHeight("50%");
    vLayout.addMember(marketDataCanvas);
    ticketCanvas.setHeight("15%");
    vLayout.addMember(ticketCanvas);

    vLayout.setShowResizeBar(true);
    
    priceDepthPortalLayout = new PriceDepthPortalLayout(this);
    
    priceDepthPoralCanvas = new PriceDepthPortalLayoutCanvas(priceDepthPortalLayout);
    
    priceDepthPoralCanvas.setPrompt("Drag Instruments from Market Data Grid to see Price Depth");
  
    marketDataAndTradeEntryLayout.addMember(vLayout);
    marketDataAndTradeEntryLayout.addMember(priceDepthPoralCanvas);
    marketDataAndTradeEntryLayout.setShowResizeBar(true);

    mainLayout.addMember(marketDataAndTradeEntryLayout);
    orderHistoryCanvas.setWidth100();
    orderHistoryCanvas.setShowResizeBar(true);
    orderHistoryCanvas.setHeight("30%");
    mainLayout.addMember(orderHistoryCanvas);
    mainLayout.setShowResizeBar(true);
    mainLayout.setHeight("90%");

    RootPanel.get("traderPortlet").add(mainLayout);

    disableTrading();
  }

  public void enableTrading() {
    logonCanvas.disableLogon();
    marketDataCanvas.setDisabled(false);
    priceDepthCanvas.setDisabled(false);
    orderHistoryCanvas.setDisabled(false);
    priceDepthPoralCanvas.setDisabled(false);
    priceDepthPortalLayout.setDisabled(false);
  }

  public void disableTrading() {
    marketDataCanvas.setDisabled(true);
    priceDepthCanvas.setDisabled(true);
    ticketCanvas.setDisabled(true);
    orderHistoryCanvas.setDisabled(true);
    priceDepthPoralCanvas.setDisabled(true);
    priceDepthPortalLayout.setDisabled(true);
  }

  public Instrument getActiveInstrument() { return activeInstrument; }
  
  public Instrument getInstrumentWithRefData( Instrument instrument )
  {
	    Integer instrumentWithRefDataIndex = WebTrader.getInstance().getCompleteSecurityList().indexOf(instrument);
	    
	    Instrument instrumentWithRefData = WebTrader.getInstance().getCompleteSecurityList().get(instrumentWithRefDataIndex);
	    
	    return instrumentWithRefData;
  }

  public void setActiveInstrument(Instrument instrument) {
	    
	    Instrument instrumentWithRefData = getInstrumentWithRefData( instrument );
	    
    this.activeInstrument = instrumentWithRefData;
  }

  public FIXServiceInterfaceAsync getFIXService() { return fixService; }

  public TicketCanvas getTicketCanvas() { return ticketCanvas; }

  public void initiateLogon() {
    stateMask = 0;
    stateMask |= 1 << FIXUserSession.LOGON_STATE_BIT;
    logonCheckTimer.schedule(1000);
  }

  public void checkSessionState() {
    final FIXUserSession fixUserSession =
        UserSessionSingleton.getInstance().getFIXUserSession();

    SessionStateRequest sessionStateRequest = new SessionStateRequest();

    sessionStateRequest.token = fixUserSession.token;
    sessionStateRequest.stateMask = stateMask;
    sessionStateRequest.activeSecurityList = activeSecurityList;
    sessionStateRequest.maxOrderSequenceNumber = orderSequenceNumber;
    sessionStateRequest.marketDataSequenceNumber = marketDataSequenceNumber;

    fixService.getLastestSessionState(sessionStateRequest, new AsyncCallback<
                                                               FIXUserSession>() {
      @Override
      public void onFailure(Throwable caught) {
        // TODO Auto-generated method stub
        SC.say("Unble to check session state try later : " +
               caught.getLocalizedMessage());
      }

      @Override
      public void onSuccess(FIXUserSession fixSessionData ) {

        // no session state - intializing
        if (fixUserSession.sessionState !=
            FIXUserSession.STATE_SUCCESSFUL_LOGIN) {
          // TODO Auto-generated method stub
          if (fixSessionData.sessionState == FIXUserSession.STATE_SUCCESSFUL_LOGIN) {
            logonCanvas.statusItem.setValue(
                "<b><font color='green'>Loading instrument ... Please wait</font></b>");

            stateMask = 0;
            stateMask |= 1 << FIXUserSession.SECURITY_LIST_BIT;

            UserSessionSingleton.getInstance().setFIXUserSession(fixSessionData);

            logonCheckTimer.schedule(1000);

          } else if (fixSessionData.sessionState ==
                     FIXUserSession.STATE_PENDING_LOGON) {
            stateMask = 0;
            stateMask |= 1 << FIXUserSession.LOGON_STATE_BIT;

            logonCanvas.statusItem.setValue(
                "<b><font color='blue'>Pending Logon ... Please wait</font></b>");

            logonCheckTimer.schedule(1000);

          } else if (fixSessionData.sessionState == FIXUserSession.STATE_LOGGED_OUT) {
            logonCanvas.statusItem.setValue(fixSessionData.sessionStateText);
          } else {
            logonCanvas.statusItem.setValue(
                "<b><font color='#b30000'>Unable to login at this time, please try again later</font></b>");
          }

        } else {

          if (fixSessionData.sessionState != fixUserSession.sessionState) {
            if (fixSessionData.sessionState != FIXUserSession.STATE_SUCCESSFUL_LOGIN) {
              logonCanvas.statusItem.setValue(
                  "<b><font color='#b30000'>Session disconnected, please refresh browser and re-login</font></b>");

              disableTrading();
            }
          }

          if ((fixSessionData.stateMask & (1 << FIXUserSession.SECURITY_LIST_BIT)) !=
              0) {
            completeSecurityList = fixSessionData.activeSecurityList;

            logonCanvas.statusItem.setValue(
                "<b><font color='green'>Ready to trade</font></b>");

            stateMask = 0;

            enableTrading();

            logonCheckTimer.schedule(1000);
          }

          if ((fixSessionData.stateMask & (1 << FIXUserSession.MARKET_DATA_BIT)) != 0) {

            if (fixSessionData.instrumentMarketDataSnapshot != null)
              marketDataCanvas.updateTopLevelMarketData(
            		  fixSessionData.instrumentMarketDataSnapshot);

            marketDataSequenceNumber = fixSessionData.marketDataSequenceNumber;

            marketDataCanvas.updatePositions(fixSessionData.positionsMap);
            
            priceDepthPortalLayout.updatePriceDepth( fixSessionData );

            /*for (Instrument instrument : priceDepthCanvasMap.keySet()) {
              PriceDepthCanvas priceDepthCanvas =
                  priceDepthCanvasMap.get(instrument).getPriceDepthCanvas();

              MarketDataSnapshot marketDataSnapshot =
                  result.instrumentMarketDataSnapshot.get(instrument);

              if (marketDataSnapshot != null) {
                priceDepthCanvas.updatePriceDepth(marketDataSnapshot);
              }
            }*/

            //activeSecurityList.clear();
          }

          if ((fixSessionData.stateMask & (1 << FIXUserSession.ORDERS_DATA_BIT)) != 0) {
            long lastSequenceNumber =
                orderHistoryCanvas.insertUpdateOrders(fixSessionData.orders);

            if (lastSequenceNumber > orderSequenceNumber)
              orderSequenceNumber = lastSequenceNumber;
          }

          stateMask = 0;

          // Getting securities list
          if (completeSecurityList.size() == 0) {
            stateMask |= 1 << FIXUserSession.SECURITY_LIST_BIT;
          }

          // Getting market data for select in the main grid securities
          if (activeSecurityList.size() > 0) {
            stateMask |= 1 << FIXUserSession.MARKET_DATA_BIT;
          }

          stateMask |= 1 << FIXUserSession.ORDERS_DATA_BIT;
          logonCheckTimer.schedule(1000);
        }
      }
    });
  }

  public boolean insertActiveInstrument(Instrument instrument) {
    if (!this.activeSecurityList.contains(instrument)) {
      this.activeSecurityList.add(instrument);
      marketDataSequenceNumber = 0;
      return true;
    }

    return true;
  }

  public ArrayList<Instrument> getActiveSecurityList() {
    return this.activeSecurityList;
  }

  public ArrayList<Instrument> getCompleteSecurityList() {
    return this.completeSecurityList;
  }

  Timer logonCheckTimer = new Timer() {
    @Override
    public void run() {
      checkSessionState();
    }
  };

  private ArrayList<Instrument> activeSecurityList =
      new ArrayList<Instrument>(); // List of securities visible on the main
                                   // grid
  private ArrayList<Instrument> completeSecurityList =
      new ArrayList<Instrument>();

  private int stateMask;
  private long orderSequenceNumber =
      -1; // order update/execution report sequence number
  private long marketDataSequenceNumber = -1;
}
