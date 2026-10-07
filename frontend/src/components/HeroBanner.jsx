import { Link } from 'react-router-dom';
import './HeroBanner.css';

const HeroBanner = () => {
  return (
    <div className="hero-banner">
      <div className="hero-content animate-fade-in-up">
        <h1 className="hero-title">Elevate Your<br /><span className="highlight">Everyday Style</span></h1>
        <p className="hero-subtitle">
          Premium fabrics, honest prices. Up to 50% off on new-season apparel, footwear and accessories.
        </p>
        <div className="hero-actions">
          <Link to="/category/new-arrivals" className="btn btn-primary hero-btn">Shop New Arrivals</Link>
          <Link to="/category/sale" className="btn hero-btn hero-btn-ghost">Explore the Sale</Link>
        </div>
      </div>
      <div className="hero-overlay"></div>
    </div>
  );
};

export default HeroBanner;
