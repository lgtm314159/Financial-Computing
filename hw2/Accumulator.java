package hw2;

public class Accumulator {
  private double mean = 0;
  private double quadMean = 0;

  /*
   * Method to get the mean.
   *
   * @return the accumulated mean
   */
  public double getMean() {
    return mean;
  }

  /*
   * Method to get the quadratic mean.
   *
   * @return the accumulated quadratic mean
   */
  public double getQuadMean() {
    return quadMean;
  }

  /*
   * Method to accumulate the mean.
   */
  public void accumMean(double newVal, long n) {
    mean = (n - 1) * mean / n + newVal / n;
  } 

  /*
   * Method to accumulate the quadratic mean.
   */
  public void accumQuadMean(double newVal, long n) {
    quadMean = (n - 1) * quadMean / n + newVal / n;
  }
}

