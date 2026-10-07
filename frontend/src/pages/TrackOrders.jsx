import { useContext, useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import OrderTimeline from '../components/OrderTimeline';
import { AuthContext } from '../context/AuthContext';
import { errorMessage, orderApi } from '../lib/api';
import { formatDate, formatPrice } from '../lib/format';
import { STATUS_LABELS, estimatedDelivery } from '../lib/orders';
import './Policies.css';

const TrackOrders = () => {
  const { user } = useContext(AuthContext);
  const [searchParams, setSearchParams] = useSearchParams();
  const initial = searchParams.get('order') || '';

  const [orderNumber, setOrderNumber] = useState(initial);
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(Boolean(initial && user));
  const [error, setError] = useState('');

  const [lookupKey, setLookupKey] = useState(0);

  useEffect(() => {
    if (!initial || !user) return undefined;
    let active = true;
    orderApi
      .get(`/api/orders/${encodeURIComponent(initial)}`)
      .then(({ data }) => {
        if (!active) return;
        setOrder(data);
        setError('');
      })
      .catch((err) => {
        if (!active) return;
        setOrder(null);
        setError(err.response?.status === 404
          ? 'We could not find that order on your account. Check the number and try again.'
          : errorMessage(err, 'Could not load the order.'));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [initial, user, lookupKey]);

  const handleSubmit = (e) => {
    e.preventDefault();
    const reference = orderNumber.trim().toUpperCase();
    if (!reference) return;
    setLoading(true);
    if (reference === initial) {
      setLookupKey((key) => key + 1);
    } else {
      setSearchParams({ order: reference });
    }
  };

  return (
    <div className="policy-page">
      <h1>Track Your Order</h1>

      {!user ? (
        <div className="track-form" style={{ textAlign: 'center' }}>
          <p style={{ marginBottom: '20px' }}>Sign in to track your orders.</p>
          <Link to="/auth" state={{ from: `/track-orders${initial ? `?order=${initial}` : ''}` }} className="btn btn-primary">
            Sign in
          </Link>
        </div>
      ) : (
        <>
          <form onSubmit={handleSubmit} className="track-form">
            <div className="input-group">
              <label htmlFor="orderNumber">Order number</label>
              <input
                id="orderNumber"
                value={orderNumber}
                onChange={(e) => setOrderNumber(e.target.value)}
                placeholder="e.g. AXD261006-7KQ2M"
                autoComplete="off"
                required
              />
            </div>
            <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={loading}>
              {loading ? 'Looking up...' : 'Track order'}
            </button>
            <p className="track-hint">
              Your order number is in your confirmation email and on the <Link to="/orders">My Orders</Link> page.
            </p>
          </form>

          {error && <div className="error-alert" style={{ maxWidth: 500, margin: '0 auto 30px' }}>{error}</div>}

          {order && (
            <div className="track-results">
              <div className="track-summary">
                <h3>{order.orderNumber}</h3>
                <p>
                  {STATUS_LABELS[order.status]} · {order.items.length} {order.items.length === 1 ? 'item' : 'items'} ·{' '}
                  {formatPrice(order.total)}
                </p>
                {order.status !== 'CANCELLED' && order.status !== 'DELIVERED' && (
                  <p>Estimated delivery: <strong>{formatDate(estimatedDelivery(order))}</strong></p>
                )}
              </div>
              <OrderTimeline order={order} />
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default TrackOrders;
