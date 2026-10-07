import { BRAND_NAME, FREE_SHIPPING_FROM, SHIPPING_FEE } from '../config';
import { formatPrice } from '../lib/format';
import './Policies.css';

const Shipping = () => {
  return (
    <div className="policy-page">
      <h1>Shipping Policy</h1>

      <h2>Where we deliver</h2>
      <p>We currently deliver to all serviceable PIN codes across India.</p>

      <h2>Delivery charges and timelines</h2>
      <ul>
        <li><strong>Free delivery</strong> on orders of {formatPrice(FREE_SHIPPING_FROM)} or more.</li>
        <li><strong>Orders below {formatPrice(FREE_SHIPPING_FROM)}:</strong> flat {formatPrice(SHIPPING_FEE)} delivery fee.</li>
        <li><strong>Metro cities:</strong> usually 2 to 4 business days.</li>
        <li><strong>Rest of India:</strong> usually 4 to 7 business days.</li>
      </ul>

      <h2>Order processing</h2>
      <p>
        Orders are packed within 1 to 2 business days (excluding Sundays and public holidays). You will receive an
        email when your order is confirmed, and you can follow every step from the My Orders and Track Order pages.
      </p>

      <h2>Cash on Delivery</h2>
      <p>
        All {BRAND_NAME} orders are Cash on Delivery. Please keep the exact amount ready, or pay the delivery partner
        by UPI where available.
      </p>
    </div>
  );
};

export default Shipping;
