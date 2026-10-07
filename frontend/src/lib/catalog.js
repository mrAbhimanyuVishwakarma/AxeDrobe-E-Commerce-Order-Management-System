import { productApi } from './api';
import { discountPercent } from './format';

export const FOOTWEAR = ['Sports Shoes', 'Sneakers', 'Boots', 'Loafers', 'Heels', 'Flats'];
export const ACCESSORIES = ['Watches', 'Sunglasses', 'Bags & Backpacks', 'Handbags', 'Wallets', 'Caps'];
export const ETHNIC = ['Kurtas', 'Kurtas & Kurtis', 'Sarees'];

export const GENDER_LABELS = { MEN: 'Men', WOMEN: 'Women', UNISEX: 'Unisex' };

export function slugify(text) {
  return text
    .toLowerCase()
    .replace(/&/g, 'and')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-|-$/g, '');
}

const byNewest = (a, b) => new Date(b.createdAt) - new Date(a.createdAt);
const byPopularity = (a, b) => b.ratingCount - a.ratingCount;

const COLLECTIONS = {
  men: { title: 'Men', filter: (p) => p.gender === 'MEN' || p.gender === 'UNISEX' },
  women: { title: 'Women', filter: (p) => p.gender === 'WOMEN' || p.gender === 'UNISEX' },
  ethnic: { title: 'Ethnic Wear', filter: (p) => ETHNIC.includes(p.category) },
  footwear: { title: 'Footwear', filter: (p) => FOOTWEAR.includes(p.category) },
  accessories: { title: 'Accessories', filter: (p) => ACCESSORIES.includes(p.category) },
  sale: { title: 'Sale', filter: (p) => discountPercent(p.price, p.mrp) >= 45 },
  'new-arrivals': { title: 'New Arrivals', select: (list) => [...list].sort(byNewest).slice(0, 16) },
  'best-sellers': { title: 'Best Sellers', select: (list) => [...list].sort(byPopularity).slice(0, 16) },
};

/** Resolves a /category/:slug URL to a title and the products it contains. */
export function resolveCollection(slug, products) {
  const known = COLLECTIONS[slug];
  if (known) {
    return {
      title: known.title,
      products: known.select ? known.select(products) : products.filter(known.filter),
    };
  }
  const match = products.find((p) => slugify(p.category) === slug);
  return {
    title: match ? match.category : slug.replace(/-/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase()),
    products: products.filter((p) => slugify(p.category) === slug),
  };
}

export function bestSellers(products, count) {
  return [...products].sort(byPopularity).slice(0, count);
}

export function newArrivals(products, count) {
  return [...products].sort(byNewest).slice(0, count);
}

// The catalog is small, so it is fetched once per visit and shared by every page
let catalogRequest = null;

export function fetchCatalog({ refresh = false } = {}) {
  if (!catalogRequest || refresh) {
    catalogRequest = productApi
      .get('/api/products')
      .then((response) => response.data)
      .catch((error) => {
        catalogRequest = null;
        throw error;
      });
  }
  return catalogRequest;
}

export async function fetchProduct(id) {
  const response = await productApi.get(`/api/products/${encodeURIComponent(id)}`);
  return response.data;
}
