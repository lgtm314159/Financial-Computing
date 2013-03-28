import java.util.List;
import java.util.LinkedList;
import org.apache.commons.math3.analysis.function.Max;
import org.joda.time.DateTime;

public class AsianCallPayOutImpl implements PayOut {
  private double strikePrice;

  public AsianCallPayOutImpl(double strikePrice) {
    this.strikePrice = strikePrice;
  }

  public double getPayout(StockPath path) {
    LinkedList<Pair<DateTime, Double>> prices =
        new LinkedList<Pair<DateTime, Double>>();
    List<Pair<DateTime, Double>> tmpPrices = path.getPrices();
    // Make defensive copy of the prices.
    for (Pair<DateTime, Double> price: tmpPrices) {
      prices.add(price);
    }
    Max max = new Max();
    double total = 0;
    for (Pair<DateTime, Double> price: prices) {
      total += price.getRight(); 
    }
    //System.out.println(total);
    //System.out.println(total / prices.size());
    return max.value(0, total / prices.size() - strikePrice);
  }
}

