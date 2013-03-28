/* Author: Junyang Xin (jx372@nyu.edu)
 * Date: 3/13/2013
 */

package hw1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;

@RunWith(JUnit4.class)
public class PortfolioUnitTest {
  private PortfolioImpl portfolio;    
  private PositionImpl pos1;
  private PositionImpl pos2;
  private PositionImpl pos3;
  private PositionIter posIter;

  @Before
  public void setUp() {
    portfolio = new PortfolioImpl();
    pos1 = new PositionImpl("IBM", 100);
    pos2 = new PositionImpl("MSFT", -200);
    pos3 = new PositionImpl("GOOG", 700);
    posIter = portfolio.getPositionIter();
  }

  @Test
  public void testPositionImplConstructor() {
    assertTrue("IBM".equals(pos1.getSymbol()));
    assertEquals(100, pos1.getQuantity());
    assertTrue("MSFT".equals(pos2.getSymbol()));
    assertEquals(-200, pos2.getQuantity());
    assertTrue("GOOG".equals(pos3.getSymbol()));
    assertEquals(700, pos3.getQuantity());
  }

  @Test
  public void testPositionImplGetQuantity() {
    assertEquals(100, pos1.getQuantity());
    assertEquals(-200, pos2.getQuantity());
    assertEquals(700, pos3.getQuantity());
  }

  @Test
  public void testPositionImplGetSymbol() {
    assertTrue("IBM".equals(pos1.getSymbol()));
    assertTrue("MSFT".equals(pos2.getSymbol()));
    assertTrue("GOOG".equals(pos3.getSymbol()));
  }

  @Test
  public void testPositionImplSetQuantity() {
    pos1.setQuantity(200);
    assertEquals(200, pos1.getQuantity());
    pos2.setQuantity(9999);
    assertEquals(9999, pos2.getQuantity());
  }

  @Test
  public void testPositionImplToString() {
    assertTrue("Symbol:IBM Quantity:100".equals(pos1.toString()));
    assertTrue("Symbol:MSFT Quantity:-200".equals(pos2.toString()));
  }

  @Test
  public void testPortfolioImplNewTrade() {
    // A trade for a new position with quantity 0 should create no impact to
    // the portfolio.
    portfolio.newTrade("Foo", 0);
    HashMap<String, PositionImpl> map = portfolio.getMap();
    assertEquals(0, map.size());

    // Test for a trade for a new position with non-zero quantity.
    portfolio.newTrade("IBM", 100);
    assertEquals(1, map.size());
    assertEquals(100, map.get("IBM").getQuantity());
    assertTrue("IBM".equals(map.get("IBM").getSymbol()));

    // Test for more trades for new positions.
    portfolio.newTrade("MSFT", -200);
    portfolio.newTrade("GOOG", 700);
    assertEquals(3, map.size());

    // Test for a trade for an already existing position.
    portfolio.newTrade("IBM", 100);
    assertEquals(200, map.get("IBM").getQuantity());

    // Test for a trade that results in 0 quantity, thus causes the position
    // to be removed from the portfolio.
    portfolio.newTrade("IBM", -200);
    assertEquals(2, map.size());
    assertNull(map.get("IBM"));
    portfolio.newTrade("MSFT", 200);
    assertEquals(1, map.size());
    assertNull(map.get("MSFT"));
  } 

  @Test
  public void testPortfolioImplGetPositionIter() {
    // Test for empty iterator.
    assertNull(posIter.getNextPosition());

    // Test iterator's contents after populating trades.
    portfolio.newTrade("IBM", 100);
    portfolio.newTrade("MSFT", -200);
    portfolio.newTrade("GOOG", 700);
    posIter = portfolio.getPositionIter();
    for (int i = 0; i < 3; ++i) {
      assertNotNull(posIter.getNextPosition());
    }
    assertNull(posIter.getNextPosition());
  }

  @Test
  public void testPositionIterImplGetNextPosition() {
    HashMap<String, PositionImpl> map = portfolio.getMap();
    // Test for empty iterator.
    PositionIterImpl posIterImpl = new PositionIterImpl(map.values().iterator());
    assertNull(posIterImpl.getNextPosition());

    // Test for non-empty iterator.
    portfolio.newTrade("IBM", 100);
    posIterImpl = new PositionIterImpl(map.values().iterator());
    Position pos = posIterImpl.getNextPosition();
    assertTrue("IBM".equals(pos.getSymbol()));
    assertEquals(100, pos.getQuantity());
  }

  @Test
  public void testPositionIterImplResetIter() {
    HashMap<String, PositionImpl> map = portfolio.getMap();
    portfolio.newTrade("IBM", 100);
    PositionIterImpl posIterImpl = new PositionIterImpl(map.values().iterator());
    // Iterate through the only position in the portfolio, so
    // it reaches the end of the list of positions.
    posIterImpl.getNextPosition();
    assertNull(posIterImpl.getNextPosition());
    // Reset the iterator to the start of the position list and test it.
    posIterImpl.resetIter(map.values().iterator());
    Position pos = posIterImpl.getNextPosition();
    assertTrue("IBM".equals(pos.getSymbol()));
    assertEquals(100, pos.getQuantity());
  }
}

