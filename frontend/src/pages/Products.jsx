import { useMemo, useState } from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { SlidersHorizontal } from 'lucide-react';
import ProductCard from '../components/ProductCard';
import { useCatalog } from '../hooks/useCatalog';
import { useQuickAdd } from '../hooks/useQuickAdd';
import { GENDER_LABELS, resolveCollection } from '../lib/catalog';
import { discountPercent } from '../lib/format';
import './Products.css';

const PRICE_RANGES = [
  { id: 'under-1000', label: 'Under ₹1,000', test: (p) => p < 1000 },
  { id: '1000-2500', label: '₹1,000 to ₹2,500', test: (p) => p >= 1000 && p <= 2500 },
  { id: '2500-5000', label: '₹2,500 to ₹5,000', test: (p) => p > 2500 && p <= 5000 },
  { id: 'over-5000', label: 'Over ₹5,000', test: (p) => p > 5000 },
];

const DISCOUNTS = [30, 40, 50];

const SORTS = {
  recommended: { label: 'Recommended', compare: null },
  popular: { label: 'Popularity', compare: (a, b) => b.ratingCount - a.ratingCount },
  newest: { label: "What's New", compare: (a, b) => new Date(b.createdAt) - new Date(a.createdAt) },
  'price-asc': { label: 'Price: Low to High', compare: (a, b) => a.price - b.price },
  'price-desc': { label: 'Price: High to Low', compare: (a, b) => b.price - a.price },
  discount: { label: 'Better Discount', compare: (a, b) => discountPercent(b.price, b.mrp) - discountPercent(a.price, a.mrp) },
  rating: { label: 'Customer Rating', compare: (a, b) => b.rating - a.rating },
};

const EMPTY_FILTERS = { genders: [], categories: [], brands: [], price: '', discount: 0 };

const toggle = (list, value) => (list.includes(value) ? list.filter((v) => v !== value) : [...list, value]);

function ProductListing({ categoryId, searchQuery }) {
  const { products, loading, error } = useCatalog();
  const quickAdd = useQuickAdd();

  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const [sort, setSort] = useState('recommended');
  const [isFilterOpen, setIsFilterOpen] = useState(false);

  // Products for the current page (collection, search or everything) before sidebar filters
  const { title, base } = useMemo(() => {
    if (searchQuery) {
      const terms = searchQuery.toLowerCase().split(/\s+/);
      const matches = products.filter((p) => {
        const haystack = `${p.name} ${p.brand} ${p.category} ${p.color} ${p.description} ${GENDER_LABELS[p.gender]}`.toLowerCase();
        return terms.every((term) => haystack.includes(term));
      });
      return { title: `Results for "${searchQuery}"`, base: matches };
    }
    if (categoryId) {
      const collection = resolveCollection(categoryId, products);
      return { title: collection.title, base: collection.products };
    }
    return { title: 'All Products', base: products };
  }, [products, categoryId, searchQuery]);

  const facets = useMemo(() => {
    const count = (key) =>
      Object.entries(base.reduce((acc, p) => ({ ...acc, [p[key]]: (acc[p[key]] || 0) + 1 }), {})).sort(([a], [b]) =>
        a.localeCompare(b),
      );
    return { genders: count('gender'), categories: count('category'), brands: count('brand') };
  }, [base]);

  const visible = useMemo(() => {
    const priceRange = PRICE_RANGES.find((r) => r.id === filters.price);
    const result = base.filter(
      (p) =>
        (filters.genders.length === 0 || filters.genders.includes(p.gender)) &&
        (filters.categories.length === 0 || filters.categories.includes(p.category)) &&
        (filters.brands.length === 0 || filters.brands.includes(p.brand)) &&
        (!priceRange || priceRange.test(Number(p.price))) &&
        discountPercent(p.price, p.mrp) >= filters.discount,
    );
    const compare = SORTS[sort].compare;
    return compare ? [...result].sort(compare) : result;
  }, [base, filters, sort]);

  const activeFilterCount =
    filters.genders.length + filters.categories.length + filters.brands.length + (filters.price ? 1 : 0) + (filters.discount ? 1 : 0);

  return (
    <div className="products-page container">
      <div className="breadcrumbs">
        <Link to="/">Home</Link> / <span>{title}</span>
      </div>

      <div className="products-layout">
        <button className="mobile-filter-toggle" onClick={() => setIsFilterOpen(!isFilterOpen)} aria-expanded={isFilterOpen}>
          <SlidersHorizontal size={16} /> {isFilterOpen ? 'Hide filters' : 'Filters'}
          {activeFilterCount > 0 && <span className="filter-count">{activeFilterCount}</span>}
        </button>

        <aside className={`products-sidebar ${isFilterOpen ? 'mobile-open' : ''}`}>
          <div className="sidebar-header">
            <h3>FILTERS</h3>
            {activeFilterCount > 0 && (
              <button className="clear-filters" onClick={() => setFilters(EMPTY_FILTERS)}>CLEAR ALL</button>
            )}
          </div>

          {facets.genders.length > 1 && (
            <div className="filter-group">
              <h4>GENDER</h4>
              {facets.genders.map(([gender, n]) => (
                <label key={gender}>
                  <input
                    type="checkbox"
                    checked={filters.genders.includes(gender)}
                    onChange={() => setFilters({ ...filters, genders: toggle(filters.genders, gender) })}
                  />
                  {GENDER_LABELS[gender] || gender} <span className="facet-count">({n})</span>
                </label>
              ))}
            </div>
          )}

          {facets.categories.length > 1 && (
            <div className="filter-group">
              <h4>CATEGORIES</h4>
              {facets.categories.map(([category, n]) => (
                <label key={category}>
                  <input
                    type="checkbox"
                    checked={filters.categories.includes(category)}
                    onChange={() => setFilters({ ...filters, categories: toggle(filters.categories, category) })}
                  />
                  {category} <span className="facet-count">({n})</span>
                </label>
              ))}
            </div>
          )}

          {facets.brands.length > 1 && (
            <div className="filter-group">
              <h4>BRAND</h4>
              {facets.brands.map(([brand, n]) => (
                <label key={brand}>
                  <input
                    type="checkbox"
                    checked={filters.brands.includes(brand)}
                    onChange={() => setFilters({ ...filters, brands: toggle(filters.brands, brand) })}
                  />
                  {brand} <span className="facet-count">({n})</span>
                </label>
              ))}
            </div>
          )}

          <div className="filter-group">
            <h4>PRICE</h4>
            {PRICE_RANGES.map((range) => (
              <label key={range.id}>
                <input
                  type="radio"
                  name="price"
                  checked={filters.price === range.id}
                  onChange={() => setFilters({ ...filters, price: range.id })}
                />
                {range.label}
              </label>
            ))}
          </div>

          <div className="filter-group">
            <h4>DISCOUNT RANGE</h4>
            {DISCOUNTS.map((value) => (
              <label key={value}>
                <input
                  type="radio"
                  name="discount"
                  checked={filters.discount === value}
                  onChange={() => setFilters({ ...filters, discount: value })}
                />
                {value}% and above
              </label>
            ))}
          </div>
        </aside>

        <div className="products-main">
          <div className="products-topbar">
            <div className="item-count">
              <strong>{title}</strong> <span>- {visible.length} items</span>
            </div>
            <div className="sort-by">
              <label htmlFor="sort">Sort by:</label>
              <select id="sort" value={sort} onChange={(e) => setSort(e.target.value)}>
                {Object.entries(SORTS).map(([value, option]) => (
                  <option key={value} value={value}>{option.label}</option>
                ))}
              </select>
            </div>
          </div>

          {loading ? (
            <div className="loading-spinner">Loading products...</div>
          ) : error ? (
            <div className="error-message">{error}</div>
          ) : visible.length > 0 ? (
            <div className="product-grid">
              {visible.map((product) => (
                <ProductCard key={product.id} product={product} onQuickAdd={quickAdd} />
              ))}
            </div>
          ) : (
            <div className="empty-state">
              <h3>No products found</h3>
              <p>Try removing some filters or searching for something else.</p>
              {activeFilterCount > 0 ? (
                <button className="btn btn-primary" onClick={() => setFilters(EMPTY_FILTERS)}>Clear filters</button>
              ) : (
                <Link to="/products" className="btn btn-primary">Browse all products</Link>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

// Remount on navigation so filters and sorting start fresh for each category or search
const Products = () => {
  const { categoryId } = useParams();
  const [searchParams] = useSearchParams();
  const searchQuery = (searchParams.get('q') || '').trim();
  return <ProductListing key={`${categoryId || ''}|${searchQuery}`} categoryId={categoryId} searchQuery={searchQuery} />;
};

export default Products;
