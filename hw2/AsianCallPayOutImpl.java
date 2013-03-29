/* Author: Junyang Xin (jx372@nyu.edu)
 * Date: 3/29/2013
 */

package hw2;

import java.util.List;
import java.util.LinkedList;
import org.apache.commons.math3.analysis.function.Max;
import org.joda.time.DateTime;

public class AsianCallPayOutImpl implements PayOut {
  private double strikePrice;

  /*
   * Constructor.
   *
   * @param strikePrice, the strike price of a stock option.
   */
  public AsianCallPayOutImpl(double strikePrice) {
    this.strikePrice = strikePrice;
  }

  /*
   * Method to calculate payout.
   *
   * @param path, a StockPath instance for generating stock paths.
   */
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
    return max.value(0, total / prices.size() - strikePrice);
  }

  /* This main method is only for testing purpose. */
  public static void main(String[] args) {
    AsianCallPayOutImpl asianPayOut = new AsianCallPayOutImpl(164);
    StockPathImpl stockPath = new StockPathImpl(152.35, 0.01, 0.0001, 252);
    System.out.println(asianPayOut.getPayout(stockPath));
  }
}

