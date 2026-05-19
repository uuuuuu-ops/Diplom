import React from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import './css/MainPage.css';

const MainPage: React.FC = () => {
  return (
    <div className="main-page">
      <Navbar />
      <section className="hero">
        <div className="hero-content">
          <div className="hero-text">
            <h1 className="hero-title">CouTeach</h1>
            <p className="hero-subtitle">Educational Content<br />Distribution Platform</p>
            <p className="hero-description">
              Connect learners with expert educators. Browse courses, watch lessons,
              take quizzes, and grow your skills — or share your expertise with the world.
            </p>
            <div className="hero-cta">
              <Link to="/signup" className="cta-primary">Get Started</Link>
              <Link to="/login" className="cta-secondary">Already have an account?</Link>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="footer">
        <p> CouTeach — Educational Content Distribution Platform</p>
      </footer>
    </div>
  );
};

export default MainPage;
