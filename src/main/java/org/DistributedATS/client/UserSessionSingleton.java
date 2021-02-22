package org.DistributedATS.client;

import org.DistributedATS.shared.FIXUserSession;

public class UserSessionSingleton {

  private static UserSessionSingleton instance = null;

  public static UserSessionSingleton getInstance() {
    if (instance == null) {
      instance = new UserSessionSingleton();
    }
    return instance;
  }

  public void setFIXUserSession(FIXUserSession fixUserSession) {
    this.fixUserSession = fixUserSession;
  }

  public FIXUserSession getFIXUserSession() { return fixUserSession; }

  private FIXUserSession fixUserSession = null;
}
