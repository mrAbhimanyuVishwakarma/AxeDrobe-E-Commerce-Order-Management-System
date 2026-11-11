import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { Truck, Banknote, RotateCcw, BadgeCheck } from 'lucide-react';
import HeroShowcase from '../components/HeroShowcase';
import ProductCard from '../components/ProductCard';
import { useCatalog } from '../hooks/useCatalog';
import { useQuickAdd } from '../hooks/useQuickAdd';
import { bestSellers, resolveCollection } from '../lib/catalog';
import { formatPrice, sizedImage } from '../lib/format';
import { FREE_SHIPPING_FROM } from '../config';
import { orderApi } from '../lib/api';
import './Home.css';

const CATEGORY_TILES = [
  { slug: 'men', label: 'Men', pick: 'Light Wash Trucker Denim Jacket' },
  { slug: 'women', label: 'Women', pick: 'Floral Wrap Maxi Dress' },
  { slug: 'ethnic', label: 'Ethnic Wear', pick: 'Printed Cotton Kurta Set' },
  { slug: 'footwear', label: 'Footwear', pick: 'Suede Lace-Up Boots' },
  { slug: 'accessories', label: 'Accessories', pick: 'Structured Leather Handbag' },
];

const PERKS = [
  { icon: Truck, title: 'Free delivery', text: `On orders above ${formatPrice(FREE_SHIPPING_FROM)}` },
  { icon: Banknote, title: 'Cash on delivery', text: 'Pay when it arrives' },
  { icon: RotateCcw, title: '14-day returns', text: 'Easy pickups from home' },
  { icon: BadgeCheck, title: '100% original', text: 'Quality checked products' },
];

const TREND_FILTERS = [
  { id: 'all', label: 'All' }, { id: 'women', label: 'Women' }, { id: 'men', label: 'Men' },
  { id: 'ethnic', label: 'Ethnic' }, { id: 'accessories', label: 'Accessories' }, { id: 'footwear', label: 'Footwear' },
];

const Home = () => {
  const { products, loading, error } = useCatalog();
  const quickAdd = useQuickAdd();
  const [trendFilter, setTrendFilter] = useState('all');
  const [reviews, setReviews] = useState([]);

  useEffect(() => {
    orderApi.get('/api/reviews/latest?limit=3').then(({ data }) => setReviews(data)).catch(() => setReviews([]));
  }, []);

  const tiles = CATEGORY_TILES.map((tile) => {
    const { products: inCollection } = resolveCollection(tile.slug, products);
    const cover = inCollection.find((p) => p.name === tile.pick) || inCollection[0];
    return { ...tile, image: cover?.imageUrl, count: inCollection.length };
  });
  const trending = useMemo(() => {
    const source = trendFilter === 'all' ? products : resolveCollection(trendFilter, products).products;
    return bestSellers(source, 12);
  }, [products, trendFilter]);

  return (
    <div className="home-page">
      {!loading && !error && <HeroShowcase products={products} />}

      <section className="perks container" aria-label="Why shop with us">
        {PERKS.map(({ icon: Icon, title, text }) => (
          <div key={title} className="perk">
            <Icon size={22} aria-hidden="true" />
            <div>
              <strong>{title}</strong>
              <span>{text}</span>
            </div>
          </div>
        ))}
      </section>

      {loading ? (
        <div className="loading-spinner">Loading the store...</div>
      ) : error ? (
        <div className="error-message container">{error}</div>
      ) : (
        <>
          <section className="container section-categories">
            <div className="section-header">
              <h2>Find Your Frame</h2>
              <p>Shop by category.</p>
            </div>
            <div className="category-grid">
              {tiles.map((tile) => (
                <Link key={tile.slug} to={`/category/${tile.slug}`} className="category-tile">
                  {tile.image && <img src={sizedImage(tile.image, 500)} alt="" loading="lazy" />}
                  <div className="category-tile-label">
                    <span>{tile.label}</span>
                    <small>{tile.count} styles</small>
                  </div>
                </Link>
              ))}
            </div>
          </section>

          <section className="container section-products trending-section">
            <div className="section-header home-heading-row">
              <div><h2>Trending Now</h2><p>Freshly picked from the styles our customers keep coming back for.</p></div>
              <Link to={trendFilter === 'all' ? '/category/best-sellers' : `/category/${trendFilter}`} className="home-text-link">View all</Link>
            </div>
            <div className="trend-tabs" role="group" aria-label="Filter trending products">
              {TREND_FILTERS.map((filter) => <button key={filter.id} type="button" className={trendFilter === filter.id ? 'active' : ''} aria-pressed={trendFilter === filter.id} onClick={() => setTrendFilter(filter.id)}>{filter.label}</button>)}
            </div>
            <div className="product-grid">{trending.map((product) => <ProductCard key={product.id} product={product} onQuickAdd={quickAdd} />)}</div>
          </section>
        </>
      )}

      <section className="review-home" aria-labelledby="reviews-title">
        <div><p className="review-label">Verified purchases only</p><h2 id="reviews-title">What shoppers are saying</h2><p>Written reviews will appear here after customers receive and review their orders.</p></div>
        {reviews.length ? <div className="review-list">{reviews.map((review) => <article key={review.id}><div className="review-stars" aria-label={`${review.rating} out of 5 stars`}>{'★'.repeat(review.rating)}{'☆'.repeat(5 - review.rating)}</div><blockquote>“{review.comment}”</blockquote><div><strong>{review.reviewerName}</strong><span>Verified purchase · {review.productName}</span></div></article>)}</div> : <div className="review-empty"><strong>No verified written reviews yet</strong><span>Be the first to review an item after delivery.</span></div>}
      </section>

      <section className="newsletter-home">
        <h2>The good stuff, before everyone else.</h2>
        <form onSubmit={(event) => event.preventDefault()}><label htmlFor="newsletter-email">Email address</label><div><input id="newsletter-email" type="email" placeholder="you@example.com" required /><button type="submit">Join the list</button></div></form>
      </section>

    </div>
  );
};

export default Home;
