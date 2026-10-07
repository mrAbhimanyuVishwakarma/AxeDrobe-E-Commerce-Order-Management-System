import { Link } from 'react-router-dom';
import './Policies.css';

const Cancellation = () => {
  return (
    <div className="policy-page">
      <h1>Cancellation & Returns</h1>

      <h2>Cancelling an order</h2>
      <p>
        You can cancel any order that has not shipped yet. Open <Link to="/orders">My Orders</Link>, find the order
        and choose <strong>Cancel order</strong>. The cancellation is instant and you will receive a confirmation
        email. Once an order has shipped it can no longer be cancelled, but you can return it after delivery.
      </p>

      <h2>Return policy</h2>
      <p>
        We accept returns within 14 days of delivery for items that are unused, unwashed and have their original tags.
        Innerwear, socks and items marked as non-returnable cannot be returned for hygiene reasons.
      </p>

      <h3>How to return an item</h3>
      <ol>
        <li>Note your order number from <Link to="/orders">My Orders</Link> (it starts with AXD).</li>
        <li>Contact us through the <Link to="/contact">Help & Contact</Link> page with the order number and the item you want to return.</li>
        <li>Pack the item with its tags; our courier partner will pick it up within 2 to 3 business days.</li>
      </ol>

      <h2>Refunds</h2>
      <p>
        Once the returned item passes a quality check, we refund the amount to your bank account or UPI ID within
        5 to 7 business days. Delivery charges, if any, are not refunded.
      </p>
    </div>
  );
};

export default Cancellation;
