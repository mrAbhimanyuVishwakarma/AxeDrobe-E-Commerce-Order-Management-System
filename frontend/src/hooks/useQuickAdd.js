import { useCallback } from 'react';
import { useCart } from '../context/CartContext';
import { useToast } from '../context/ToastContext';

/** "Add to bag" straight from a product card, for products without sizes. */
export function useQuickAdd() {
  const { addItem } = useCart();
  const { showToast } = useToast();

  return useCallback(
    (product) => {
      if (addItem(product, null, 1)) {
        showToast(`${product.name} added to your bag`, 'success');
      } else {
        showToast('You already have the maximum quantity of this item in your bag', 'error');
      }
    },
    [addItem, showToast],
  );
}
