package orderGenerator;

/**
 * Created with IntelliJ IDEA.
 * User: eran
 * Date: 9/28/12
 * Time: 8:30 AM
 * To change this template use File | Settings | File Templates.
 */
public interface NewOrder extends Message{

    /**
     * @return The symbol for this new order.
     */
    public String getSymbol();

    /**
     * @return The size of the order. Negative for sell
     */
    public int getSize();

    /**
     * @return The orderId for this new order
     */
    public String getOrderId();

    /**
     * @return The limit price for the order. NaN for market Order.
     */
    public double getLimitPrice();
}
