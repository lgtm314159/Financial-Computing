/* Author: Junyang Xin (jx372@nyu.edu)
 * Date: 3/29/2013
 */

package hw2;

import java.util.List;
import java.util.LinkedList;
import org.apache.commons.math3.analysis.function.Max;
import org.joda.time.DateTime;

public class EuCallPayOutImpl implements PayOut {
  private double strikePrice;

  /*
   * Constructor.
   *
   * @param strikePrice, the strike price of a stock option.
   */
  public EuCallPayOutImpl(double strikePrice) {
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
    return max.value(0, prices.get(prices.size() - 1).getRight() - strikePrice);
  }

  /* This main method is only for testing purpose. */
  public static void main(String[] args) {
    EuCallPayOutImpl euPayOut = new EuCallPayOutImpl(165);
    StockPathImpl stockPath = new StockPathImpl(152.35, 0.01, 0.0001, 252);
    System.out.println(euPayOut.getPayout(stockPath));
  }
}

