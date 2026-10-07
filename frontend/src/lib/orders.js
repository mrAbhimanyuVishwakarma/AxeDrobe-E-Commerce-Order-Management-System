export const STATUS_LABELS = {
  CONFIRMED: 'Confirmed',
  SHIPPED: 'Shipped',
  OUT_FOR_DELIVERY: 'Out for delivery',
  DELIVERED: 'Delivered',
  CANCELLED: 'Cancelled',
};

export const DELIVERY_STEPS = ['CONFIRMED', 'SHIPPED', 'OUT_FOR_DELIVERY', 'DELIVERED'];

export const canCancel = (order) => order.status === 'CONFIRMED';

/** Rough delivery estimate shown to customers: five days after the order was placed. */
export function estimatedDelivery(order) {
  const date = new Date(order.createdAt);
  date.setDate(date.getDate() + 5);
  return date;
}
