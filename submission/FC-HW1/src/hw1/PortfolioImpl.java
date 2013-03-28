/* Author: Junyang Xin (jx372@nyu.edu)
 * Date: 3/13/2013
 */

package hw1;

import java.util.HashMap;

public class PortfolioImpl implements Portfolio {
  private HashMap<String, PositionImpl> map =
      new HashMap<String, PositionImpl>();
  private PositionIterImpl posIter =
      new PositionIterImpl(map.values().iterator());

  /**
   * Method to perform a trade.
   *
   * @param symbol, the name of a position
   * @param quantity, the quantity of this trade. Positive means buying,
   *        and negative means selling
   */
  public void newTrade(String symbol, int quantity) {
    if (quantity != 0) { 
      if (map.containsKey(symbol)) {
        map.get(symbol).setQuantity(
            map.get(symbol).getQuantity() + quantity);
        if (map.get(symbol).getQuantity() == 0) {
          map.remove(symbol);
        }
      } else {
        map.put(symbol, new PositionImpl(symbol, quantity));
      }
    }
  }

  /**
   * Method to get the iterator of the positions.
   *
   * @return the iterator of the positions in this portfolio
   */
  public PositionIter getPositionIter() {
    posIter.resetIter(map.values().iterator());
    return posIter;
  }

  /* This package private singleton method only exists to make junit test
     easier. */
  HashMap<String, PositionImpl> getMap() {
    return map;
  }
}

