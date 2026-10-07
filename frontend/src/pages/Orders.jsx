import { useContext, useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { CheckCircle } from 'lucide-react';
import { AuthContext } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { errorMessage, orderApi } from '../lib/api';
import { fetchCatalog } from '../lib/catalog';
import { formatDate, formatPrice, sizedImage } from '../lib/format';
import { STATUS_LABELS, canCancel, estimatedDelivery } from '../lib/orders';
import './Orders.css';

const Orders = () => {
  const { user } = useContext(AuthContext);
  const { showToast } = useToast();
  const [searchParams] = useSearchParams();
  const placedNumber = searchParams.get('placed');

  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(Boolean(user));
  const [error, setError] = useState(null);
  const [cancelling, setCancelling] = useState(null);

  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    if (!user) return undefined;
    let active = true;
    orderApi
      .get('/api/orders')
      .then((response) => {
        if (!active) return;
        setOrders(response.data);
        setError(null);
      })
      .catch((err) => {
        if (active) setError(errorMessage(err, 'Could not load your orders.'));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [user, reloadKey]);

  const retry = () => {
    setLoading(true);
    setReloadKey((key) => key + 1);
  };

  const cancelOrder = async (order) => {
    if (!window.confirm(`Cancel order ${order.orderNumber}?`)) return;
    setCancelling(order.id);
    try {
      const { data } = await orderApi.post(`/api/orders/${order.id}/cancel`);
      setOrders((prev) => prev.map((o) => (o.id === data.id ? data : o)));
      fetchCatalog({ refresh: true }).catch(() => {});
      showToast('Order cancelled', 'success');
    } catch (err) {
      showToast(errorMessage(err, 'Could not cancel the order.'), 'error');
    } finally {
      setCancelling(null);
    }
  };

  if (!user) {
    return (
      <div className="orders-page container orders-empty">
        <h2>My Orders</h2>
        <p>Sign in to see and track your orders.</p>
        <Link to="/auth" state={{ from: '/orders' }} className="btn btn-primary">Sign in</Link>
      </div>
    );
  }

  return (
    <div className="orders-page container">
      <h2 className="page-title">My Orders</h2>

      {placedNumber && (
        <div className="order-success">
          <CheckCircle size={22} aria-hidden="true" />
          <div>
            <strong>Thank you! Your order {placedNumber} has been placed.</strong>
            <span>
              {user.email ? `A confirmation is on its way to ${user.email}. ` : ''}Pay with cash or UPI when it arrives.
            </span>
          </div>
        </div>
      )}

      {loading ? (
        <div className="loading-spinner">Loading your orders...</div>
      ) : error ? (
        <div className="empty-state">
          <h3>Something went wrong</h3>
          <p>{error}</p>
          <button className="btn btn-primary" onClick={retry}>Try again</button>
        </div>
      ) : orders.length === 0 ? (
        <div className="empty-state">
          <h3>No orders yet</h3>
          <p>When you place an order, it will show up here.</p>
          <Link to="/products" className="btn btn-primary">Start shopping</Link>
        </div>
      ) : (
        <div className="orders-list">
          {orders.map((order) => (
            <article key={order.id} className="order-card">
              <header className="order-card-header">
                <div>
                  <span className="order-number">{order.orderNumber}</span>
                  <span className="order-date">Placed on {formatDate(order.createdAt)}</span>
                </div>
                <span className={`status-badge status-${order.status.toLowerCase()}`}>{STATUS_LABELS[order.status]}</span>
              </header>

              <ul className="order-items">
                {order.items.map((item) => (
                  <li key={`${item.productId}|${item.size || ''}`}>
                    <Link to={`/product/${item.productId}`} className="order-item-image">
                      <img src={sizedImage(item.imageUrl, 160)} alt={item.name} loading="lazy" />
                    </Link>
                    <div className="order-item-info">
                      <span className="order-item-brand">{item.brand}</span>
                      <span>{item.name}</span>
                      <span className="order-item-meta">
                        {item.size ? `Size ${item.size} · ` : ''}Qty {item.quantity} · {formatPrice(item.unitPrice)}
                      </span>
                    </div>
                  </li>
                ))}
              </ul>

              <footer className="order-card-footer">
                <div className="order-meta">
                  <span>Total <strong>{formatPrice(order.total)}</strong> · Cash on Delivery</span>
                  {order.status !== 'CANCELLED' && order.status !== 'DELIVERED' && (
                    <span>Expected by {formatDate(estimatedDelivery(order))}</span>
                  )}
                  {order.shippingAddress && (
                    <span className="order-address">
                      Deliver to {order.shippingAddress.fullName}, {order.shippingAddress.city} {order.shippingAddress.pincode}
                    </span>
                  )}
                </div>
                <div className="order-actions">
                  <Link to={`/track-orders?order=${encodeURIComponent(order.orderNumber)}`} className="btn btn-secondary">
                    Track
                  </Link>
                  {canCancel(order) && (
                    <button className="btn btn-secondary danger" onClick={() => cancelOrder(order)} disabled={cancelling === order.id}>
                      {cancelling === order.id ? 'Cancelling...' : 'Cancel order'}
                    </button>
                  )}
                </div>
              </footer>
            </article>
          ))}
        </div>
      )}
    </div>
  );
};

export default Orders;
