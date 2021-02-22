package org.DistributedATS.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import org.DistributedATS.shared.FIXUserSession;
import org.DistributedATS.shared.Instrument;
import org.DistributedATS.shared.Order;
import org.DistributedATS.shared.PriceLevel;
import org.DistributedATS.shared.SessionStateRequest;

@RemoteServiceRelativePath("fixservice")
public interface FIXServiceInterface extends RemoteService {

  FIXUserSession logon(String username, String password);
  FIXUserSession logout(String username);
  FIXUserSession
  getLastestSessionState(SessionStateRequest sessionStateRequest);
  Order submitOrder(String username, String token, String buy_or_sell,
                    Instrument instrument, Double price, Double size);
  String cancelOrder(String username, String token, Order orderIn);
  String cancelAllOrders(String username, String token);
}
