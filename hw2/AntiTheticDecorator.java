class AntiTheticDecorator implements RandomVectorGenerator {
  private RandomVectorGeneratorImpl rvg;

  public AntiTheticDecorator(RandomVectorGeneratorImpl rvg) {
    this.rvg = rvg;
  }

  @Override
  public double[] getVector() {
    double[] rdVec = rvg.getCurrentVector();
    for (int i = 0; i < rdVec.length; ++i) {
      rdVec[i] = -rdVec[i];
    }
    return rdVec;
  }
} 

