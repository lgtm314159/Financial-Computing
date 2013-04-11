/* Author: Junyang Xin (jx372@nyu.edu)
 * Date: 3/29/2013
 */

package hw2;

class AntiTheticDecorator implements RandomVectorGenerator {
  private RandomVectorGeneratorImpl rvg;

  /*
   * Constructor.
   *
   * @param rvg, a RandomVectorGeneratorImpl object passed to use as a pointer
   */
  public AntiTheticDecorator(RandomVectorGeneratorImpl rvg) {
    this.rvg = rvg;
  }

  @Override
  public double[] getVector() {
    double[] rdVec = rvg.getCurrentVector();
    double[] antiRdVec = new double[rdVec.length];
    // Decorate the vector into its anti-thetic counter part.
    for (int i = 0; i < rdVec.length; ++i) {
      antiRdVec[i] = -rdVec[i];
    }
    return antiRdVec;
  }
} 

