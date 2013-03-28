public class Pair<T, U> {
  T left;
  U right;

  public Pair(T left, U right) {
    this.left = left;
    this.right = right;
  }

  public T getLeft() {
    return left;
  }

  public U getRight() {
    return right;
  }

  @Override
  public String toString() {
    return left.toString() + " " + right.toString();
  }
}