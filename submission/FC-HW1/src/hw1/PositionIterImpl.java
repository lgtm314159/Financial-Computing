/* Author: Junyang Xin (jx372@nyu.edu)
 * Date: 3/13/2013
 */

package hw1;

import java.util.Iterator;

public class PositionIterImpl implements PositionIter {
  private Iterator<PositionImpl> posIter = null;

  /**
   * Constructor for this class.
   *
   * @param posIter, the iterator of a collection of positions.
   */
  public PositionIterImpl(Iterator<PositionImpl> posIter) {
    this.posIter = posIter;
  }

  /**
   * Method to get the next position of this iterator. This simply utilizes
   * an iterator of a collection of PositionImpl instances and illustrates the
   * the adapter pattern.
   *
   * @return the next position, or null if this iterator has no more position
   */
  public Position getNextPosition() {
    if (posIter.hasNext()) {
      return posIter.next();
    } else {
      return null;
    }
  }

  /**
   * Package-private Method to reset the iterator.
   *
   * @param posIter, the iterator of the collection of positions
   */
  void resetIter(Iterator<PositionImpl> posIter) {
    this.posIter = posIter;
  }
}

