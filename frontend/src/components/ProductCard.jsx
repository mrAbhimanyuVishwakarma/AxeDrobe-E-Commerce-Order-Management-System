import { Link } from 'react-router-dom';
import { Star } from 'lucide-react';
import { discountPercent, formatCount, formatPrice, sizedImage } from '../lib/format';
import './ProductCard.css';

const LOW_STOCK = 10;

const ProductCard = ({ product, onQuickAdd }) => {
  const discount = discountPercent(product.price, product.mrp);
  const needsSize = product.sizes?.length > 0;
  const outOfStock = product.stock <= 0;

  let badge = null;
  if (outOfStock) badge = 'Sold out';
  else if (product.stock <= LOW_STOCK) badge = `Only ${product.stock} left`;
  else if (discount >= 45) badge = 'Hot deal';

  return (
    <div className="product-card animate-fade-in-up">
      <Link to={`/product/${product.id}`} className="product-link">
        <div className="product-image-container">
          {badge && <span className="badge-tag">{badge}</span>}
          <img
            src={sizedImage(product.imageUrl, 500)}
            alt={product.name}
            className="product-image"
            loading="lazy"
            width="500"
            height="500"
          />
        </div>
        <div className="product-info">
          <div className="product-brand">{product.brand}</div>
          <h3 className="product-title">{product.name}</h3>
          {product.ratingCount > 0 && (
            <div className="rating">
              <Star size={12} fill="currentColor" aria-hidden="true" /> {product.rating.toFixed(1)}
              <span className="rating-count"> | {formatCount(product.ratingCount)}</span>
            </div>
          )}
          <div className="price-container">
            <span className="current-price">{formatPrice(product.price)}</span>
            {discount > 0 && (
              <>
                <span className="original-price">{formatPrice(product.mrp)}</span>
                <span className="discount">{discount}% OFF</span>
              </>
            )}
          </div>
        </div>
      </Link>

      <div className="product-actions">
        {needsSize ? (
          <Link to={`/product/${product.id}`} className="add-to-cart-btn secondary">
            Choose size
          </Link>
        ) : (
          <button className="add-to-cart-btn" onClick={() => onQuickAdd?.(product)} disabled={outOfStock}>
            {outOfStock ? 'Sold out' : 'Add to bag'}
          </button>
        )}
      </div>
    </div>
  );
};

export default ProductCard;
