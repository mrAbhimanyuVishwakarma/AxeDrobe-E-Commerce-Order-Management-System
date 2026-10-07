// Central place for anything that changes between deployments or when rebranding the store.
export const BRAND_NAME = import.meta.env.VITE_BRAND_NAME || 'AxeDrobe';

export const API = {
  users: import.meta.env.VITE_USER_SERVICE_URL || 'http://localhost:8081',
  products: import.meta.env.VITE_PRODUCT_SERVICE_URL || 'http://localhost:8082',
  orders: import.meta.env.VITE_ORDER_SERVICE_URL || 'http://localhost:8083',
};

// Extra health URLs to ping on page load (e.g. the inventory and notification services).
// Services on free hosting sleep when idle; pinging them early means they're awake by checkout.
export const WARMUP_URLS = (import.meta.env.VITE_WARMUP_URLS || '')
  .split(',')
  .map((url) => url.trim())
  .filter(Boolean);

export const FREE_SHIPPING_FROM = 999;
export const SHIPPING_FEE = 79;
export const MAX_QUANTITY = 10;
