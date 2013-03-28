/* Author: Junyang Xin (jx372@nyu.edu)
 * Date: 3/13/2013
 */

package hw1;

public class PositionImpl implements Position {
  int quantity = 0;
  String symbol = null;

  /**
   * Construtor for this class.
   * 
   * @param symbo, the symbol of this position
   * @param quantity, the quantity this a postion.
   */
  public PositionImpl(String symbol, int quantity) {
    this.quantity = quantity;
    this.symbol = symbol;
  }
  
  /**
   * Method to get the quantity.
   *
   * @return quantity of this position
   */
  public int getQuantity() {
    return quantity;
  }

  /**
   * Method to get the symbol.
   *
   * @return symbol of this position
   */
  public String getSymbol() {
    return symbol;
  }

  /**
   * Package-private Method to set the quantity.
   *
   * @param quantity, the new quantity of this position
   */
  void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  @Override
  public String toString() {
    return "Symbol:" + symbol + " Quantity:" + quantity;
  }
}

