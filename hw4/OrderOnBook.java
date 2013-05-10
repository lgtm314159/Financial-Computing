package hw4;

/**
 * Class that represents an order on a ask/bid book.
 */
public class OrderOnBook {
  private String symbol;
  private int size;
  private String orderId;
  private double limitPrice;
  boolean dead;

  /**
   * Class constructor that specifies the symbol, size, id and limit price
   * of an order.
   * 
   * @param symbol, symbol of an order
   * @param size, size of an order
   * @param orderId, id of an order
   * @param limitPrice, limit price of an order. NaN indicates it is a
   *                    market order
   */
  public OrderOnBook(String symbol, int size, String orderId,
      double limitPrice) {
    this.symbol = symbol;
    this.size = size;
    this.orderId = orderId;
    this.limitPrice = limitPrice;
    this.dead = false;
  }

  /**
   * Method to get the symbol of this order.
   * 
   * @return, the symbol of this order
   */
  public String getSymbol() {
    return symbol;
  }

  /**
   * Method to get the size of this order.
   * 
   * @return, the size of this order
   */
  public int getSize() {
    return size;
  }

  /**
   * Method to set the size of this order.
   * 
   * @param newSize, the new size of this order
   */  
  void setSize(int newSize) {
    size = newSize;
  }

  /**
   * Method to get the id of this order.
   * 
   * @return, the id of this order
   */
  public String getOrderId() {
    return orderId;
  }

  /**
   * Method to get the limit price of this order.
   * 
   * @return, the limit price of this order
   */
  public double getLimitPrice() {
    return limitPrice;
  }

  /**
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Id:");
    sb.append(orderId);
    sb.append(" Dead:");
    if (size == 0) {
      sb.append("true");
    } else {
      sb.append("false");
    }
    sb.append(" Size:");
    sb.append(size);
    sb.append(" Price:");
    sb.append(String.format("%.2f", limitPrice));
    return sb.toString();
  }
}
