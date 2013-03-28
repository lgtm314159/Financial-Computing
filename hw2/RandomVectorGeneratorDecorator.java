abstract class RandomVectorGeneratorDecorator implements RandomVectorGenerator {
  protected RandomVectorGeneratorImpl decoratedRandVecGenerator;

  public RandomVectorGeneratorDecorator(RandomVectorGeneratorImpl rvg) {
    this.decoratedRandVecGenerator = rvg;
  }

  public double[] getVector() {
    return decoratedRandVecGenerator.getVector();
  }
}

