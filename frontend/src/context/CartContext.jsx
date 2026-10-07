import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { FREE_SHIPPING_FROM, MAX_QUANTITY, SHIPPING_FEE } from '../config';

const CartContext = createContext(null);
const STORAGE_KEY = 'cart:v2';

const lineKey = (productId, size) => `${productId}|${size || ''}`;

function loadCart() {
  try {
    const items = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
    return Array.isArray(items) ? items.filter((item) => item?.productId && item.quantity > 0) : [];
  } catch {
    return [];
  }
}

function maxFor(item) {
  return Math.max(0, Math.min(MAX_QUANTITY, item.stock ?? MAX_QUANTITY));
}

export function CartProvider({ children }) {
  const [items, setItems] = useState(loadCart);

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
    } catch {
      // storage unavailable, cart lives in memory only
    }
  }, [items]);

  /** Adds a product to the bag. Returns false when the quantity limit or stock is already reached. */
  const addItem = useCallback(
    (product, size = null, quantity = 1) => {
      const key = lineKey(product.id, size);
      const existing = items.find((item) => lineKey(item.productId, item.size) === key);
      const limit = maxFor({ stock: product.stock });
      const current = existing?.quantity || 0;
      if (current >= limit) return false;

      const next = Math.min(limit, current + quantity);
      const line = {
        productId: product.id,
        name: product.name,
        brand: product.brand,
        imageUrl: product.imageUrl,
        price: Number(product.price),
        mrp: Number(product.mrp),
        stock: product.stock,
        size,
        quantity: next,
      };
      setItems((prev) =>
        existing
          ? prev.map((item) => (lineKey(item.productId, item.size) === key ? line : item))
          : [...prev, line],
      );
      return true;
    },
    [items],
  );

  const updateQuantity = useCallback((productId, size, quantity) => {
    setItems((prev) =>
      prev.map((item) =>
        lineKey(item.productId, item.size) === lineKey(productId, size)
          ? { ...item, quantity: Math.max(1, Math.min(maxFor(item), quantity)) }
          : item,
      ),
    );
  }, []);

  const removeItem = useCallback((productId, size) => {
    setItems((prev) => prev.filter((item) => lineKey(item.productId, item.size) !== lineKey(productId, size)));
  }, []);

  const clear = useCallback(() => setItems([]), []);

  const value = useMemo(() => {
    const count = items.reduce((sum, item) => sum + item.quantity, 0);
    const subtotal = items.reduce((sum, item) => sum + item.price * item.quantity, 0);
    const mrpTotal = items.reduce((sum, item) => sum + Math.max(item.mrp, item.price) * item.quantity, 0);
    const shipping = subtotal === 0 || subtotal >= FREE_SHIPPING_FROM ? 0 : SHIPPING_FEE;
    return {
      items,
      count,
      subtotal,
      mrpTotal,
      savings: mrpTotal - subtotal,
      shipping,
      total: subtotal + shipping,
      addItem,
      updateQuantity,
      removeItem,
      clear,
    };
  }, [items, addItem, updateQuantity, removeItem, clear]);

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export const useCart = () => useContext(CartContext);
