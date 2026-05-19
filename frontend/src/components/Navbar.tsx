import React, { useEffect, useRef, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { isAuthenticated, logout, clearToken } from '../api/auth';
import { getProfile, UserProfile } from '../api/profile';
import './Navbar.css';

const Navbar: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const isAuth = location.pathname === '/login' || location.pathname === '/signup' || location.pathname === '/verify';
  const loggedIn = isAuthenticated();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (loggedIn) {
      getProfile().then(res => setProfile(res.data)).catch(() => {});
    } else {
      setProfile(null);
    }
  }, [loggedIn]);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = async () => {
    setDropdownOpen(false);
    try { await logout(); } catch {}
    clearToken();
    setProfile(null);
    navigate('/');
  };

  return (
    <nav className="navbar">
      <div className="navbar-left">
        <Link to="/" className="navbar-brand">
          <div className="navbar-brand-text">
            <span className="brand-cou">Cou</span>
            <span className="brand-teach">Teach</span>
          </div>
        </Link>
        {!isAuth && (
          <div className="navbar-links">
            <Link to="/courses" className={`nav-link`}>Courses</Link>
            <Link to="/my-enrollments" className="nav-link">My Courses</Link>
          </div>
        )}
      </div>
      <div className="navbar-right">
        {!isAuth && (
          <>
            <span className="nav-lang">Eng</span>
            {loggedIn ? (
              <div className="nav-user-menu" ref={dropdownRef}>
                <button
                  className="nav-user-trigger"
                  onClick={() => setDropdownOpen(o => !o)}
                >
                  {profile?.avatarUrl ? (
                    <img src={profile.avatarUrl} alt={profile.name} className="nav-avatar" />
                  ) : (
                    <div className="nav-avatar-placeholder">
                      {profile?.name?.[0]?.toUpperCase() ?? '?'}
                    </div>
                  )}
                  <span className="nav-username">{profile?.name ?? 'Profile'}</span>
                  <span className="nav-chevron">{dropdownOpen ? '▲' : '▼'}</span>
                </button>
                {dropdownOpen && (
                  <div className="nav-dropdown">
                    <Link to="/profile" className="nav-dropdown-item" onClick={() => setDropdownOpen(false)}>
                      My Profile
                    </Link>
                    <Link to="/my-enrollments" className="nav-dropdown-item" onClick={() => setDropdownOpen(false)}>
                      My Courses
                    </Link>
                    <button className="nav-dropdown-item nav-dropdown-logout" onClick={handleLogout}>
                      Log Out
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <>
                <Link to="/login" className="btn-login">Login</Link>
                <Link to="/signup" className="btn-signup">Sign Up</Link>
              </>
            )}
          </>
        )}
      </div>
    </nav>
  );
};

export default Navbar;