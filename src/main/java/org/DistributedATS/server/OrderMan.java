package org.DistributedATS.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.DistributedATS.shared.Order;
import org.DistributedATS.shared.OrderKey;
import quickfix.SessionID;

public class OrderMan {

  synchronized public void insertUpdateOrder(SessionID sessionID,
                                             OrderKey orderKey, Order order) {
    HashMap<OrderKey, Order> userOrders = orderMap.get(sessionID);

    if (userOrders == null) {
      userOrders = new HashMap<OrderKey, Order>();
      orderMap.put(sessionID, userOrders);
    }

    userOrders.put(orderKey, order);

    ArrayList<Order> usersOrderList = new ArrayList<Order>(userOrders.values());

    Collections.sort(usersOrderList);

    sortedBySequenceNumberOrders.put(sessionID, usersOrderList);
  }

  synchronized public Order getOrder(SessionID sessionID, OrderKey orderId) {
    HashMap<OrderKey, Order> userOrders = orderMap.get(sessionID);

    if (userOrders == null)
      return null;

    Order order = userOrders.get(orderId);

    return order;
  }

  synchronized public HashMap<OrderKey, Order>
  getAllOrders(SessionID sessionID) {
    return orderMap.get(sessionID);
  }

  synchronized public ArrayList<Order>
  getSortedBySequenceOrderList(SessionID sessionID) {
    return sortedBySequenceNumberOrders.get(sessionID);
  }

  private HashMap<SessionID, HashMap<OrderKey, Order>> orderMap =
      new HashMap<SessionID, HashMap<OrderKey, Order>>();

  // TODO: In order to avoid extra sorts, check if LinkedHashMap can be used 
  private HashMap<SessionID, ArrayList<Order>> sortedBySequenceNumberOrders =
      new HashMap<SessionID, ArrayList<Order>>();
}
