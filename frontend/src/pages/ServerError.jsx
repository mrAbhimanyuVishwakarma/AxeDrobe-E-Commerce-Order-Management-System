import { Link } from 'react-router-dom';
import './ErrorPages.css';

const ServerError = () => {
  return (
    <div className="container">
      <div className="error-page animate-fade-in-up">
        <div className="error-code">500</div>
        <h1 className="error-title">Internal Server Error</h1>
        <p className="error-message">
          Oops! Something went wrong on our end. We're currently trying to fix the problem. Please try again later.
        </p>
        <div className="error-actions">
          <button className="btn btn-primary" onClick={() => window.location.reload()}>
            Refresh Page
          </button>
          <Link to="/" className="btn btn-secondary">
            Go to Homepage
          </Link>
        </div>
      </div>
    </div>
  );
};

export default ServerError;
