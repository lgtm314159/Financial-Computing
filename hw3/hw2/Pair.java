/* Author: Junyang Xin (jx372@nyu.edu)
 * Date: 3/29/2013
 */

package hw2;

/* Helper class to store information. */
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
