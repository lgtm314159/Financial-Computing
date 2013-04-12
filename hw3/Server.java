import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.broker.BrokerService;

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

import org.apache.commons.math3.analysis.function.Exp;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.analysis.function.Sqrt;
import org.apache.commons.math3.analysis.function.Ceil;

import hw2.Accumulator;

public class Server implements Runnable {
  private Connection connection; 
  private Session reqSession;
  private Session resSession;
  private MessageProducer reqProducer;
  private MessageConsumer resConsumer;
  private double confIntervalProb = 0;
  private double error = 0;
  private double q = 0;
  private double[] payouts;

  public Server(double confIntervalProb, double error) {
    this.confIntervalProb = confIntervalProb;
    this.error = error;
    NormalDistribution nd = new NormalDistribution();
    q = nd.inverseCumulativeProbability((confIntervalProb + 1) / 2);
    payouts = new double[100];
  }

  public static void main(String[] args) {
    thread(new Server(0.96, 0.01), false);
  }

  public void run() {
    try {
      // Create a ConnectionFactory

      BrokerService broker = new BrokerService();
      broker.setPersistent(false);
      broker.setUseJmx(false);
      broker.addConnector("tcp://localhost:61616");
      broker.start();

      //String url = ActiveMQConnection.DEFAULT_BROKER_URL;
      //ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
      ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

      // Create a Connection
      connection = connectionFactory.createConnection();
      connection.start();

      // Create a session for the simulation requests
      reqSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      // Create the destination queue for the simulation requests
      Destination destination = reqSession.createQueue("SimulationRequest");
      // Create a MessageProducer for sending simulation requests
      reqProducer = reqSession.createProducer(destination);
      reqProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

      String topic = "SimulationResult";
      // Create a session for the simulation results
      resSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      // Create a topic for the simulation results
      Topic resTopic = resSession.createTopic(topic);
      // Create a MessageConsumer for sending the simulation results 
      resConsumer= resSession.createConsumer(resTopic);

      /* Calculate the EU call option price. */
      double r = 0.0001;
      double sigma = 0.01;
      double strikePrice = 165;
      double originPrice = 152.35;
      int duration = 252;
      String auctionType = "EuCall";
      Exp exp = new Exp();
      Sqrt sqrt = new Sqrt();
      Ceil ceil = new Ceil();
      double constant = exp.value(-r * duration);
      // The first 100 batch.
      for(int i = 0; i < 100; ++i) {
        // Send a simulation request.
        sendSimReq(r, sigma, strikePrice, originPrice,
            auctionType, duration, topic); 

        // Wait for a simulation result. 
        Message message = resConsumer.receive();
        if (message instanceof TextMessage) {
          payouts[i] = processSimRes(message);
        } else {
          System.out.println("Received: " + message);
        }
      }
      int payoutIndex = 0;
      Accumulator accumulatorEu = new Accumulator();
      accumulatorEu.accumMean(payouts[payoutIndex], 1);
      accumulatorEu.accumQuadMean(payouts[payoutIndex] * payouts[payoutIndex], 1);
      double stdDeviEu = sqrt.value(accumulatorEu.getQuadMean()
          - accumulatorEu.getMean() * accumulatorEu.getMean());
      ++payoutIndex;
      // Set n to greater than 1 as initial value so the program can carry on to
      // subsequent iterations and update n accordingly.
      long n = 10;
      System.out.println(
          "Calculating the EU call option price with relevant error 0.01,"
          + "this may take a while...");
      for (long i = 2; i <= n; ++i) {
        //System.out.println(n + " " + i);
        accumulatorEu.accumMean(payouts[payoutIndex], i);
        accumulatorEu.accumQuadMean(payouts[payoutIndex] * payouts[payoutIndex], i);
        // Estimate the standard deviatoin on the fly.
        stdDeviEu = sqrt.value(accumulatorEu.getQuadMean()
            - accumulatorEu.getMean() * accumulatorEu.getMean());
        // Update n accordingly.
        n = (long) ceil.value((q * stdDeviEu / (error * accumulatorEu.getMean()))
            * (q * stdDeviEu / (error * accumulatorEu.getMean())));
        // Sometimes at some early iterations the sigma gets 0, so I am setting
        // n to be 10000 when this happens, to get the iteration going. 
        if (n == 0) {
          n = 10000;
        }

        ++payoutIndex;
        // Request the client to generate another batch of 100 simulated payouts.
        if (payoutIndex == 100) {
          payoutIndex = 0;
          // The first 100 batch.
          for(int j = 0; j < 100; ++j) {
            // Send a simulation request.
            sendSimReq(r, sigma, strikePrice, originPrice,
                auctionType, duration, topic); 

            // Wait for a simulation result. 
            Message message = resConsumer.receive();
            if (message instanceof TextMessage) {
              payouts[j] = processSimRes(message);
            } else {
              System.out.println("Received: " + message);
            }
          }
        }
      }
      double euCallOptionPrice = accumulatorEu.getMean() * constant;
      System.out.println("EU call option price is: " + euCallOptionPrice);

      /* Calculate the Asian call option price. */
      strikePrice = 164;
      auctionType = "AsianCall";
      // The first 100 batch.
      for(int i = 0; i < 100; ++i) {
        // Send a simulation request.
        sendSimReq(r, sigma, strikePrice, originPrice,
            auctionType, duration, topic); 

        // Wait for a simulation result. 
        Message message = resConsumer.receive();
        if (message instanceof TextMessage) {
          payouts[i] = processSimRes(message);
        } else {
          System.out.println("Received: " + message);
        }
      }
      payoutIndex = 0;
      Accumulator accumulatorAsian = new Accumulator();
      accumulatorAsian.accumMean(payouts[payoutIndex], 1);
      accumulatorAsian.accumQuadMean(payouts[payoutIndex] * payouts[payoutIndex], 1);
      double stdDeviAsian = sqrt.value(accumulatorAsian.getQuadMean()
          - accumulatorAsian.getMean() * accumulatorAsian.getMean());
      ++payoutIndex;
      // Set n to greater than 1 as initial value so the program can carry on to
      // subsequent iterations and update n accordingly.
      n = 10;
      /* Calculating the Asian call option price. */
      System.out.println(
          "Calculating the Asian call option price with relevant error 0.01,"
          + "this may take a while...");
      for (long i = 2; i <= n; ++i) {
        //System.out.println(n + " " + i);
        accumulatorAsian.accumMean(payouts[payoutIndex], i);
        accumulatorAsian.accumQuadMean(payouts[payoutIndex] * payouts[payoutIndex], i);
        // Estimate the standard deviatoin on the fly.
        stdDeviAsian = sqrt.value(accumulatorAsian.getQuadMean()
            - accumulatorAsian.getMean() * accumulatorAsian.getMean());
        // Update n accordingly.
        n = (long) ceil.value((q * stdDeviAsian / (error * accumulatorAsian.getMean()))
            * (q * stdDeviAsian / (error * accumulatorAsian.getMean())));
        // Sometimes at some early iterations the sigma gets 0, so I am setting
        // n to be 10000 when this happens, to get the iteration going. 
        if (n == 0) {
          n = 10000;
        }
        ++payoutIndex;
        // Request the client to generate another batch of 100 simulated payouts.
        if (payoutIndex == 100) {
          payoutIndex = 0;
          // The first 100 batch.
          for(int j = 0; j < 100; ++j) {
            // Send a simulation request.
            sendSimReq(r, sigma, strikePrice, originPrice,
                auctionType, duration, topic); 

            // Wait for a simulation result. 
            Message message = resConsumer.receive();
            if (message instanceof TextMessage) {
              payouts[j] = processSimRes(message);
            } else {
              System.out.println("Received: " + message);
            }
          }
        }
      }
      double asianCallOptionPrice = accumulatorAsian.getMean() * constant;
      System.out.println("Asian call option price is: " + asianCallOptionPrice);

      cleanUp();
    } catch (Exception e) {
      System.out.println("Caught: " + e);
      e.printStackTrace();
    }
  }

  /*
   * Method to send the simulation request.
   *
   * @param r, the interest rate
   * @param sigma, the volatility 
   * @param strikePrice, the strike price
   * @param originPrice, the original price
   * @param auctionType, the type of auction to simulation 
   * @param duration, the option's duaration 
   * @param topic, the topic for simulation result messages 
   */
  private void sendSimReq(double r, double sigma, double strikePrice,
      double originPrice, String auctionType, int duration, String topic)
      throws javax.jms.JMSException {
    StringBuffer sb = new StringBuffer();
    sb.append(r);
    sb.append("|");
    sb.append(sigma);
    sb.append("|");
    sb.append(strikePrice);
    sb.append("|");
    sb.append(originPrice);
    sb.append("|");
    sb.append(auctionType);
    sb.append("|");
    sb.append(duration);
    sb.append("|");
    sb.append(topic);
    String simReqText = sb.toString();
    TextMessage simReqMsg = reqSession.createTextMessage(simReqText);
    reqProducer.send(simReqMsg);
  }

  /*
   * Method to process simulation result message.
   *
   * @param: message, the simulation result message
   * @return: the payout received from client
   */
  private double processSimRes(Message message) throws javax.jms.JMSException {
    TextMessage simResMsg= (TextMessage) message;
    String resText = simResMsg.getText();
    return Double.valueOf(resText);
  }

  /* Method to do the clean up, i.e. closing resources. */
  private void cleanUp() {
    try {
      if (reqProducer != null)
        reqProducer.close();
      if (reqSession != null)
        reqSession.close();
      if (resConsumer != null)
        resConsumer.close();
      if (resSession != null)
        resSession.close();
      if (connection != null)
        connection.close();
    } catch (Exception e) {
      System.out.println("Caught: " + e);
      e.printStackTrace();
    }
  }

  /* Method to launch a thread. */
  public static void thread(Runnable runnable, boolean daemon) {
    Thread brokerThread = new Thread(runnable);
    brokerThread.setDaemon(daemon);
    brokerThread.start();
  }
}

