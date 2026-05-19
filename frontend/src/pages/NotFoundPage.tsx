import React from 'react';
import { useNavigate, Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import './css/NotFoundPage.css';

const NotFoundPage: React.FC = () => {
  const navigate = useNavigate();
  return (
    <div className="notfound-page">
      <Navbar />
      <div className="nf-container">
        <h1 className="nf-code">404</h1>
        <h2 className="nf-title">Page not found</h2>
        <p className="nf-sub">
          The page you were looking for doesn't exist, has moved, or you typed it wrong.
        </p>
        <div className="nf-actions">
          <button className="nf-btn" onClick={() => navigate(-1)}>
            ← Go back
          </button>
          <Link to="/" className="nf-btn nf-primary">
            Home
          </Link>
          <Link to="/courses" className="nf-btn">
            Browse courses
          </Link>
        </div>
      </div>
    </div>
  );
};

export default NotFoundPage;
