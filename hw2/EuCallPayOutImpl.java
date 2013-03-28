import java.util.List;
import java.util.LinkedList;
import org.apache.commons.math3.analysis.function.Max;
import org.joda.time.DateTime;

public class EuCallPayOutImpl implements PayOut {
  private double strikePrice;

  public EuCallPayOutImpl(double strikePrice) {
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
    return max.value(0, prices.get(prices.size() - 1).getRight() - strikePrice);
  }
}

