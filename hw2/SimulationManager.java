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
    StockPathImpl stockPathEu = new StockPathImpl(165, 0.01, 0.0001, 252);
    PayOut euCallPayOut = new EuCallPayOutImpl(152.35);
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
        "Calculating the EU call option price, this may take a while...");
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
      //System.out.println(n + " " + i);
    }
    double euCallOptionPrice = accumulatorEu.getMean() * constant;
    System.out.println("EU call option price is: " + euCallOptionPrice);

    /* Calculating the Asian call option price. */
    System.out.println(
        "Calculating the Asian call option price, this may take a while...");
    StockPathImpl stockPathAsian = new StockPathImpl(164, 0.01, 0.0001, 252);
    PayOut asianCallPayOut = new AsianCallPayOutImpl(152.35);
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
      //System.out.println(n + " " + i);
    }
    double asianCallOptionPrice = accumulatorAsian.getMean() * constant;
    System.out.println("Asian call option price is: " + asianCallOptionPrice);
  }
}

