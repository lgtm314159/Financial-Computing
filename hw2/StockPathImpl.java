import org.apache.commons.math3.analysis.function.Exp;
import org.apache.commons.math3.analysis.function.Sqrt;
import org.joda.time.DateTime;
import java.util.List;
import java.util.LinkedList;

public class StockPathImpl implements StockPath {
  private double originPrice, volatility, r; 
  private RandomVectorGenerator rvg;
  private RandomVectorGenerator antiTheticRvg;
  private int days;
  private boolean isToAnti = false;

  public StockPathImpl(double originPrice, double volatility, double r,
      int days) {
    this.originPrice = originPrice;
    this.volatility = volatility;
    this.r = r;
    this.days = days;
    rvg = new RandomVectorGeneratorImpl(days);
    antiTheticRvg = new AntiTheticDecorator((RandomVectorGeneratorImpl) rvg);
  }

  List<Pair<DateTime, Double>> generatePrices(double[] randomVector) {
    LinkedList<Pair<DateTime, Double>> prices = 
        new LinkedList<Pair<DateTime, Double>>();
    DateTime day = DateTime.now();
    double price = originPrice;
    int t = 0;
    Exp exp = new Exp();
    Sqrt sqrt = new Sqrt();
    double exponent;
    double constant = r - volatility * volatility / 2;
    for (int i = 0; i < randomVector.length; ++i) {
      exponent = constant +
          volatility * randomVector[i];
      price = price * exp.value(exponent);
      day = day.plusDays(1);
      Pair<DateTime, Double> pair = new Pair<DateTime, Double>(day, price);
      prices.add(pair);
      ++t;
    }
    return prices;
  }

  public List<Pair<DateTime, Double>> getPrices() {
    if (!isToAnti) {
      isToAnti = true;
      return generatePrices(rvg.getVector());
    } else {
      isToAnti = false;
      return generatePrices(antiTheticRvg.getVector());
    }
  } 
}
