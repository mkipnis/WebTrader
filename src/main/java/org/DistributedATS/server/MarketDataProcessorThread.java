package org.DistributedATS.server;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import org.DistributedATS.shared.FIXUserSession;
import org.DistributedATS.shared.Instrument;
import org.DistributedATS.shared.MarketDataSnapshot;
import org.DistributedATS.shared.PriceLevel;
import org.DistributedATS.shared.SessionStateRequest;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.Message.Header;
import quickfix.SessionID;
import quickfix.field.MDEntryType;
import quickfix.field.MsgType;

public class MarketDataProcessorThread implements Runnable {

  BlockingQueue<FIXMessageBlock> marketDataQueue =
      new LinkedBlockingDeque<FIXMessageBlock>();

  public void enqueueMarketDataMessage(FIXMessageBlock marketData) {
    try {
      marketDataQueue.put(marketData);
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
        FIXMessageBlock marketData = marketDataQueue.take();

        processMarketData(marketData);
      }

    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void processMarketData(FIXMessageBlock messageBlock) {
    Message message = messageBlock.fixMessage;
    FIXSessionID fixSessionID = messageBlock.fixSessionID;

    try {
      Header header = message.getHeader();
      quickfix.field.MsgType msgType = new MsgType();
      header.getField(msgType);

      fixSessionID.incrementMarketDataSequence();

      if (msgType.getValue().equals(
              MsgType.MARKET_DATA_SNAPSHOT_FULL_REFRESH)) {
        quickfix.field.Symbol symbol = new quickfix.field.Symbol();
        message.getField(symbol);

        quickfix.field.SecurityExchange securityExchange =
            new quickfix.field.SecurityExchange();
        message.getField(securityExchange);

        quickfix.field.NoMDEntries numberOfMDEntries =
            new quickfix.field.NoMDEntries();
        message.getField(numberOfMDEntries);

        Instrument instrument =
            new Instrument(securityExchange.getValue(), symbol.getValue());

        MarketDataSnapshot marketDataSnapshot = new MarketDataSnapshot();
        Integer bidLevel = 0;
        Integer askLevel = 0;

        for (int md_entry = 0; md_entry < numberOfMDEntries.getValue();
             md_entry++) {
          quickfix.FieldMap group =
              message.getGroup(md_entry + 1, numberOfMDEntries.getField());

          quickfix.field.MDEntryType entryType =
              new quickfix.field.MDEntryType();
          quickfix.field.MDEntryPx entryPrice = new quickfix.field.MDEntryPx();
          quickfix.field.MDEntrySize entrySize =
              new quickfix.field.MDEntrySize();

          group.getField(entryType);
          group.getField(entryPrice);
          group.getField(entrySize);

          PriceLevel priceLevel =
              new PriceLevel(entryType.getValue(), entryPrice.getValue(),
                             entrySize.getValue());

          switch (entryType.getValue()) {
          case MDEntryType.BID:
            marketDataSnapshot.insertBid(bidLevel++, priceLevel);
            break;
          case MDEntryType.OFFER:
            marketDataSnapshot.insertAsk(askLevel++, priceLevel);
            break;
          case MDEntryType.TRADE_VOLUME:
            marketDataSnapshot.setVolume(entrySize.getValue());
            break;
          case MDEntryType.TRADE:
            marketDataSnapshot.setLastTradedPrice(entryPrice.getValue());
            break;
          case MDEntryType.OPENING_PRICE:
            marketDataSnapshot.setOpenPrice(entryPrice.getValue());
            break;
          default:
          };
        }

        insertMarketSnapshot(fixSessionID, instrument, marketDataSnapshot);

      } else if (msgType.getValue().equals(
                     MsgType.MARKET_DATA_INCREMENTAL_REFRESH)) {
        quickfix.field.NoMDEntries numberOfMDEntries =
            new quickfix.field.NoMDEntries();
        message.getField(numberOfMDEntries);

        HashMap<Instrument, MarketDataSnapshot> latestMarketDataSnapshots =
            new HashMap<Instrument, MarketDataSnapshot>();

        for (int md_entry = 0; md_entry < numberOfMDEntries.getValue();
             md_entry++) {
          quickfix.FieldMap group =
              message.getGroup(md_entry + 1, numberOfMDEntries.getField());

          quickfix.field.Symbol symbol = new quickfix.field.Symbol();
          group.getField(symbol);

          quickfix.field.SecurityExchange securityExchange =
              new quickfix.field.SecurityExchange();
          group.getField(securityExchange);

          quickfix.field.MDEntryType entryType =
              new quickfix.field.MDEntryType();
          quickfix.field.MDEntryPx entryPrice = new quickfix.field.MDEntryPx();
          quickfix.field.MDEntrySize entrySize =
              new quickfix.field.MDEntrySize();

          group.getField(entryType);
          group.getField(entryPrice);
          group.getField(entrySize);

          Instrument instrument =
              new Instrument(securityExchange.getValue(), symbol.getValue());

          MarketDataSnapshot marketDataSnapshot =
              latestMarketDataSnapshots.get(instrument);

          if (marketDataSnapshot == null) {
            marketDataSnapshot = new MarketDataSnapshot();
            latestMarketDataSnapshots.put(instrument, marketDataSnapshot);
          }

          PriceLevel priceLevel =
              new PriceLevel(entryType.getValue(), entryPrice.getValue(),
                             entrySize.getValue());

          switch (entryType.getValue()) {
          case MDEntryType.BID:
            Integer currentBidLevel = marketDataSnapshot.getBidSide().size();
            marketDataSnapshot.insertBid(currentBidLevel++, priceLevel);
            break;
          case MDEntryType.OFFER:
            Integer currentAskLevel = marketDataSnapshot.getAskSide().size();
            marketDataSnapshot.insertAsk(currentAskLevel++, priceLevel);
            break;
          case MDEntryType.TRADE_VOLUME:
            marketDataSnapshot.setVolume(entrySize.getValue());
            break;
          case MDEntryType.TRADE:
            marketDataSnapshot.setLastTradedPrice(entryPrice.getValue());
            break;
          case MDEntryType.OPENING_PRICE:
            marketDataSnapshot.setOpenPrice(entryPrice.getValue());
            break;
          default:
          };
        }

        for (Instrument instrument : latestMarketDataSnapshots.keySet()) {
          MarketDataSnapshot marketDataSnapshot =
              latestMarketDataSnapshots.get(instrument);

          insertMarketSnapshot(fixSessionID, instrument, marketDataSnapshot);
        }
      }

    } catch (FieldNotFound e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } /*catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
    }*/
  }

  private void insertMarketSnapshot(FIXSessionID sessionId,
                                    Instrument instrument,
                                    MarketDataSnapshot marketDataSnapshot) {
    ConcurrentHashMap<Instrument, MarketDataSnapshot>
        sessionMarketDataSnapshots = userMarketData.get(sessionId);

    if (sessionMarketDataSnapshots == null) {
      sessionMarketDataSnapshots =
          new ConcurrentHashMap<Instrument, MarketDataSnapshot>();
      userMarketData.put(sessionId, sessionMarketDataSnapshots);
    }

    marketDataSnapshot.setMarketDataSequenceNumber(
        sessionId.getMarketDataSequenceNumber());

    sessionMarketDataSnapshots.put(instrument, marketDataSnapshot);
  }

  public void
  populateMarketDataSnapshots(SessionID sessionID,
                              SessionStateRequest sessionStateRequest,
                              FIXUserSession sessionState) {
    ConcurrentHashMap<Instrument, MarketDataSnapshot> sessionMarketData =
        userMarketData.get(sessionID);

    if (sessionMarketData == null)
      return;

    if (sessionStateRequest.activeSecurityList == null)
      return;

    // Iterate through passed in by Session active instrument and populate the most recent market data
    for (Instrument instrument : sessionStateRequest.activeSecurityList) {
    	
      MarketDataSnapshot marketDataSnapshot = sessionMarketData.get(instrument);

      // only include market data that was changed since the latest poll
      if (sessionStateRequest.marketDataSequenceNumber <
          marketDataSnapshot.getMarketDataSequenceNumber()) {

        sessionState.instrumentMarketDataSnapshot.put(instrument,
                                                      marketDataSnapshot);

        if (marketDataSnapshot.getMarketDataSequenceNumber() >
            sessionState.marketDataSequenceNumber)
          sessionState.marketDataSequenceNumber =
              marketDataSnapshot.getMarketDataSequenceNumber();
      }
    };
  }

  private ConcurrentHashMap<SessionID,
                            ConcurrentHashMap<Instrument, MarketDataSnapshot>>
      userMarketData = new ConcurrentHashMap<
          SessionID, ConcurrentHashMap<Instrument, MarketDataSnapshot>>();
}
