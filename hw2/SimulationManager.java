import org.apache.commons.math3.analysis.function.Exp;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.analysis.function.Sqrt;
import org.apache.commons.math3.analysis.function.Ceil;

public class SimulationManager {
  private double confIntervalProb = 0;
  private double confIntervalRadius = 0;
  private double q = 0;

  public static void main(String[] args) {
    SimulationManager simMan = new SimulationManager(0.96, 0.01);
    simMan.runSimulation();
  }

  public SimulationManager(double confIntervalProb, double confIntervalRadius) {
    this.confIntervalProb = confIntervalProb;
    this.confIntervalRadius = confIntervalRadius;
    NormalDistribution nd = new NormalDistribution();
    q = nd.inverseCumulativeProbability((confIntervalProb + 1) / 2);
  }

  public void runSimulation() {
    Exp exp = new Exp();
    Sqrt sqrt = new Sqrt();
    Ceil ceil = new Ceil();
    double constant = exp.value(-0.0001 * 252);
    /*
    StockPathImpl stockPathEu = new StockPathImpl(165, 0.01, 0.0001, 252);
    PayOut euCallPayOut = new EuCallPayOutImpl(152.35);
    double euPayOut = euCallPayOut.getPayout(stockPathEu);
    double euCallOptionPrice = euPayOut * constant;
    Accumulator accumulatorEu = new Accumulator();
    accumulatorEu.accumMean(euCallOptionPrice, 1);
    accumulatorEu.accumQuadMean(euCallOptionPrice * euCallOptionPrice, 1);
    double stdDeviEu = sqrt.value(accumulatorEu.getQuadMean() - accumulatorEu.getMean() * accumulatorEu.getMean());
    //System.out.println(accumulatorEu.getQuadMean() - accumulatorEu.getMean() * accumulatorEu.getMean());
    //int n = (int) ceil.value((q * stdDeviEu / confIntervalRadius) * (q * stdDeviEu / confIntervalRadius));
    int n = 10;
    for (int i = 2; i <= n; ++i) {
      euPayOut = euCallPayOut.getPayout(stockPathEu);
      euCallOptionPrice = euPayOut * constant;
      accumulatorEu.accumMean(euCallOptionPrice, i);
      accumulatorEu.accumQuadMean(euCallOptionPrice * euCallOptionPrice, i);
      stdDeviEu = sqrt.value(accumulatorEu.getQuadMean()
          - accumulatorEu.getMean() * accumulatorEu.getMean());
      //System.out.println(stdDeviEu);
      n = (int) ceil.value((q * stdDeviEu / confIntervalRadius) * (q * stdDeviEu / confIntervalRadius));
      //int tmp = (int) ceil.value((q * stdDeviEu / confIntervalRadius) * (q * stdDeviEu / confIntervalRadius));
      //System.out.println(tmp);
      System.out.println(n);
    }
    System.out.println("EU call option price is: " + euCallOptionPrice);
*/
    StockPathImpl stockPathAsian = new StockPathImpl(164, 0.01, 0.0001, 252);
    //System.out.println(stockPathAsian.getPrices());
    PayOut asianCallPayOut = new AsianCallPayOutImpl(152.35);
    double asianPayOut = asianCallPayOut.getPayout(stockPathAsian);
    double asianCallOptionPrice = asianPayOut * constant;
    Accumulator accumulatorAsian = new Accumulator();
    accumulatorAsian.accumMean(asianCallOptionPrice, 1);
    accumulatorAsian.accumQuadMean(asianCallOptionPrice * asianCallOptionPrice, 1);
    double stdDeviAsian = sqrt.value(accumulatorAsian.getQuadMean() - accumulatorAsian.getMean() * accumulatorAsian.getMean());
    //n = (int) ceil.value((q * stdDeviAsian / confIntervalRadius) * (q * stdDeviAsian / confIntervalRadius));
    int n = 100000;
    for (int i = 2; i <= n; ++i) {
      asianPayOut = asianCallPayOut.getPayout(stockPathAsian);
      asianCallOptionPrice = asianPayOut * constant;
      accumulatorAsian.accumMean(asianCallOptionPrice, i);
      accumulatorAsian.accumQuadMean(asianCallOptionPrice * asianCallOptionPrice, i);
      stdDeviAsian = sqrt.value(accumulatorAsian.getQuadMean() - accumulatorAsian.getMean() * accumulatorAsian.getMean());
      //System.out.println(accumulatorAsian.getMean() + " " + accumulatorAsian.getQuadMean());
      //n = (int) ceil.value((q * stdDeviAsian / confIntervalRadius) * (q * stdDeviAsian / confIntervalRadius));
      //System.out.println(n);
      //int tmp = (int) ceil.value((q * stdDeviAsian / confIntervalRadius) * (q * stdDeviAsian / confIntervalRadius));
      //System.out.println(tmp);
    }
    System.out.println("Asian call option price is: " + asianCallOptionPrice);
  }
}

