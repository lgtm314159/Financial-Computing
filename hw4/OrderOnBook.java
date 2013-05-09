package hw4;

public class OrderOnBook {
  private String symbol;
  private int size;
  private String orderId;
  private double limitPrice;
  boolean dead;

  public OrderOnBook(String symbol, int size, String orderId,
      double limitPrice) {
    this.symbol = symbol;
    this.size = size;
    this.orderId = orderId;
    this.limitPrice = limitPrice;
    this.dead = false;
  }

  public String getSymbol() {
    return symbol;
  }

  public int getSize() {
    return size;
  }

  void setSize(int newSize) {
    size = newSize;
  }

  public String getOrderId() {
    return orderId;
  }

  public double getLimitPrice() {
    return limitPrice;
  }
  
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
    sb.append(limitPrice);
    return sb.toString();
  }
}
