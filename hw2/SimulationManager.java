/* Author: Junyang Xin (jx372@nyu.edu)
 * Date: 3/29/2013
 */

package hw2;

import org.apache.commons.math3.analysis.function.Exp;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.analysis.function.Sqrt;
import org.apache.commons.math3.analysis.function.Ceil;

public class SimulationManager {
  private double confIntervalProb = 0;
  private double error = 0;
  private double q = 0;

  public static void main(String[] args) {
    SimulationManager simMan = new SimulationManager(0.96, 0.01);
    simMan.runSimulation();
    simMan.runSimulationWithAbsError();
  }

  /*
   * Constructor to initialze the simultion manager.
   *
   * @param confIntervalProb the probability of the required
   *         confidence internval
   * @param error the error/deviation required
   */
  public SimulationManager(double confIntervalProb, double error) {
    this.confIntervalProb = confIntervalProb;
    this.error = error;
    NormalDistribution nd = new NormalDistribution();
    q = nd.inverseCumulativeProbability((confIntervalProb + 1) / 2);
  }

  /*
   * Method to run the simulation.
   */
  public void runSimulation() {
    Exp exp = new Exp();
    Sqrt sqrt = new Sqrt();
    Ceil ceil = new Ceil();
    double constant = exp.value(-0.0001 * 252);

    /* Calculating the Asian call option price. */
    StockPathImpl stockPathEu = new StockPathImpl(152.35, 0.01, 0.0001, 252);
    PayOut euCallPayOut = new EuCallPayOutImpl(165);
    double euPayOut = euCallPayOut.getPayout(stockPathEu);
    Accumulator accumulatorEu = new Accumulator();
    accumulatorEu.accumMean(euPayOut, 1);
    accumulatorEu.accumQuadMean(euPayOut * euPayOut, 1);
    double stdDeviEu = sqrt.value(accumulatorEu.getQuadMean()
        - accumulatorEu.getMean() * accumulatorEu.getMean());
    // Set n to greater than 1 as initial value so the program can carry on to
    // subsequent iterations and update n accordingly.
    long n = 10;
    System.out.println(
        "Calculating the EU call option price with relevant error 0.01,"
        + "this may take a while...");
    for (long i = 2; i <= n; ++i) {
      euPayOut = euCallPayOut.getPayout(stockPathEu);
      accumulatorEu.accumMean(euPayOut, i);
      accumulatorEu.accumQuadMean(euPayOut * euPayOut, i);
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
    }
    double euCallOptionPrice = accumulatorEu.getMean() * constant;
    System.out.println("EU call option price is: " + euCallOptionPrice);

    /* Calculating the Asian call option price. */
    System.out.println(
        "Calculating the Asian call option price with relevant error 0.01,"
        + "this may take a while...");
    StockPathImpl stockPathAsian = new StockPathImpl(152.35, 0.01, 0.0001, 252);
    PayOut asianCallPayOut = new AsianCallPayOutImpl(164);
    double asianPayOut = asianCallPayOut.getPayout(stockPathAsian);
    Accumulator accumulatorAsian = new Accumulator();
    accumulatorAsian.accumMean(asianPayOut, 1);
    accumulatorAsian.accumQuadMean(asianPayOut * asianPayOut, 1);
    double stdDeviAsian = sqrt.value(accumulatorAsian.getQuadMean()
        - accumulatorAsian.getMean() * accumulatorAsian.getMean());
    // Set n to greater than 1 as initial value so the program can carry on to
    // subsequent iterations and update n accordingly.
    n = 10;
    for (long i = 2; i <= n; ++i) {
      asianPayOut = asianCallPayOut.getPayout(stockPathAsian);
      accumulatorAsian.accumMean(asianPayOut, i);
      accumulatorAsian.accumQuadMean(asianPayOut * asianPayOut, i);
      // Estimate the standard deviatoin on the fly.
      stdDeviAsian = sqrt.value(accumulatorAsian.getQuadMean()
          - accumulatorAsian.getMean() * accumulatorAsian.getMean());
      // Update n accordingly.
      n = (long) ceil.value(
          (q * stdDeviAsian / (error * accumulatorAsian.getMean()))
          * (q * stdDeviAsian / (error * accumulatorAsian.getMean())));
      // Sometimes at some early iterations the sigma gets 0, so I am setting
      // n to be 10000 when this happens, to get the iteration going. 
      if (n == 0) {
        n = 10000;
      }
    }
    double asianCallOptionPrice = accumulatorAsian.getMean() * constant;
    System.out.println("Asian call option price is: " + asianCallOptionPrice);
  }

  /*
   * Method to run the simulation with absolute error. This causes the
   * Monte-Carlo iterations number to go up to 7+ million which takes long.
   * The absolute error is unlikely to be the correct choice so in the main
   * method the call to this method is commented out.
   */
  public void runSimulationWithAbsError() {
    Exp exp = new Exp();
    Sqrt sqrt = new Sqrt();
    Ceil ceil = new Ceil();
    double constant = exp.value(-0.0001 * 252);
    long n = 10;
    /* Calculating the Asian call option price. */
    StockPathImpl stockPathEu = new StockPathImpl(152.35, 0.01, 0.0001, 252);
    PayOut euCallPayOut = new EuCallPayOutImpl(165);
    double euPayOut = euCallPayOut.getPayout(stockPathEu);
    Accumulator accumulatorEu = new Accumulator();
    accumulatorEu.accumMean(euPayOut, 1);
    accumulatorEu.accumQuadMean(euPayOut * euPayOut, 1);
    double stdDeviEu = sqrt.value(accumulatorEu.getQuadMean()
        - accumulatorEu.getMean() * accumulatorEu.getMean());
    // Set n to greater than 1 as initial value so the program can carry on to
    // subsequent iterations and update n accordingly.
    System.out.println(
        "Calculating the EU call option price with absolute error 0.01, "
        + "this may take a much longer while...");
    for (long i = 2; i <= n; ++i) {
      euPayOut = euCallPayOut.getPayout(stockPathEu);
      accumulatorEu.accumMean(euPayOut, i);
      accumulatorEu.accumQuadMean(euPayOut * euPayOut, i);
      // Estimate the standard deviatoin on the fly.
      stdDeviEu = sqrt.value(accumulatorEu.getQuadMean()
          - accumulatorEu.getMean() * accumulatorEu.getMean());
      // Update n accordingly.
      n = (long) ceil.value((q * stdDeviEu / error)
          * (q * stdDeviEu / error));
      // Sometimes at some early iterations the sigma gets 0, so I am setting
      // n to be 10000 when this happens, to get the iteration going. 
      if (n == 0) {
        n = 10000;
      }
    }
    double euCallOptionPrice = accumulatorEu.getMean() * constant;
    System.out.println("EU call option price is: " + euCallOptionPrice);

    /* Calculating the Asian call option price. */
    System.out.println(
        "Calculating the Asian call option price with absolute error 0.01, "
        + "this may take a much longer while...");
    StockPathImpl stockPathAsian = new StockPathImpl(152.35, 0.01, 0.0001, 252);
    PayOut asianCallPayOut = new AsianCallPayOutImpl(164);
    double asianPayOut = asianCallPayOut.getPayout(stockPathAsian);
    Accumulator accumulatorAsian = new Accumulator();
    accumulatorAsian.accumMean(asianPayOut, 1);
    accumulatorAsian.accumQuadMean(asianPayOut * asianPayOut, 1);
    double stdDeviAsian = sqrt.value(accumulatorAsian.getQuadMean()
        - accumulatorAsian.getMean() * accumulatorAsian.getMean());
    // Set n to greater than 1 as initial value so the program can carry on to
    // subsequent iterations and update n accordingly.
    n = 10;
    for (long i = 2; i <= n; ++i) {
      asianPayOut = asianCallPayOut.getPayout(stockPathAsian);
      accumulatorAsian.accumMean(asianPayOut, i);
      accumulatorAsian.accumQuadMean(asianPayOut * asianPayOut, i);
      // Estimate the standard deviatoin on the fly.
      stdDeviAsian = sqrt.value(accumulatorAsian.getQuadMean()
          - accumulatorAsian.getMean() * accumulatorAsian.getMean());
      // Update n accordingly.
      n = (long) ceil.value(
          (q * stdDeviAsian / error)
          * (q * stdDeviAsian / error));
      // Sometimes at some early iterations the sigma gets 0, so I am setting
      // n to be 10000 when this happens, to get the iteration going. 
      if (n == 0) {
        n = 10000;
      }
    }
    double asianCallOptionPrice = accumulatorAsian.getMean() * constant;
    System.out.println("Asian call option price is: " + asianCallOptionPrice);
  }
}

