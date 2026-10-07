import { Link } from 'react-router-dom';
import { Truck, Banknote, RotateCcw, BadgeCheck } from 'lucide-react';
import HeroBanner from '../components/HeroBanner';
import ProductCard from '../components/ProductCard';
import FAQSection from '../components/FAQSection';
import { useCatalog } from '../hooks/useCatalog';
import { useQuickAdd } from '../hooks/useQuickAdd';
import { bestSellers, newArrivals, resolveCollection } from '../lib/catalog';
import { formatPrice, sizedImage } from '../lib/format';
import { FREE_SHIPPING_FROM } from '../config';
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

function ProductRow({ title, subtitle, products, link, onQuickAdd }) {
  return (
    <section className="container section-products">
      <div className="section-header">
        <h2>{title}</h2>
        <p>{subtitle}</p>
      </div>
      <div className="product-grid">
        {products.map((product) => (
          <ProductCard key={product.id} product={product} onQuickAdd={onQuickAdd} />
        ))}
      </div>
      <div className="section-cta">
        <Link to={link} className="btn btn-secondary">View all</Link>
      </div>
    </section>
  );
}

const Home = () => {
  const { products, loading, error } = useCatalog();
  const quickAdd = useQuickAdd();

  const tiles = CATEGORY_TILES.map((tile) => {
    const { products: inCollection } = resolveCollection(tile.slug, products);
    const cover = inCollection.find((p) => p.name === tile.pick) || inCollection[0];
    return { ...tile, image: cover?.imageUrl, count: inCollection.length };
  });

  return (
    <div className="home-page">
      <HeroBanner />

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
              <h2>Shop by Category</h2>
              <p>Find exactly what you are looking for.</p>
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

          <ProductRow
            title="Best Sellers"
            subtitle="The pieces our customers keep coming back for."
            products={bestSellers(products, 8)}
            link="/category/best-sellers"
            onQuickAdd={quickAdd}
          />

          <ProductRow
            title="New Arrivals"
            subtitle="Fresh styles, just landed."
            products={newArrivals(products, 8)}
            link="/category/new-arrivals"
            onQuickAdd={quickAdd}
          />
        </>
      )}

      <FAQSection />
    </div>
  );
};

export default Home;
