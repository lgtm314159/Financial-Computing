package orderGenerator;

import java.util.HashMap;
import java.util.LinkedList;

public class Runner {
  private HashMap<LinkedList<NewOrder>> askBook; 
  private HashMap<LinkedList<NewOrder>> bidBook; 

  public Runner() {
    askBook = new HashMap<LinkedList<NewOrder>>();
    bidBook = new HashMap<LinkedList<NewOrder>>();
  }

  public static void main(String[] args) {
    OrdersIterator iter = new OrdersIterator();
    iter.getIterator();
    //ORdersIterator.OrderCxRImpl a = new OrdersIterator.OrderCxRImpl();
  }

}

