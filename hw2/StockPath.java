package hw2;

import java.util.List;
import org.joda.time.DateTime;

// The interface for creating StockPath. The returned list should be ordered by date
public interface StockPath {
  public List<Pair<DateTime, Double>> getPrices();
}

