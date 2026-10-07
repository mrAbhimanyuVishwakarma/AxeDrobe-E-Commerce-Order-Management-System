import { useEffect, useState } from 'react';
import { errorMessage } from '../lib/api';
import { fetchCatalog } from '../lib/catalog';

export function useCatalog() {
  const [state, setState] = useState({ products: [], loading: true, error: null });

  useEffect(() => {
    let active = true;
    fetchCatalog()
      .then((products) => {
        if (active) setState({ products, loading: false, error: null });
      })
      .catch((error) => {
        if (active) setState({ products: [], loading: false, error: errorMessage(error, 'Could not load products.') });
      });
    return () => {
      active = false;
    };
  }, []);

  return state;
}
