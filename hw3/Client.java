import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import hw2.StockPathImpl;
import hw2.EuCallPayOutImpl;
import hw2.AsianCallPayOutImpl;
import hw2.PayOut;

public class Client implements Runnable, ExceptionListener {
  private Connection connection; 
  private Session reqSession;
  private Session resSession;
  private MessageConsumer reqConsumer;
  private MessageProducer resProducer;
  private StockPathImpl stockPath;
  private EuCallPayOutImpl euCallPayOut;
  private AsianCallPayOutImpl asianCallPayOut;
  private String topic;

  public void run() {
    try {
      // Create a ConnectionFactory
      ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");

      // Create a Connection
      connection = connectionFactory.createConnection();
      connection.start();
      connection.setExceptionListener(this);

      // Create a session for the simulation requests
      reqSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      // Create the destination queue for the simulation requests
      Destination destination = reqSession.createQueue("SimulationRequest");
      // Create a MessageConsumer for receiving simulation requests
      reqConsumer = reqSession.createConsumer(destination);

      // Create a session for the simulation results
      resSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      /*
      // Create a topic for the simulation results
      Topic resTopic = resSession.createTopic("SimulationResult");
      // Create a MessageProducer for sending the simulation results 
      resProducer = resSession.createProducer(resTopic);
      resProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
      */

      int nullMsgCnt = 0;
      for(;;) {
        /*
        // Wait for a simulation request 
        Message message = reqConsumer.receive(1000);

        if (message instanceof TextMessage) {
          TextMessage simReqMsg= (TextMessage) message;
          String text = simReqMsg.getText();
          System.out.println("Received: " + text);

          // Send back simulation result.
          String simResText = "Simulation result from: " + Thread.currentThread().getName() + " : " + "Client";
          TextMessage simResMsg = resSession.createTextMessage(simResText);
          System.out.println("Sent simulation result: "+ simResMsg.hashCode() + " : " + Thread.currentThread().getName());
          resProducer.send(simResMsg);
        } else {
            System.out.println("Received: " + message);
        }
        */

        // Wait for a simulation request 
        Message message = reqConsumer.receive(1000);
        if (message instanceof TextMessage) {
          String[] result = processSimReq(message);
          sendSimRes(result);
        } else {
          System.out.println("Received: " + message);
          ++nullMsgCnt;
          if (nullMsgCnt == 10)
            break;
        }

        //Thread.sleep(1000);
      }
      
      cleanUp();
    } catch (Exception e) {
      System.out.println("Caught: " + e);
      e.printStackTrace();
    }
  }

  private String[] processSimReq(Message message)
      throws javax.jms.JMSException {
    TextMessage simReqMsg= (TextMessage) message;
    String text = simReqMsg.getText();
    //System.out.println("Received: " + text);

    String[] fields = text.split("\\|");
    double r = Double.valueOf(fields[0]);
    double sigma = Double.valueOf(fields[1]);
    double strikePrice = Double.valueOf(fields[2]);
    double originPrice = Double.valueOf(fields[3]);
    String auctionType = fields[4];
    int duration = Integer.valueOf(fields[5]);

    StockPathImpl stockPath = new StockPathImpl(originPrice, sigma, r, duration); 
    if (auctionType.equals("EuCall")) {
      PayOut euCallPayOut = new EuCallPayOutImpl(strikePrice);
      double euPayOut = euCallPayOut.getPayout(stockPath);
      String[] result = new String[2];
      result[0] = euPayOut + "";
      result[1] = fields[6]; 
      return result;
    } else {
      PayOut asianCallPayOut = new AsianCallPayOutImpl(strikePrice);
      double asianPayOut = asianCallPayOut.getPayout(stockPath);
      String[] result = new String[2];
      result[0] = asianPayOut + "";
      result[1] = fields[6]; 
      return result;
    }
  }

  private void sendSimRes(String[] result) throws javax.jms.JMSException {
    // If it's the first of sending simulation results, or the topic has
    // been changed, then create a new message producer with the given topic.
    if (resProducer == null || !result[1].equals(topic)) {
      // Update the current topic.
      topic = result[1];
      Topic resTopic = resSession.createTopic(topic);
      // Create a MessageProducer for sending the simulation results. 
      resProducer = resSession.createProducer(resTopic);
      resProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
    }
    TextMessage simResMsg = resSession.createTextMessage(result[0]);
    resProducer.send(simResMsg);
  }

  private void cleanUp() {
    try {
      if (reqConsumer != null)
        reqConsumer.close();
      if (reqSession != null)
        reqSession.close();
      if (resProducer != null)
        resProducer.close();
      if (resSession != null)
        resSession.close();
      if (connection != null)
        connection.close();
    } catch (Exception e) {
      System.out.println("Caught: " + e);
      e.printStackTrace();
    }
  }

  public synchronized void onException(JMSException ex) {
      System.out.println("JMS Exception occured.  Shutting down client.");
  }
}

