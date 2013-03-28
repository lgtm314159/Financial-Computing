import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.random.JDKRandomGenerator;

public class RandomVectorGeneratorImpl implements RandomVectorGenerator {
  private double[] rdVec = null;
  GaussianRandomGenerator gr;

  public RandomVectorGeneratorImpl(int size) throws IllegalArgumentException {
    if (size <= 0) {
      throw new IllegalArgumentException();
    } else {
      rdVec = new double[size];
      JDKRandomGenerator jdkRg= new JDKRandomGenerator();
      jdkRg.setSeed((int)System.currentTimeMillis());
      gr = new GaussianRandomGenerator(jdkRg);
    } 
  }

  double[] getCurrentVector() {
    return rdVec;
  }

  public double[] getVector() {
    for (int i = 0; i < rdVec.length; ++i) {
      rdVec[i] = gr.nextNormalizedDouble();
    }
    return rdVec;
  }

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

