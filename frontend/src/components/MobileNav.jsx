import { useContext } from 'react';
import { NavLink } from 'react-router-dom';
import { Home, LayoutGrid, ShoppingBag, User } from 'lucide-react';
import { AuthContext } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import './MobileNav.css';

const navClass = ({ isActive }) => (isActive ? 'nav-item active' : 'nav-item');

const MobileNav = () => {
  const { user } = useContext(AuthContext);
  const { count } = useCart();

  return (
    <nav className="mobile-nav" aria-label="Main">
      <NavLink to="/" className={navClass} end>
        <Home size={22} />
        <span>Home</span>
      </NavLink>

      <NavLink to="/products" className={navClass}>
        <LayoutGrid size={22} />
        <span>Shop</span>
      </NavLink>

      <NavLink to="/cart" className={navClass}>
        <span className="nav-icon-wrap">
          <ShoppingBag size={22} />
          {count > 0 && <span className="cart-badge">{count > 99 ? '99+' : count}</span>}
        </span>
        <span>Bag</span>
      </NavLink>

      <NavLink to={user ? '/orders' : '/auth'} className={navClass}>
        <User size={22} />
        <span>{user ? 'Orders' : 'Sign in'}</span>
      </NavLink>
    </nav>
  );
};

export default MobileNav;
