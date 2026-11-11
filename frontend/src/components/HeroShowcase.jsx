import { useEffect, useMemo, useRef, useState } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { Link } from 'react-router-dom';
import { sizedImage } from '../lib/format';
import './HeroShowcase.css';

const ACCESSORY_TERMS = ['bag', 'backpack', 'wallet', 'watch', 'boot', 'shoe', 'sneaker', 'loafer', 'sandal', 'heel', 'flat'];

function rank(products) {
  return [...products]
    .filter((product) => Number(product.stock) > 0)
    .sort((a, b) => Number(b.rating || 0) - Number(a.rating || 0) || String(b.id).localeCompare(String(a.id)));
}

function selectHeroProducts(products) {
  const women = rank(products.filter((product) => product.gender === 'WOMEN')).slice(0, 2);
  const men = rank(products.filter((product) => product.gender === 'MEN')).slice(0, 2);
  const accessory = rank(products.filter((product) => {
    const value = `${product.category || ''} ${product.name || ''}`.toLowerCase();
    return ACCESSORY_TERMS.some((term) => value.includes(term));
  })).slice(0, 1);
  return [...women, ...men, ...accessory];
}

export default function HeroShowcase({ products }) {
  const slides = useMemo(() => selectHeroProducts(products), [products]);
  const [index, setIndex] = useState(0);
  const [paused, setPaused] = useState(false);
  const regionRef = useRef(null);
  const safeIndex = slides.length ? index % slides.length : 0;
  const current = slides[safeIndex] || products[0];

  useEffect(() => {
    if (!slides.length || paused || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return undefined;
    const timer = window.setInterval(() => setIndex((value) => (value + 1) % slides.length), 5000);
    return () => window.clearInterval(timer);
  }, [slides.length, paused]);

  if (!current) return null;

  const move = (offset) => setIndex((value) => (value + offset + slides.length) % slides.length);

  return (
    <section className="showcase" aria-label="Featured styles">
      <div
        className="showcase-media"
        ref={regionRef}
        onMouseEnter={() => setPaused(true)}
        onMouseLeave={() => setPaused(false)}
        onFocusCapture={() => setPaused(true)}
        onBlurCapture={(event) => {
          if (!regionRef.current?.contains(event.relatedTarget)) setPaused(false);
        }}
      >
        <img key={current.id} src={sizedImage(current.imageUrl, 1200)} alt={current.name} />
        <button type="button" className="showcase-arrow prev" onClick={() => move(-1)} aria-label="Previous featured style"><ChevronLeft /></button>
        <button type="button" className="showcase-arrow next" onClick={() => move(1)} aria-label="Next featured style"><ChevronRight /></button>
        <span className="showcase-count">{safeIndex + 1} / {slides.length}</span>
        <Link className="showcase-product" to={`/product/${current.id}`}>{current.name}</Link>
      </div>
      <div className="showcase-copy">
        <h1>Elevate your<br />everyday style.</h1>
        <p>Premium fabrics, honest prices. Discover new-season apparel, footwear and accessories made for every day.</p>
        <div className="showcase-actions">
          <Link to="/category/new-arrivals" className="btn btn-primary">Shop new arrivals</Link>
          <Link to="/products" className="btn btn-secondary">Explore the collection</Link>
        </div>
      </div>
    </section>
  );
}
