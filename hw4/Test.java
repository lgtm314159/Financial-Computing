package hw4;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

import orderGenerator.OrdersIterator;
import java.util.Iterator;
import orderGenerator.Message;
import orderGenerator.OrdersIterator.OrderImpl;
import orderGenerator.OrdersIterator.OrderCxRImpl;


public class Test {
  public static void main(String[] args) {
    Iterator<Message> iter = OrdersIterator.getIterator();
    //for (int i = 0; i < 100; i++) {
    while (iter.hasNext()) {
      
      Message msg = iter.next();
      
      if (msg instanceof OrderImpl) {
      /*
      OrderImpl order = (OrderImpl) msg;
      System.out.println(order.getSymbol() + " " + 
          order.getSize() + " " + order.getOrderId() + " " + order.getLimitPrice());
      */
      } else {
        OrderCxRImpl orderCxR = (OrderCxRImpl) msg;
        if (orderCxR.getOrderId().equals(""))
          System.out.println(orderCxR.getOrderId() + " " + orderCxR.getLimitPrice() + " " + orderCxR.getSize());
      }
    }
  }
}
