import { Link } from 'react-router-dom';
import './ErrorPages.css';

const NotFound = () => {
  return (
    <div className="container">
      <div className="error-page animate-fade-in-up">
        <div className="error-code">404</div>
        <h1 className="error-title">Page Not Found</h1>
        <p className="error-message">
          The page you are looking for might have been removed, had its name changed, or is temporarily unavailable.
        </p>
        <div className="error-actions">
          <Link to="/" className="btn btn-primary">
            Go to Homepage
          </Link>
          <Link to="/contact" className="btn btn-secondary">
            Contact Support
          </Link>
        </div>
      </div>
    </div>
  );
};

export default NotFound;
