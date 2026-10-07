import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ShoppingBag, Star, Truck, Banknote, RotateCcw } from 'lucide-react';
import ProductCard from '../components/ProductCard';
import { useCart } from '../context/CartContext';
import { useToast } from '../context/ToastContext';
import { useCatalog } from '../hooks/useCatalog';
import { useQuickAdd } from '../hooks/useQuickAdd';
import { errorMessage } from '../lib/api';
import { GENDER_LABELS, fetchProduct, slugify } from '../lib/catalog';
import { discountPercent, formatCount, formatPrice, sizedImage } from '../lib/format';
import { FREE_SHIPPING_FROM } from '../config';
import './ProductDetail.css';

const LOW_STOCK = 10;

function ProductView({ id }) {
  const navigate = useNavigate();
  const { addItem, items } = useCart();
  const { showToast } = useToast();
  const { products } = useCatalog();
  const quickAdd = useQuickAdd();

  const [state, setState] = useState({ product: null, loading: true, error: null });
  const [selectedSize, setSelectedSize] = useState('');
  const [sizeError, setSizeError] = useState(false);

  useEffect(() => {
    let active = true;
    fetchProduct(id)
      .then((product) => {
        if (active) setState({ product, loading: false, error: null });
      })
      .catch((error) => {
        if (!active) return;
        const notFound = error.response?.status === 404;
        setState({ product: null, loading: false, error: notFound ? 'This product is no longer available.' : errorMessage(error) });
      });
    return () => {
      active = false;
    };
  }, [id]);

  const { product, loading, error } = state;

  if (loading) return <div className="container loading-spinner">Loading product...</div>;
  if (error || !product) {
    return (
      <div className="container empty-state" style={{ marginTop: '2rem' }}>
        <h3>{error || 'Product not found.'}</h3>
        <Link to="/products" className="btn btn-primary">Continue shopping</Link>
      </div>
    );
  }

  const discount = discountPercent(product.price, product.mrp);
  const hasSizes = product.sizes?.length > 0;
  const outOfStock = product.stock <= 0;
  const inBag = items.some((item) => item.productId === product.id && (!hasSizes || item.size === selectedSize));

  const similar = products
    .filter((p) => p.id !== product.id && p.category === product.category)
    .concat(products.filter((p) => p.id !== product.id && p.category !== product.category && p.gender === product.gender))
    .slice(0, 4);

  const handleAddToBag = () => {
    if (hasSizes && !selectedSize) {
      setSizeError(true);
      showToast('Please select a size', 'error');
      return;
    }
    if (inBag) {
      navigate('/cart');
      return;
    }
    if (addItem(product, hasSizes ? selectedSize : null, 1)) {
      showToast(`Added to bag${hasSizes ? ` (Size ${selectedSize})` : ''}`, 'success');
    } else {
      showToast('You already have the maximum quantity of this item in your bag', 'error');
    }
  };

  return (
    <div className="product-detail-page container animate-fade-in-up">
      <div className="breadcrumbs">
        <Link to="/">Home</Link> / <Link to={`/category/${slugify(product.category)}`}>{product.category}</Link> /{' '}
        <span>{product.name}</span>
      </div>

      <div className="pdp-layout">
        <div className="pdp-images">
          <div className="main-image">
            <img src={sizedImage(product.imageUrl, 1100)} alt={product.name} />
          </div>
        </div>

        <div className="pdp-details">
          <h1 className="pdp-brand">{product.brand}</h1>
          <h2 className="pdp-title">{product.name}</h2>

          {product.ratingCount > 0 && (
            <div className="pdp-rating">
              <span className="rating-stars">
                {product.rating.toFixed(1)} <Star size={14} fill="currentColor" aria-hidden="true" />
              </span>
              <span className="rating-count">| {formatCount(product.ratingCount)} Ratings</span>
            </div>
          )}

          <div className="pdp-pricing">
            <span className="current-price">{formatPrice(product.price)}</span>
            {discount > 0 && (
              <>
                <span className="original-price">MRP {formatPrice(product.mrp)}</span>
                <span className="discount">({discount}% OFF)</span>
              </>
            )}
          </div>
          <p className="tax-inclusive">inclusive of all taxes</p>

          {outOfStock ? (
            <p className="stock-note out">Currently out of stock</p>
          ) : product.stock <= LOW_STOCK ? (
            <p className="stock-note low">Hurry, only {product.stock} left!</p>
          ) : null}

          {hasSizes && (
            <div className="pdp-sizes">
              <div className="size-header">
                <h4>Select size</h4>
                {sizeError && !selectedSize && <span className="size-error">Please select a size</span>}
              </div>
              <div className="size-options" role="radiogroup" aria-label="Size">
                {product.sizes.map((size) => (
                  <button
                    key={size}
                    type="button"
                    role="radio"
                    aria-checked={selectedSize === size}
                    className={`size-btn ${selectedSize === size ? 'selected' : ''} ${size.length > 3 ? 'wide' : ''}`}
                    onClick={() => {
                      setSelectedSize(size);
                      setSizeError(false);
                    }}
                  >
                    {size}
                  </button>
                ))}
              </div>
            </div>
          )}

          <div className="pdp-actions">
            <button className="btn-add-to-bag" onClick={handleAddToBag} disabled={outOfStock}>
              <ShoppingBag size={18} aria-hidden="true" />
              {outOfStock ? 'OUT OF STOCK' : inBag ? 'GO TO BAG' : 'ADD TO BAG'}
            </button>
          </div>

          <ul className="pdp-perks">
            <li><Truck size={18} aria-hidden="true" /> Free delivery on orders above {formatPrice(FREE_SHIPPING_FROM)}</li>
            <li><Banknote size={18} aria-hidden="true" /> Cash on delivery available</li>
            <li><RotateCcw size={18} aria-hidden="true" /> Easy 14-day returns</li>
          </ul>

          <div className="pdp-description">
            <h4>Product details</h4>
            <p>{product.description}</p>
            <dl className="pdp-specs">
              <dt>Brand</dt><dd>{product.brand}</dd>
              <dt>Category</dt><dd>{product.category}</dd>
              {product.color && (<><dt>Colour</dt><dd>{product.color}</dd></>)}
              <dt>For</dt><dd>{GENDER_LABELS[product.gender] || product.gender}</dd>
            </dl>
          </div>
        </div>
      </div>

      {similar.length > 0 && (
        <section className="pdp-similar">
          <h3>You may also like</h3>
          <div className="product-grid">
            {similar.map((p) => (
              <ProductCard key={p.id} product={p} onQuickAdd={quickAdd} />
            ))}
          </div>
        </section>
      )}
    </div>
  );
}

// A fresh instance per product resets the size picker and loading state
const ProductDetail = () => {
  const { id } = useParams();
  return <ProductView key={id} id={id} />;
};

export default ProductDetail;
