package org.DistributedATS.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Timer;

public class SessionStateManager extends JavaScriptObject {

  public SessionStateManager(){};

  public void startCheckSessionStateTimer(final int stateMask) {
    Timer sessionCheckTimer = new Timer() {
      @Override
      public void run() {
        checkSessionState(stateMask);
      }
    };

    sessionCheckTimer.schedule(1000);
  };

  public void checkSessionState(int stateMask) {}
}
