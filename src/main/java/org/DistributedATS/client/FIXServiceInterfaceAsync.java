package org.DistributedATS.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.DistributedATS.shared.FIXUserSession;
import org.DistributedATS.shared.Instrument;
import org.DistributedATS.shared.Order;
import org.DistributedATS.shared.SessionStateRequest;

public interface FIXServiceInterfaceAsync {

  void logon(String username, String password,
             AsyncCallback<FIXUserSession> callback);

  void logout(String username, AsyncCallback<FIXUserSession> callback);

  void submitOrder(String username, String token, String buy_or_sell,
                   Instrument instrument, Double price, Double size,
                   AsyncCallback<Order> callback);

  void getLastestSessionState(SessionStateRequest sessionStateRequest,
                              AsyncCallback<FIXUserSession> callback);

  void cancelOrder(String username, String token, Order orderIn,
                   AsyncCallback<String> callback);

  void cancelAllOrders(String username, String token,
                       AsyncCallback<String> callback);
}
