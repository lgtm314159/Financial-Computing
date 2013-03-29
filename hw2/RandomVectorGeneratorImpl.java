/* Author: Junyang Xin (jx372@nyu.edu)
 * Date: 3/29/2013
 */

package hw2;

import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.random.JDKRandomGenerator;

public class RandomVectorGeneratorImpl implements RandomVectorGenerator {
  private double[] rdVec = null;
  GaussianRandomGenerator gr;

  /*
   * Constructor with vector size intializatoin.
   *
   * @param size, the size of the random vector
   */
  public RandomVectorGeneratorImpl(int size) { 
    if (size <= 0) {
      System.err.println("Invalid vector size!");
      System.exit(-1);
    } else {
      rdVec = new double[size];
      JDKRandomGenerator jdkRg= new JDKRandomGenerator();
      jdkRg.setSeed((int)System.currentTimeMillis());
      gr = new GaussianRandomGenerator(jdkRg);
    } 
  }

  /*
   * Method to get current random vector. This for decoration purpose.
   *
   * @return a double array of random numbers representing a random vector
   */
  double[] getCurrentVector() {
    return rdVec;
  }

  /*
   * Method to generate a random vector. This for decoration purpose.
   *
   * @return a double array of random numbers representing a random vector
   */
  public double[] getVector() {
    for (int i = 0; i < rdVec.length; ++i) {
      rdVec[i] = gr.nextNormalizedDouble();
    }
    return rdVec;
  }

  /* The main function is for testing the random vector genration. */
  public static void main(String[] args) {
    RandomVectorGenerator rd = new RandomVectorGeneratorImpl(252);
    if (rd instanceof RandomVectorGeneratorImpl) {
      AntiTheticDecorator anti = new AntiTheticDecorator(
          (RandomVectorGeneratorImpl)rd);
      double[] rdVec = rd.getVector(); 
      for (int i = 0; i < rdVec.length; ++i) {
        System.out.print(rdVec[i] + " ");
      }
      System.out.println();
      rdVec = anti.getVector(); 
      for (int i = 0; i < rdVec.length; ++i) {
        System.out.print(rdVec[i] + " ");
      }
    }
  }
}

