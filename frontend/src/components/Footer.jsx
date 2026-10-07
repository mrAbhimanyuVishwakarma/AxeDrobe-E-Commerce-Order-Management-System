import { Link } from 'react-router-dom';
import { BRAND_NAME, FREE_SHIPPING_FROM } from '../config';
import { formatPrice } from '../lib/format';
import './Footer.css';

const Footer = () => {
  return (
    <footer className="footer-container">
      <div className="footer-content container">
        <div className="footer-links-section">
          <div className="footer-column">
            <h4>ONLINE SHOPPING</h4>
            <ul>
              <li><Link to="/category/men">Men</Link></li>
              <li><Link to="/category/women">Women</Link></li>
              <li><Link to="/category/ethnic">Ethnic Wear</Link></li>
              <li><Link to="/category/footwear">Footwear</Link></li>
              <li><Link to="/category/accessories">Accessories</Link></li>
              <li><Link to="/category/new-arrivals">New Arrivals</Link></li>
              <li><Link to="/category/sale">Sale</Link></li>
            </ul>
          </div>

          <div className="footer-column">
            <h4>CUSTOMER POLICIES & INFO</h4>
            <ul>
              <li><a href="/about-us.html">About Us</a></li>
              <li><Link to="/contact">Contact Us</Link></li>
              <li><a href="/privacy-policy.html">Privacy Policy</a></li>
              <li><Link to="/terms">Terms & Conditions</Link></li>
              <li><Link to="/faq">FAQ</Link></li>
              <li><Link to="/track-orders">Track Orders</Link></li>
              <li><Link to="/shipping">Shipping</Link></li>
              <li><Link to="/cancellation">Cancellation & Returns</Link></li>
            </ul>
          </div>
        </div>

        <div className="footer-trust-section">
          <div className="trust-item">
            <div className="trust-icon" aria-hidden="true">💯</div>
            <p><strong>100% ORIGINAL</strong> guarantee for every product on {BRAND_NAME}</p>
          </div>
          <div className="trust-item">
            <div className="trust-icon" aria-hidden="true">🚚</div>
            <p><strong>Free delivery</strong> on orders above {formatPrice(FREE_SHIPPING_FROM)}</p>
          </div>
          <div className="trust-item">
            <div className="trust-icon" aria-hidden="true">🔄</div>
            <p><strong>Return within 14 days</strong> of receiving your order</p>
          </div>
        </div>
      </div>

      <div className="footer-bottom">
        <p>&copy; {new Date().getFullYear()} {BRAND_NAME}. All rights reserved.</p>
      </div>
    </footer>
  );
};

export default Footer;
