package org.DistributedATS.server;

import java.util.HashMap;
import org.DistributedATS.shared.Instrument;
import org.DistributedATS.shared.Order;
import org.DistributedATS.shared.Position;
import quickfix.SessionID;
import quickfix.field.Side;

//
// Builds positions with VWAPS from Exection Report
//
public class PositionMan {

  public void updatePosition(SessionID sessionId, Order order, Double size,
                             Double price) {
    HashMap<Instrument, Position> customerPosition = positions.get(sessionId);

    if (customerPosition == null) {
      customerPosition = new HashMap<Instrument, Position>();
      positions.put(sessionId, customerPosition);
    }

    Position instrumentPosition = customerPosition.get(order.instrument);

    if (instrumentPosition == null) {
      instrumentPosition = new Position();
      customerPosition.put(order.instrument, instrumentPosition);
    }

    if (order.side == Side.BUY) {
      if (instrumentPosition.buy_amt + size != 0.0) {
        instrumentPosition.buy_avg_price =
            ((instrumentPosition.buy_avg_price * instrumentPosition.buy_amt) +
             (size * price)) /
            (instrumentPosition.buy_amt + size);

        instrumentPosition.buy_amt += size;
      } else {
        instrumentPosition.buy_avg_price = 0.0;
        instrumentPosition.buy_amt = 0.0;
      }

    } else {

      if (instrumentPosition.sell_amt + size != 0.0) {
        instrumentPosition.sell_avg_price =
            ((instrumentPosition.sell_avg_price * instrumentPosition.sell_amt) +
             (size * price)) /
            (instrumentPosition.sell_amt + size);

        instrumentPosition.sell_amt += size;
      } else {
        instrumentPosition.sell_avg_price = 0.0;
        instrumentPosition.sell_amt = 0.0;
      }
    }
  }

  public HashMap<SessionID, HashMap<Instrument, Position>> positions =
      new HashMap<SessionID, HashMap<Instrument, Position>>();
}
