import org.apache.commons.math3.analysis.function.Exp;
import org.apache.commons.math3.distribution.NormalDistribution;

public class Test {
  public static void main(String[] args) {
    StockPathImpl sp = new StockPathImpl(152.35, 0.01, 0.0001, 20);
    RandomVectorGeneratorImpl rvg = new RandomVectorGeneratorImpl(20);
    double[] vec = rvg.getVector();
    for (int i = 0; i < vec.length; ++i) {
      System.out.print(vec[i] + " ");
    }
    System.out.println();
    System.out.println(sp.getPrices());

    NormalDistribution nd = new NormalDistribution();
    double q = nd.inverseCumulativeProbability((0.997 + 1) / 2);
    System.out.println(q);


  }
}

