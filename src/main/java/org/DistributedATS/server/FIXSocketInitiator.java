package org.DistributedATS.server;

import quickfix.ConfigError;
import quickfix.RuntimeError;
import quickfix.SessionFactory;
import quickfix.SessionSettings;
import quickfix.mina.EventHandlingStrategy;
import quickfix.mina.SingleThreadedEventHandlingStrategy;
import quickfix.mina.initiator.AbstractSocketInitiator;

public class FIXSocketInitiator extends AbstractSocketInitiator {

  public FIXSocketInitiator(SessionSettings settings,
                            SessionFactory sessionFactory) throws ConfigError {
    super(settings, sessionFactory);

    eventHandlingStrategy =
        new SingleThreadedEventHandlingStrategy(this, DEFAULT_QUEUE_CAPACITY);
  }

  @Override
  public void start() throws ConfigError, RuntimeError {
    // TODO Auto-generated method stub
    initialize();
  }

  private void initialize() throws ConfigError {
    if (isStarted.equals(Boolean.FALSE)) {
      eventHandlingStrategy.setExecutor(longLivedExecutor);
    }

    startInitiators();
    eventHandlingStrategy.blockInThread();
    isStarted = Boolean.TRUE;
  }

  @Override
  public void stop(boolean arg0) {
    // TODO Auto-generated method stub
  }

  @Override
  protected EventHandlingStrategy getEventHandlingStrategy() {
    // TODO Auto-generated method stub
    return eventHandlingStrategy;
  }

  private volatile Boolean isStarted = Boolean.FALSE;
  private final SingleThreadedEventHandlingStrategy eventHandlingStrategy;
}
