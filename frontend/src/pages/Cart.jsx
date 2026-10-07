import { useContext, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Minus, Plus, Trash2 } from 'lucide-react';
import { AuthContext } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { useToast } from '../context/ToastContext';
import { errorMessage, orderApi } from '../lib/api';
import { fetchCatalog } from '../lib/catalog';
import { formatPrice, sizedImage } from '../lib/format';
import { INDIAN_STATES } from '../lib/india';
import { FREE_SHIPPING_FROM, MAX_QUANTITY } from '../config';
import './Cart.css';

const ADDRESS_KEY = 'address:last';
const EMPTY_ADDRESS = { fullName: '', phone: '', line1: '', line2: '', city: '', state: '', pincode: '' };

function loadAddress(user) {
  try {
    const saved = JSON.parse(localStorage.getItem(ADDRESS_KEY) || 'null');
    if (saved) return { ...EMPTY_ADDRESS, ...saved };
  } catch {
    // ignore unreadable saved address
  }
  return { ...EMPTY_ADDRESS, fullName: user?.name || '', phone: user?.mobile || '' };
}

const Cart = () => {
  const { user } = useContext(AuthContext);
  const cart = useCart();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const [address, setAddress] = useState(() => loadAddress(user));
  const [placing, setPlacing] = useState(false);
  const [error, setError] = useState('');

  const setField = (e) => setAddress({ ...address, [e.target.name]: e.target.value });

  const placeOrder = async (e) => {
    e.preventDefault();
    setError('');
    setPlacing(true);
    try {
      const { data: order } = await orderApi.post('/api/orders', {
        items: cart.items.map((item) => ({ productId: item.productId, size: item.size, quantity: item.quantity })),
        shippingAddress: {
          ...address,
          phone: address.phone.replace(/\D/g, '').slice(-10),
          line2: address.line2 || null,
        },
      });
      try {
        localStorage.setItem(ADDRESS_KEY, JSON.stringify(address));
      } catch {
        // not critical
      }
      cart.clear();
      fetchCatalog({ refresh: true }).catch(() => {});
      showToast(`Order ${order.orderNumber} placed!`, 'success');
      navigate(`/orders?placed=${encodeURIComponent(order.orderNumber)}`);
    } catch (err) {
      if (err.response?.status === 401) {
        navigate('/auth', { state: { from: '/cart' } });
        return;
      }
      setError(errorMessage(err, 'Could not place your order. Please try again.'));
    } finally {
      setPlacing(false);
    }
  };

  if (cart.items.length === 0) {
    return (
      <div className="cart-page container">
        <h2 className="page-title">Your Bag</h2>
        <div className="empty-cart glass-panel">
          <p>Your bag is empty. Let&apos;s fix that.</p>
          <Link className="btn btn-primary" to="/category/best-sellers">Shop best sellers</Link>
        </div>
      </div>
    );
  }

  const toFreeShipping = FREE_SHIPPING_FROM - cart.subtotal;

  return (
    <div className="cart-page container">
      <h2 className="page-title">Your Bag <span className="page-title-count">({cart.count} {cart.count === 1 ? 'item' : 'items'})</span></h2>

      <div className="cart-content">
        <div className="cart-main">
          {toFreeShipping > 0 && (
            <div className="shipping-banner">
              Add items worth {formatPrice(toFreeShipping)} more to get <strong>free delivery</strong>.
            </div>
          )}

          <div className="cart-items">
            {cart.items.map((item) => (
              <div key={`${item.productId}|${item.size || ''}`} className="cart-item glass-panel">
                <Link to={`/product/${item.productId}`} className="cart-item-image">
                  <img src={sizedImage(item.imageUrl, 240)} alt={item.name} />
                </Link>
                <div className="item-info">
                  <span className="item-brand">{item.brand}</span>
                  <Link to={`/product/${item.productId}`} className="item-name">{item.name}</Link>
                  {item.size && <span className="item-size">Size: {item.size}</span>}
                  <div className="item-price-row">
                    <span className="item-price">{formatPrice(item.price * item.quantity)}</span>
                    {item.mrp > item.price && <span className="original-price">{formatPrice(item.mrp * item.quantity)}</span>}
                  </div>
                  <div className="item-controls">
                    <div className="qty-stepper" aria-label={`Quantity for ${item.name}`}>
                      <button
                        type="button"
                        onClick={() => cart.updateQuantity(item.productId, item.size, item.quantity - 1)}
                        disabled={item.quantity <= 1}
                        aria-label="Decrease quantity"
                      >
                        <Minus size={14} />
                      </button>
                      <span>{item.quantity}</span>
                      <button
                        type="button"
                        onClick={() => cart.updateQuantity(item.productId, item.size, item.quantity + 1)}
                        disabled={item.quantity >= Math.min(MAX_QUANTITY, item.stock ?? MAX_QUANTITY)}
                        aria-label="Increase quantity"
                      >
                        <Plus size={14} />
                      </button>
                    </div>
                    <button type="button" className="remove-btn" onClick={() => cart.removeItem(item.productId, item.size)}>
                      <Trash2 size={14} /> Remove
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {user ? (
            <form id="checkout-form" className="address-form glass-panel" onSubmit={placeOrder}>
              <h3>Delivery address</h3>
              <div className="form-grid">
                <div className="input-group">
                  <label htmlFor="fullName">Full name</label>
                  <input id="fullName" name="fullName" value={address.fullName} onChange={setField} required maxLength={80} autoComplete="name" />
                </div>
                <div className="input-group">
                  <label htmlFor="phone">Mobile number</label>
                  <input id="phone" name="phone" value={address.phone} onChange={setField} required inputMode="numeric" maxLength={10} pattern="[6-9][0-9]{9}" title="10-digit mobile number" placeholder="10-digit mobile number" autoComplete="tel-national" />
                </div>
                <div className="input-group span-2">
                  <label htmlFor="line1">House no., building, street</label>
                  <input id="line1" name="line1" value={address.line1} onChange={setField} required maxLength={200} autoComplete="address-line1" />
                </div>
                <div className="input-group span-2">
                  <label htmlFor="line2">Area, landmark (optional)</label>
                  <input id="line2" name="line2" value={address.line2} onChange={setField} maxLength={200} autoComplete="address-line2" />
                </div>
                <div className="input-group">
                  <label htmlFor="city">City</label>
                  <input id="city" name="city" value={address.city} onChange={setField} required maxLength={60} autoComplete="address-level2" />
                </div>
                <div className="input-group">
                  <label htmlFor="pincode">PIN code</label>
                  <input id="pincode" name="pincode" value={address.pincode} onChange={setField} required inputMode="numeric" pattern="[1-9][0-9]{5}" title="6-digit PIN code" autoComplete="postal-code" />
                </div>
                <div className="input-group span-2">
                  <label htmlFor="state">State</label>
                  <select id="state" name="state" value={address.state} onChange={setField} required>
                    <option value="">Select state</option>
                    {INDIAN_STATES.map((state) => (
                      <option key={state} value={state}>{state}</option>
                    ))}
                  </select>
                </div>
              </div>
              <p className="payment-note">Payment: <strong>Cash on Delivery</strong>. Pay by cash or UPI when your order arrives.</p>
            </form>
          ) : (
            <div className="checkout-signin glass-panel">
              <p>Sign in to add a delivery address and place your order.</p>
              <button className="btn btn-primary" onClick={() => navigate('/auth', { state: { from: '/cart' } })}>
                Sign in to checkout
              </button>
            </div>
          )}
        </div>

        <aside className="cart-summary glass-panel">
          <h3>Price details</h3>
          <div className="summary-row">
            <span>Total MRP</span>
            <span>{formatPrice(cart.mrpTotal)}</span>
          </div>
          {cart.savings > 0 && (
            <div className="summary-row savings">
              <span>Discount on MRP</span>
              <span>-{formatPrice(cart.savings)}</span>
            </div>
          )}
          <div className="summary-row">
            <span>Delivery fee</span>
            <span>{cart.shipping === 0 ? <span className="free">FREE</span> : formatPrice(cart.shipping)}</span>
          </div>
          <hr className="summary-divider" />
          <div className="summary-row total">
            <span>Total amount</span>
            <span>{formatPrice(cart.total)}</span>
          </div>
          {cart.savings > 0 && <p className="savings-note">You save {formatPrice(cart.savings)} on this order</p>}

          {error && <div className="error-alert">{error}</div>}

          {user ? (
            <button type="submit" form="checkout-form" className="btn btn-primary checkout-btn" disabled={placing}>
              {placing ? 'Placing order...' : `Place order · ${formatPrice(cart.total)}`}
            </button>
          ) : (
            <button className="btn btn-primary checkout-btn" onClick={() => navigate('/auth', { state: { from: '/cart' } })}>
              Sign in to checkout
            </button>
          )}
        </aside>
      </div>
    </div>
  );
};

export default Cart;
