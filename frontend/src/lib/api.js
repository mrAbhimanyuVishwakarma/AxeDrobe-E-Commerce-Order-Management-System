import axios from 'axios';
import { API, WARMUP_URLS } from '../config';

export const TOKEN_KEY = 'token';

export function getToken() {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
}

function createClient(baseURL) {
  // Long timeout because a sleeping free-tier service can take ~a minute to start
  const client = axios.create({ baseURL, timeout: 75000 });

  client.interceptors.request.use((config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });

  client.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401 && getToken()) {
        window.dispatchEvent(new Event('auth:expired'));
      }
      return Promise.reject(error);
    },
  );

  return client;
}

export const userApi = createClient(API.users);
export const productApi = createClient(API.products);
export const orderApi = createClient(API.orders);

/** Turns an axios error into a message that can be shown to the customer. */
export function errorMessage(error, fallback = 'Something went wrong. Please try again.') {
  const message = error?.response?.data?.message;
  if (message) return message;
  if (!error?.response) {
    return 'We could not reach our servers. They may be waking up, please try again in a moment.';
  }
  return fallback;
}

/** Fire-and-forget pings so sleeping services start booting as soon as someone opens the site. */
export function warmUpServices() {
  const urls = [API.users, API.products, API.orders].map((base) => `${base}/actuator/health`).concat(WARMUP_URLS);
  urls.forEach((url) => {
    fetch(url, { mode: 'no-cors' }).catch(() => {});
  });
}
