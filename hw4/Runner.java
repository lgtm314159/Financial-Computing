package hw4;

import java.util.Iterator;
import orderGenerator.Message;
import orderGenerator.OrdersIterator;
import orderGenerator.OrdersIterator.OrderCxRImpl;
import orderGenerator.OrdersIterator.OrderImpl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

public class Runner {
  private Iterator<Message> iter = OrdersIterator.getIterator();
  private HashMap<String, TreeMap<Double, LinkedList<OrderOnBook>>> ask;
  private HashMap<String, TreeMap<Double, LinkedList<OrderOnBook>>> bid;
  //private HashMap<String, LinkedList<OrderOnBook>> ask;
  //private HashMap<String, LinkedList<OrderOnBook>> bid;
  private HashMap<String, OrderOnBook> lookupTable;

  public void test() {
    while (iter.hasNext()) {
    //for (int i = 0; i < 300; ++i) {
      Message msg = iter.next();
      if (msg instanceof OrderImpl) {
        OrderImpl order = (OrderImpl) msg;
        OrderOnBook orderOnBook = new OrderOnBook(order.getSymbol(),
            order.getSize(), order.getOrderId(), order.getLimitPrice());
        
        if (orderOnBook.getSize() < 0) {
          boolean fullyExecuted = execOrder(orderOnBook);
          if (!fullyExecuted) {
            if (ask.containsKey(orderOnBook.getSymbol())) {
              TreeMap<Double, LinkedList<OrderOnBook>> subBook =
                  ask.get(orderOnBook.getSymbol());
              if (subBook.containsKey(orderOnBook.getLimitPrice())) {
                subBook.get(orderOnBook.getLimitPrice()).add(orderOnBook);
              } else {
                LinkedList<OrderOnBook> list =
                    new LinkedList<OrderOnBook>();
                list.add(orderOnBook);
                subBook.put(orderOnBook.getLimitPrice(), list);
              }
            } else {
              TreeMap<Double, LinkedList<OrderOnBook>> subBook =
                  new TreeMap<Double, LinkedList<OrderOnBook>>();
              LinkedList<OrderOnBook> list = new LinkedList<OrderOnBook>();
              list.add(orderOnBook);
              subBook.put(orderOnBook.getLimitPrice(), list);
              ask.put(order.getSymbol(), subBook);
            }
            lookupTable.put(orderOnBook.getOrderId(), orderOnBook);
          }
        }
        
        if (orderOnBook.getSize() > 0) {
          boolean fullyExecuted = execOrder(orderOnBook);
          if (!fullyExecuted) {
            if (bid.containsKey(orderOnBook.getSymbol())) {
              TreeMap<Double, LinkedList<OrderOnBook>> subBook =
                  bid.get(orderOnBook.getSymbol());
              if (subBook.containsKey(orderOnBook.getLimitPrice())) {
                subBook.get(orderOnBook.getLimitPrice()).add(orderOnBook);
              } else {
                LinkedList<OrderOnBook> list =
                    new LinkedList<OrderOnBook>();
                list.add(orderOnBook);
                subBook.put(orderOnBook.getLimitPrice(), list);
              }
            } else {
              TreeMap<Double, LinkedList<OrderOnBook>> subBook =
                  new TreeMap<Double, LinkedList<OrderOnBook>>();
              LinkedList<OrderOnBook> list = new LinkedList<OrderOnBook>();
              list.add(orderOnBook);
              subBook.put(orderOnBook.getLimitPrice(), list);
              bid.put(order.getSymbol(), subBook);
            }
            lookupTable.put(orderOnBook.getOrderId(), orderOnBook);
          }
        }
      } else if (msg instanceof OrdersIterator.OrderCxRImpl) {
        OrderCxRImpl orderCxR = (OrderCxRImpl) msg;
        if (orderCxR.getSize() < 0) {
          System.out.println("OrderCxR");
          System.out.println(orderCxR.getSize() + " " + orderCxR.getOrderId()
              + " " + orderCxR.getLimitPrice());
        }
      }
    }
  }

  public boolean execOrder(OrderOnBook order) throws IllegalArgumentException {
    String symbol = order.getSymbol();
    double price = order.getLimitPrice();
    if (order.getSize() < 0) {
      // This is a sell order. Checking the bid book to see if there is any
      // match.
      if (!Double.isNaN(price)) {
        // This is a limit order.
        if (bid.containsKey(symbol)) {
          int size = -order.getSize();
          size = execLimitOrder(size, order.getSymbol(), order.getLimitPrice(), true);
          order.setSize(size);
          if (size == 0)
            return true;
          else
            return false;
        } else {
          return false;
        }
      } else {
        // This is a market order.
        if (bid.containsKey(symbol)) {
          int size = -order.getSize();
          size = execMarketOrder(size, symbol, true);
          order.setSize(-size);
          if (size == 0) {
            return true;
          } else {
            return false;
          }
        } else {
          return false;
        }
      }
    }

    if (order.getSize() > 0) {
      // This is a buy order. Checking the ask book to see if there is any
      // match.
      if (!Double.isNaN(price)) {
        // This is a limit order.
        if (ask.containsKey(symbol)) {
          int size = order.getSize();
          
        } else {
          return false;
        }
      } else {
        // This is a market order.
        if (ask.containsKey(symbol)) {
          int size = order.getSize();
          size = execMarketOrder(size, symbol, false);
          order.setSize(size);
          if (size == 0) {
            return true;
          } else {
            return false;
          }
        } else {
          return false;
        }
      }
    }
    
    throw new IllegalArgumentException("Trying to execute a cancelled order");
  }

  private int execLimitOrder(int size, String symbol, double price, boolean isSell) {
    if (isSell) {
      // This is a sell limit order. Check the bid book
      // to see if there is any match.
      
      // Execute against market orders on bid book first because
      // they have higher priority.
      size = execLimitOrderAtPrice(size, symbol, Double.NaN, isSell);
      
      // If after execution against market orders on bid book the sell limit
      // order still hasn't been fully executed, proceed with limit orders on
      // the bid book.
      if (size > 0) {
        // Execute with bidding prices from low to high.
        SortedSet<Double> candidatePrices =
            bid.get(symbol).navigableKeySet().tailSet(price);
        for (Double canPrice: candidatePrices) {
          if (!canPrice.isNaN()) {
            size = execLimitOrderAtPrice(size, symbol, canPrice.doubleValue(), isSell);
            if (size == 0) {
              break;
            }
          }
        }
      }

      return size;
    } else {
      // This is a buy limit order. Check the ask book to see if there is
      // any match.
      
      // Execute against market orders on ask book first because
      // they have higher priority.
      size = execLimitOrderAtPrice(size, symbol, Double.NaN, isSell);
      
      // If after execution against market orders on ask book the buy limit
      // order still hasn't been fully executed, proceed with limit orders on
      // the ask book.
      if (size > 0) {
        SortedSet<Double> candidatePrices =
            ask.get(symbol).descendingKeySet().tailSet(price);
        for (Double canPrice: candidatePrices) {
          if (!canPrice.isNaN()) {
            size = execLimitOrderAtPrice(size, symbol, canPrice.doubleValue(), isSell);
            if (size == 0) {
              break;
            }
          }
        }
      }
      
      return size;
    }
  }
  
  private int execLimitOrderAtPrice(int size, String symbol, double price, boolean isSell) {
    LinkedList<OrderOnBook> list;
    if (isSell) {
      list = bid.get(symbol).get(price);
    } else {
      list = ask.get(symbol).get(price);
    }
    for (int i = 0; i < list.size(); ++i) {
      if (size > list.get(i).getSize()) {
        size -= list.get(i).getSize();
        // The bid order gets fully executed, removing it from the queue.
        list.remove(i);
        --i;
      } else if (size < list.get(i).getSize()) {
        // The sell order gets fully executed.
        size = 0;
        list.get(i).setSize(list.get(i).getSize() - size);
        break;
      } else {
        // Both the sell and bid orders get fully executed.
        size = 0;
        list.remove(i);
        break;
      }
    }
    return size;
  }

  private int execMarketOrder(int size, String symbol, boolean isSell) {
    TreeMap<Double, LinkedList<OrderOnBook>> subBook = bid.get(symbol);
    Set<Map.Entry<Double, LinkedList<OrderOnBook>>> entrySet;
    if (isSell) {
      entrySet = subBook.descendingMap().entrySet();
    } else {
      entrySet = subBook.entrySet();
    }
    for(Map.Entry<Double, LinkedList<OrderOnBook>> entry : entrySet) {
      LinkedList<OrderOnBook> list = entry.getValue();
      for (int i = 0; i < list.size(); ++i) {
        if (size > list.get(i).getSize()) {
          size -= list.get(i).getSize();
          list.remove(i);
          --i;
        } else if (size < list.get(i).getSize()) {
          size = 0;
          list.get(i).setSize(list.get(i).getSize() - size);
          break;
        } else {
          size = 0;
          list.remove(i);
          break;
        }
      }
      
      if (size == 0) {
        break;
      }
    }
    return size;
  }

  public static void main(String[] args) {
    Runner runner = new Runner();
    runner.test();
  }
}
