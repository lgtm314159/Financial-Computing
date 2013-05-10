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

/*
 * Class which replays the iterator and processes the messages.
 * Prints the top of the ask and bid books after processing every message.
 * Also prints out the trade record in the format 'order xxx traded with
 * order yyy'.
 */
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

  /*
   * Method to replay the iterator and process the messages.
   */
  public void run() throws InvalidTradeException {
    while (iter.hasNext()) {
      Message msg = iter.next();
      if (msg instanceof NewOrder) {
        // If the message is a new order.
        NewOrder order = (NewOrder) msg;
        OrderOnBook orderOnBook = new OrderOnBook(order.getSymbol(),
            Math.abs(order.getSize()), order.getOrderId(), order.getLimitPrice());
        
        // Process sell order.
        if (order.getSize() < 0) {
          boolean fullyExecuted = execOrder(orderOnBook, true);
          if (!fullyExecuted) {
            insertToAskBook(orderOnBook);
          }
        }
        
        // Process buy order.
        if (order.getSize() > 0) {
          boolean fullyExecuted = execOrder(orderOnBook, false);
          if (!fullyExecuted) {
            insertToBidBook(orderOnBook);
          }
        }
        
        printTopOfBooks();
      } else if (msg instanceof OrderCxR) {
        // If the message is an order replacement/cancellation.
        OrderCxR orderCxR = (OrderCxR) msg;
        execOrderCxR(orderCxR);
        printTopOfBooks();
      }
    }
  }

  /*
   * Method to execute a new order.
   * 
   * @param order, an instance of OrderOnBook, which holds information
   * of an order.
   * @param isSell, indicates if this order is a sell order or buy order.
   *                True for sell, false for buy.
   * @return, a boolean value indicating if this new order has been fully
   * executed, i.e. has been fully traded.
   */
  public boolean execOrder(OrderOnBook order, boolean isSell)
      throws InvalidTradeException {
    if (order.getSize() == 0) {
      // It is invalid to trade a cancelled order.
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

  /*
   * Method to execute a limit order.
   * 
   * @param size, the size of this limit order
   * @param symbol, the symbol of this limit order
   * @param price, the limit price of this limit order
   * @param orderId, the id of this limit order
   * @param isSell, indicates if this order is a sell order or buy order.
   * True for sell, false for buy.
   * @return, the size of this order after the execution.
   */
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

  /*
   * Method to execute a limit order at a certain price. This price is of a
   * corresponding order which this limit order is trading with.
   * 
   * @param size, the size of this limit order
   * @param symbol, the symbol of this limit order
   * @param price, the price of the corresponding order which this limit order
   *               is trading with
   * @param orderId, the id of this limit order
   * @param isSell, indicates if this order is a sell order or buy order.
   *                True for sell, false for buy
   * @return, the size of this limit order after the trading
   */
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
        if (list.get(i).getSize() != 0) {
          String tradedOrderId = list.get(i).getOrderId();
          printTrade(orderId, tradedOrderId);
        }
        // The bid order gets fully executed, removing it from the queue.
        list.remove(i);
        --i;
      } else if (resultSize < list.get(i).getSize()) {
        String tradedOrderId = list.get(i).getOrderId();
        printTrade(orderId, tradedOrderId);
        // The sell order gets fully executed.
        list.get(i).setSize(list.get(i).getSize() - resultSize);
        resultSize = 0;
        break;
      } else {
        if (list.get(i).getSize() != 0) {
          String tradedOrderId = list.get(i).getOrderId();
          printTrade(orderId, tradedOrderId);
        }
        // Both the sell and bid orders get fully executed.
        resultSize = 0;
        list.remove(i);
        break;
      }
    }
    return resultSize;
  }

  /*
   * Method to execute a market order.
   * 
   * @param size, the size of this limit order
   * @param symbol, the symbol of this limit order
   * @param orderId, the id of this limit order
   * @param isSell, indicates if this order is a sell order or buy order.
   *                True for sell, false for buy
   * @return, the size of this limit order after the trading
   */
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
            if (list.get(i).getSize() != 0) {
              String tradedOrderId = list.get(i).getOrderId();
              printTrade(orderId, tradedOrderId);
            }
            // The order on book gets fully executed.
            resultSize -= list.get(i).getSize();
            list.remove(i);
            --i;
          } else if (resultSize < list.get(i).getSize()) {
            String tradedOrderId = list.get(i).getOrderId();
            printTrade(orderId, tradedOrderId);
            // The new order gets fully executed.
            list.get(i).setSize(list.get(i).getSize() - resultSize);
            resultSize = 0;
            break;
          } else {
            if (list.get(i).getSize() != 0) {
              String tradedOrderId = list.get(i).getOrderId();
              printTrade(orderId, tradedOrderId);
            }
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

  /*
   * Method to execute an order replacement/cancellation.
   * 
   * @param orderCxR, an object of type OrderCxR which holds the
   *                  replacement/cancellation information
   */
  public void execOrderCxR(OrderCxR orderCxR) {
    // Set the replaced/cancelled order's size to 0 which indicates
    // it is now a dead order.
    lookupTable.get(orderCxR.getOrderId()).setSize(0);
    if (orderCxR.getSize() != 0) {
      // This is an order replacement, so create a new order and
      // put it to the back of the corresponding queue.
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

  /*
   * Method to insert an order to the ask book.
   * 
   * @param orderOnBook, an instance of OrderOnBook.
   */
  public void insertToAskBook(OrderOnBook orderOnBook) {
    if (ask.containsKey(orderOnBook.getSymbol())) {
      // If the ask book already have an entry for this order's symbol.
      TreeMap<Double, LinkedList<OrderOnBook>> subBook =
          ask.get(orderOnBook.getSymbol());
      if (subBook.containsKey(orderOnBook.getLimitPrice())) {
        // If the subbook of this order's symbol already has a queue for
        // this order's limit price, just insert the order to the back of
        // the queue.
        subBook.get(orderOnBook.getLimitPrice()).add(orderOnBook);
      } else {
        // If the subbook of this order's symbol hasn't had a queue for this
        // order's limit price yet, create one for it and add the order to
        // this new queue.
        LinkedList<OrderOnBook> list =
            new LinkedList<OrderOnBook>();
        list.add(orderOnBook);
        subBook.put(orderOnBook.getLimitPrice(), list);
      }
    } else {
      // If the ask book hasn't got an entry for this order's symbol,
      // create everything from scratch and put them at the right places.
      TreeMap<Double, LinkedList<OrderOnBook>> subBook =
          new TreeMap<Double, LinkedList<OrderOnBook>>();
      LinkedList<OrderOnBook> list = new LinkedList<OrderOnBook>();
      list.add(orderOnBook);
      subBook.put(orderOnBook.getLimitPrice(), list);
      ask.put(orderOnBook.getSymbol(), subBook);
    }
    // Add the order to the lookup table which allows instant time lookup
    // of the order.
    lookupTable.put(orderOnBook.getOrderId(), orderOnBook);
  }

  /*
   * Method to insert an order to the bid book.
   * 
   * @param orderOnBook, an instance of OrderOnBook.
   */
  public void insertToBidBook(OrderOnBook orderOnBook) {
    if (bid.containsKey(orderOnBook.getSymbol())) {
      // If the bid book already have an entry for this order's symbol.
      TreeMap<Double, LinkedList<OrderOnBook>> subBook =
          bid.get(orderOnBook.getSymbol());
      if (subBook.containsKey(orderOnBook.getLimitPrice())) {
        // If the subbook of this order's symbol already has a queue for
        // this order's limit price, just insert the order to the back of
        // the queue.
        subBook.get(orderOnBook.getLimitPrice()).add(orderOnBook);
      } else {
        // If the subbook of this order's symbol hasn't had a queue for this
        // order's limit price yet, create one for it and add the order to
        // this new queue.
        LinkedList<OrderOnBook> list =
            new LinkedList<OrderOnBook>();
        list.add(orderOnBook);
        subBook.put(orderOnBook.getLimitPrice(), list);
      }
    } else {
      // If the bid book hasn't got an entry for this order's symbol,
      // create everything from scratch and put them at the right places.
      TreeMap<Double, LinkedList<OrderOnBook>> subBook =
          new TreeMap<Double, LinkedList<OrderOnBook>>();
      LinkedList<OrderOnBook> list = new LinkedList<OrderOnBook>();
      list.add(orderOnBook);
      subBook.put(orderOnBook.getLimitPrice(), list);
      bid.put(orderOnBook.getSymbol(), subBook);
    }
    // Add the order to the lookup table which allows instant time lookup
    // of the order.
    lookupTable.put(orderOnBook.getOrderId(), orderOnBook);
  }

  /*
   * Method to print the top of ask and bid books.
   */
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

  /*
   * Method to print a trade.
   */
  private void printTrade(String traderId, String tradeeId) {
    String output =
        "Order " + traderId + " traded with " + tradeeId;
    System.out.println(output);
  }

  /*
   * Main method to run the simulation.
   */
  public static void main(String[] args) {
    Runner runner = new Runner();
    try {
      runner.run();
    } catch (InvalidTradeException e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }
}
