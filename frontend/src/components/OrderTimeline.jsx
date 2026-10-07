import { CheckCircle, Clock, Package, Truck, XCircle } from 'lucide-react';
import { DELIVERY_STEPS, STATUS_LABELS } from '../lib/orders';
import { formatDateTime } from '../lib/format';

const ICONS = {
  CONFIRMED: Package,
  SHIPPED: Truck,
  OUT_FOR_DELIVERY: Clock,
  DELIVERED: CheckCircle,
  CANCELLED: XCircle,
};

const OrderTimeline = ({ order }) => {
  const reachedAt = Object.fromEntries((order.statusHistory || []).map((change) => [change.status, change.at]));
  const steps = order.status === 'CANCELLED' ? ['CONFIRMED', 'CANCELLED'] : DELIVERY_STEPS;

  return (
    <div className="track-timeline">
      {steps.map((status) => {
        const Icon = ICONS[status];
        const at = reachedAt[status];
        return (
          <div key={status} className={`timeline-item ${at ? 'active' : ''} ${status === 'CANCELLED' ? 'cancelled' : ''}`}>
            <div className="timeline-icon">
              <Icon size={14} aria-hidden="true" />
            </div>
            <div className="timeline-content">
              <h4>{STATUS_LABELS[status]}</h4>
              <p>{at ? formatDateTime(at) : 'Pending'}</p>
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default OrderTimeline;
