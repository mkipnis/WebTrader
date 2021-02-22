package org.DistributedATS.server;

import quickfix.Message;

public class FIXMessageBlock {

  public FIXMessageBlock(FIXSessionID fixSessionID, Message fixMessage) {
    this.fixSessionID = fixSessionID;
    this.fixMessage = fixMessage;
  }

  public FIXSessionID fixSessionID;
  public Message fixMessage;
}
