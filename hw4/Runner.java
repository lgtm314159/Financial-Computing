package hw4;

import java.util.Iterator;
import orderGenerator.Message;
import orderGenerator.OrdersIterator;
import orderGenerator.OrderCxR;
import orderGenerator.NewOrder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

public class Runner {
  private Iterator<Message> iter = OrdersIterator.getIterator();
  private HashMap<String, TreeMap<Double, LinkedList<OrderOnBook>>> ask;
  private HashMap<String, TreeMap<Double, LinkedList<OrderOnBook>>> bid;
  private HashMap<String, OrderOnBook> lookupTable;
  
  public Runner() {
    ask = new HashMap<String, TreeMap<Double, LinkedList<OrderOnBook>>>();
    bid = new HashMap<String, TreeMap<Double, LinkedList<OrderOnBook>>>();
    lookupTable = new HashMap<String, OrderOnBook>();
  }

  public void test() throws InvalidTradeException {
    while (iter.hasNext()) {
      Message msg = iter.next();
      if (msg instanceof NewOrder) {
        NewOrder order = (NewOrder) msg;
        OrderOnBook orderOnBook = new OrderOnBook(order.getSymbol(),
            Math.abs(order.getSize()), order.getOrderId(), order.getLimitPrice());
        if (order.getSize() < 0) {
          boolean fullyExecuted = execOrder(orderOnBook, true);
          if (!fullyExecuted) {
            insertToAskBook(orderOnBook);
          }
        }
        
        if (order.getSize() > 0) {
          boolean fullyExecuted = execOrder(orderOnBook, false);
          if (!fullyExecuted) {
            insertToBidBook(orderOnBook);
          }
        }
        
        printTopOfBooks();
      } else if (msg instanceof OrderCxR) {
        OrderCxR orderCxR = (OrderCxR) msg;
        execOrderCxR(orderCxR);
        printTopOfBooks();
      }
    }
  }

  public boolean execOrder(OrderOnBook order, boolean isSell)
      throws InvalidTradeException {
    if (order.getSize() == 0) {
      throw new InvalidTradeException("Trading a cancelled order");
    } else {
      String symbol = order.getSymbol();
      double price = order.getLimitPrice();
      String orderId = order.getOrderId();
      if (isSell) {
        // This is a sell order. Checking the bid book to see if there is any
        // match.
        if (!Double.isNaN(price)) {
          // This is a limit order.
          if (bid.containsKey(symbol)) {
            int size = order.getSize();
            size = execLimitOrder(size, symbol, price, orderId, true);
            order.setSize(size);
            if (order.getSize() == 0)
              return true;
            else
              return false;
          } else {
            return false;
          }
        } else {
          // This is a market order.
          if (bid.containsKey(symbol)) {
            int size = order.getSize();
            size = execMarketOrder(size, symbol, orderId, true);
            order.setSize(size);
            if (order.getSize() == 0) {
              return true;
            } else {
              return false;
            }
          } else {
            return false;
          }
        }
      } else {
        // This is a buy order. Checking the ask book to see if there is any
        // match.
        if (!Double.isNaN(price)) {
          // This is a limit order.
          if (ask.containsKey(symbol)) {
            int size = order.getSize();
            size = execLimitOrder(size, symbol, price, orderId, false);
            order.setSize(size);
            if (order.getSize() == 0)
              return true;
            else
              return false;
          } else {
            return false;
          }
        } else {
          // This is a market order.
          if (ask.containsKey(symbol)) {
            int size = order.getSize();
            size = execMarketOrder(size, symbol, orderId, false);
            order.setSize(size);
            if (order.getSize() == 0) {
              return true;
            } else {
              return false;
            }
          } else {
            return false;
          }
        }
      }
    }
  }

  private int execLimitOrder(int size, String symbol, double price,
      String orderId, boolean isSell) {
    if (isSell) {
      // This is a sell limit order. Check the bid book
      // to see if there is any match.
      
      // Execute against market orders on bid book first because
      // they have higher priority.
      if (bid.get(symbol).containsKey(Double.NaN)) {
        size =
          execLimitOrderAtPrice(size, symbol, Double.NaN, orderId, isSell);
      }
      
      // If after execution against market orders on bid book the sell limit
      // order still hasn't been fully executed, proceed with limit orders on
      // the bid book.
      if (size > 0) {
        // Execute with bidding prices from low to high.
        SortedSet<Double> candidatePrices =
            bid.get(symbol).navigableKeySet().tailSet(price);
        for (Double canPrice: candidatePrices) {
          if (!canPrice.isNaN()) {
            size = execLimitOrderAtPrice(size, symbol, canPrice.doubleValue(),
                orderId, isSell);
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
      if (ask.get(symbol).containsKey(Double.NaN)) {
        size =
          execLimitOrderAtPrice(size, symbol, Double.NaN, orderId, isSell);
      }
      
      // If after execution against market orders on ask book the buy limit
      // order still hasn't been fully executed, proceed with limit orders on
      // the ask book.
      if (size > 0) {
        // Execute with asking prices from high to low.
        SortedSet<Double> candidatePrices =
            ask.get(symbol).descendingKeySet().tailSet(price);
        for (Double canPrice: candidatePrices) {
          if (!canPrice.isNaN()) {
            size = execLimitOrderAtPrice(size, symbol, canPrice.doubleValue(),
                orderId, isSell);
            if (size == 0) {
              break;
            }
          }
        }
      }
      
      return size;
    }
  }
  
  private int execLimitOrderAtPrice(int size, String symbol, double price,
      String orderId, boolean isSell) {
    LinkedList<OrderOnBook> list;
    if (isSell) {
      list = bid.get(symbol).get(price);
    } else {
      list = ask.get(symbol).get(price);
    }
    int resultSize = size;
    for (int i = 0; i < list.size(); ++i) {
      if (resultSize > list.get(i).getSize()) {
        resultSize -= list.get(i).getSize();
          String tradedOrderId = list.get(i).getOrderId();
          String output = "Order " + orderId + " traded with " + tradedOrderId;
          System.out.println(output);
        // The bid order gets fully executed, removing it from the queue.
        list.remove(i);
        --i;
      } else if (resultSize < list.get(i).getSize()) {
        String tradedOrderId = list.get(i).getOrderId();
        String output = "Order " + orderId + " traded with " + tradedOrderId;
        System.out.println(output);
        // The sell order gets fully executed.
        list.get(i).setSize(list.get(i).getSize() - resultSize);
        resultSize = 0;
        break;
      } else {
          String tradedOrderId = list.get(i).getOrderId();
          String output = "Order " + orderId + " traded with " + tradedOrderId;
          System.out.println(output);
        // Both the sell and bid orders get fully executed.
        resultSize = 0;
        list.remove(i);
        break;
      }
    }
    return resultSize;
  }

  private int execMarketOrder(int size, String symbol, String orderId,
      boolean isSell) {
    Set<Map.Entry<Double, LinkedList<OrderOnBook>>> entrySet;
    if (isSell) {
      TreeMap<Double, LinkedList<OrderOnBook>> subBook = bid.get(symbol);
      entrySet = subBook.descendingMap().entrySet();
    } else {
      TreeMap<Double, LinkedList<OrderOnBook>> subBook = ask.get(symbol);
      entrySet = subBook.entrySet();
    }
    int resultSize = size;
    for(Map.Entry<Double, LinkedList<OrderOnBook>> entry : entrySet) {
      if (!entry.getKey().isNaN()) {
        LinkedList<OrderOnBook> list = entry.getValue();
        for (int i = 0; i < list.size(); ++i) {
          if (resultSize > list.get(i).getSize()) {
              String tradedOrderId = list.get(i).getOrderId();
              String output =
                  "Order " + orderId + " traded with " + tradedOrderId;
              System.out.println(output);
            // The order on book gets fully executed.
            resultSize -= list.get(i).getSize();
            list.remove(i);
            --i;
          } else if (resultSize < list.get(i).getSize()) {
            String tradedOrderId = list.get(i).getOrderId();
            String output =
                "Order " + orderId + " traded with " + tradedOrderId;
            System.out.println(output);
            // The new order gets fully executed.
            list.get(i).setSize(list.get(i).getSize() - resultSize);
            resultSize = 0;
            break;
          } else {
              String tradedOrderId = list.get(i).getOrderId();
              String output =
                  "Order " + orderId + " traded with " + tradedOrderId;
              System.out.println(output);
            // Both the new order and the order on book get fully executed.
            resultSize = 0;
            list.remove(i);
            break;
          }
        }
        
        if (resultSize == 0) {
          break;
        }
      }
    }
    return resultSize;
  }

  public void execOrderCxR(OrderCxR orderCxR) {
    lookupTable.get(orderCxR.getOrderId()).setSize(0);
    if (orderCxR.getSize() != 0) {
      String symbol = lookupTable.get(orderCxR.getOrderId()).getSymbol();
      OrderOnBook orderOnBook =
          new OrderOnBook(symbol, Math.abs(orderCxR.getSize()),
          orderCxR.getOrderId(), orderCxR.getLimitPrice());
      if (orderCxR.getSize() < 0) {
        insertToAskBook(orderOnBook);
      }
      if (orderCxR.getSize() > 0) {
        insertToBidBook(orderOnBook);
      }
    }
  }

  public void insertToAskBook(OrderOnBook orderOnBook) {
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
      ask.put(orderOnBook.getSymbol(), subBook);
    }
    lookupTable.put(orderOnBook.getOrderId(), orderOnBook);
  }

  public void insertToBidBook(OrderOnBook orderOnBook) {
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
      bid.put(orderOnBook.getSymbol(), subBook);
    }
    lookupTable.put(orderOnBook.getOrderId(), orderOnBook);
  }

  private void printTopOfBooks() {
    System.out.println("Top of the ask book:");
    for (Map.Entry<String, TreeMap<Double, LinkedList<OrderOnBook>>> entry: ask.entrySet()) {
      System.out.print(entry.getKey() + " ");
      System.out.println(entry.getValue().firstEntry().getValue());
    }
    System.out.println("Top of the bid book:");
    for (Map.Entry<String, TreeMap<Double, LinkedList<OrderOnBook>>> entry: bid.entrySet()) {
      System.out.print(entry.getKey() + " ");
      System.out.println(entry.getValue().lastEntry().getValue());
    }
  }

  public static void main(String[] args) {
    Runner runner = new Runner();
    try {
      runner.test();
    } catch (InvalidTradeException e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }
}
