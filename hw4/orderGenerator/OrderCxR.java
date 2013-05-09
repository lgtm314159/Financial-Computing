package orderGenerator;

/**
 * Created with IntelliJ IDEA.
 * User: eran
 * Date: 9/28/12
 * Time: 8:32 AM
 * To change this template use File | Settings | File Templates.
 */
public interface OrderCxR extends Message{

       /**
        * The size for this order. Note, CxR will not cause an order to change sides!
        * zero indicate cancel.
        * @return The size of the order. Negative for sell.
        */
       public int getSize();

       /**
        * @return The orderId for this CxR
        */
       public String getOrderId();

       /**
        * @return The limit price for the order. This will be populated.
        */
       public double getLimitPrice();
}
