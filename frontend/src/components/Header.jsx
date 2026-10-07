import { useContext, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ShoppingBag, User, Search, Truck, Sun, Moon, Menu, X, LogOut } from 'lucide-react';
import { AuthContext } from '../context/AuthContext';
import { ThemeContext } from '../context/ThemeContext';
import { useCart } from '../context/CartContext';
import { BRAND_NAME } from '../config';
import { firstName } from '../lib/format';
import './Header.css';

const NAV_LINKS = [
  { to: '/category/best-sellers', label: 'Best Sellers' },
  { to: '/category/new-arrivals', label: 'New Arrivals' },
  { to: '/category/men', label: 'Men' },
  { to: '/category/women', label: 'Women' },
  { to: '/category/ethnic', label: 'Ethnic' },
  { to: '/category/footwear', label: 'Footwear' },
  { to: '/category/accessories', label: 'Accessories' },
  { to: '/category/sale', label: 'Sale' },
];

const Header = () => {
  const { user, logout } = useContext(AuthContext);
  const { isDarkMode, toggleTheme } = useContext(ThemeContext);
  const { count } = useCart();
  const [searchTerm, setSearchTerm] = useState('');
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const navigate = useNavigate();

  const closeMenu = () => setIsMobileMenuOpen(false);

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchTerm.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchTerm.trim())}`);
    }
  };

  const handleLogout = () => {
    logout();
    closeMenu();
    navigate('/');
  };

  return (
    <header className="header">
      <div className="header-top">
        <div className="container header-top-content">
          <div className="logo-container">
            <Link to="/" className="logo">{BRAND_NAME.toUpperCase()}</Link>
          </div>

          <form className="search-bar" onSubmit={handleSearch} role="search">
            <Search size={18} className="search-icon" aria-hidden="true" />
            <input
              type="search"
              placeholder="Search for products, brands and more"
              aria-label="Search products"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </form>

          <button
            className="mobile-menu-btn"
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            aria-label="Toggle menu"
            aria-expanded={isMobileMenuOpen}
          >
            {isMobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>

          <div className="header-actions">
            <button className="action-btn theme-icon-btn" onClick={toggleTheme} title="Toggle dark/light mode" aria-label="Toggle dark/light mode">
              {isDarkMode ? <Sun size={20} /> : <Moon size={20} />}
            </button>
            <Link to="/orders" className="action-btn" title="My orders" aria-label="My orders">
              <Truck size={20} />
            </Link>
            {user ? (
              <>
                <Link to="/orders" className="action-btn account-name" title="My account">
                  <User size={20} /> <span>Hi, {firstName(user.name)}</span>
                </Link>
                <button onClick={handleLogout} className="action-btn" title="Log out" aria-label="Log out">
                  <LogOut size={20} />
                </button>
              </>
            ) : (
              <Link to="/auth" className="action-btn" title="Sign in" aria-label="Sign in">
                <User size={20} />
              </Link>
            )}
            <Link to="/cart" className="action-btn cart-link" title="Bag" aria-label={`Bag, ${count} items`}>
              <ShoppingBag size={20} />
              {count > 0 && <span className="cart-badge">{count > 99 ? '99+' : count}</span>}
            </Link>
          </div>
        </div>
      </div>

      <div className="header-bottom">
        <div className="container">
          <nav className="nav-links" aria-label="Categories">
            {NAV_LINKS.map((link) => (
              <Link key={link.to} to={link.to} className="nav-link" onClick={closeMenu}>
                {link.label}
              </Link>
            ))}
          </nav>
        </div>
      </div>

      <div className={`mobile-menu-dropdown ${isMobileMenuOpen ? 'open' : ''}`}>
        {user && <span className="mobile-dropdown-greeting">Hi, {firstName(user.name)}</span>}
        <Link to="/orders" className="nav-link mobile-dropdown-link" onClick={closeMenu}>My Orders</Link>
        <Link to="/track-orders" className="nav-link mobile-dropdown-link" onClick={closeMenu}>Track Order</Link>
        <Link to="/cart" className="nav-link mobile-dropdown-link" onClick={closeMenu}>Bag {count > 0 ? `(${count})` : ''}</Link>
        <a href="/about-us.html" className="nav-link mobile-dropdown-link">About Us</a>
        <Link to="/contact" className="nav-link mobile-dropdown-link" onClick={closeMenu}>Help & Contact</Link>
        {user ? (
          <button className="nav-link mobile-dropdown-link mobile-btn" onClick={handleLogout}>Log out</button>
        ) : (
          <Link to="/auth" className="nav-link mobile-dropdown-link" onClick={closeMenu}>Sign in / Register</Link>
        )}
        <button className="nav-link mobile-dropdown-link mobile-btn" onClick={() => { toggleTheme(); closeMenu(); }}>
          {isDarkMode ? 'Light mode' : 'Dark mode'}
        </button>
      </div>
    </header>
  );
};

export default Header;
