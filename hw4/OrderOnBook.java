package hw4;

import orderGenerator.NewOrder;

public class OrderOnBook implements NewOrder {
  private String symbol;
  private int size;
  private String orderId;
  private double limitPrice;

  public OrderOnBook(String symbol, int size, String orderId,
      double limitPrice) {
    this.symbol = symbol;
    this.size = size;
    this.orderId = orderId;
    this.limitPrice = limitPrice;
  }

  public String getSymbol() {
    return symbol;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int newSize) {
    size = newSize;
  }

  public String getOrderId() {
    return orderId;
  }

  public double getLimitPrice() {
    return limitPrice;
  }
}
