public class Accumulator {
  private double mean = 0;
  private double quadMean = 0;

  public double getMean() {
    return mean;
  }

  public double getQuadMean() {
    return quadMean;
  }

  public void accumMean(double newVal, int n) {
    mean = (n - 1) * mean / n + newVal / n;
  } 

  public void accumQuadMean(double newVal, int n) {
    quadMean = (n - 1) * quadMean / n + newVal / n;
  }
}

