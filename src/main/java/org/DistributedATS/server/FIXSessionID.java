package org.DistributedATS.server;

import quickfix.SessionID;

public class FIXSessionID extends SessionID {

  public FIXSessionID(String beginString, String username, String password,
                      String targetID, String token) {
    super(beginString, username, targetID, token);
    this.password = password;
    this.canLogon = true;
  }

  public String getPassword() { return this.password; }

  public boolean getCanLogon() { return canLogon; }

  public void setCanLogon(boolean canLogon) { this.canLogon = canLogon; }

  public void incrementMarketDataSequence() {
    this.marketDataSequenceNumber++;
  };

  public long getMarketDataSequenceNumber() {
    return this.marketDataSequenceNumber;
  };

  private boolean canLogon;

  private String password;

  private long marketDataSequenceNumber = 0;
}
