/* Author: Junyang Xin (jx372@nyu.edu)
 * Date: 3/29/2013
 */

package hw2;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AntiTheticDecoratorUnitTest {

  @Test
  public void testGetVector() {
    RandomVectorGeneratorImpl rvg = new RandomVectorGeneratorImpl(252);
    AntiTheticDecorator anti = new AntiTheticDecorator(rvg);
    double[] vector = rvg.getVector();
    double[] antiVector = anti.getVector();
    assertEquals(antiVector.length, 252);
    assertEquals(vector.length, antiVector.length);
    for (int i = 0; i < vector.length; ++i) {
      assertEquals(vector[i], -antiVector[i], 0.000001);
    }
  } 
}

