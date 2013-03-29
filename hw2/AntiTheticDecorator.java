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
    // Decorate the vector into its anti-thetic counter part.
    for (int i = 0; i < rdVec.length; ++i) {
      rdVec[i] = -rdVec[i];
    }
    return rdVec;
  }
} 

