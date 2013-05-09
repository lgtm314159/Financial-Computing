package hw4;

import orderGenerator.OrdersIterator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
        OrderImpl order = (OrderImpl) msg;
      /*
      
      //if (order.getOrderId().indexOf("MSFT") >= 0) {
      if (order.getSize() < 0) {
        System.out.println(order.getSymbol() + " " + 
            order.getSize() + " " + order.getOrderId() + " " + order.getLimitPrice());
        msg = iter.next();
        order = (OrderImpl) msg;
        System.out.println(order.getSymbol() + " " + 
            order.getSize() + " " + order.getOrderId() + " " + order.getLimitPrice());
      }
            */
      } else {
        OrderCxRImpl orderCxR = (OrderCxRImpl) msg;
        if (Double.isNaN(orderCxR.getLimitPrice()) && orderCxR.getSize() != 0) {
        }
        //if (orderCxR.getOrderId().equals(""))
        //if (orderCxR.getSize() == 0)
          //System.out.println(orderCxR.getOrderId() + " " + orderCxR.getLimitPrice() + " " + orderCxR.getSize());
      }
    }
  }
}
