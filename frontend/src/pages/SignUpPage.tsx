import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register } from '../api/auth';
import './css/AuthPage.css';

const SignUpPage: React.FC = () => {
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [repeat, setRepeat] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (!name || !email || !password || !repeat) {
      setError('Please fill in all fields.');
      return;
    }
    if (password !== repeat) {
      setError('Passwords do not match.');
      return;
    }
    if (password.length < 6) {
      setError('Password must be at least 6 characters.');
      return;
    }
    if (!/[a-zA-Z]/.test(password) || !/[0-9]/.test(password)) {
      setError('Password must contain at least one letter and one digit.');
      return;
    }
    setError('');
    setLoading(true);
    try {
      await register({ name, email, password });
      navigate('/verify', { state: { email } });
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed. Try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleSubmit();
  };

  return (
    <div className="auth-bg">
      <Link to="/" className="auth-back-btn">← Back</Link>
      <div className="auth-card">
        <h1 className="auth-title">Registration</h1>

        <label className="auth-label">Full Name</label>
        <input
          className="auth-input"
          type="text"
          placeholder="Enter your name"
          value={name}
          onChange={e => setName(e.target.value)}
          onKeyDown={handleKeyDown}
          autoComplete="name"
        />

        <label className="auth-label">Email</label>
        <input
          className="auth-input"
          type="email"
          placeholder="Enter email"
          value={email}
          onChange={e => setEmail(e.target.value)}
          onKeyDown={handleKeyDown}
          autoComplete="email"
        />

        <label className="auth-label">Password</label>
        <input
          className="auth-input"
          type="password"
          placeholder="Enter password"
          value={password}
          onChange={e => setPassword(e.target.value)}
          onKeyDown={handleKeyDown}
          autoComplete="new-password"
        />

        <label className="auth-label">Repeat password</label>
        <input
          className="auth-input"
          type="password"
          placeholder="Repeat password"
          value={repeat}
          onChange={e => setRepeat(e.target.value)}
          onKeyDown={handleKeyDown}
          autoComplete="new-password"
        />

        {error && <p className="auth-error">{error}</p>}

        <div className="auth-links-row">
          <Link to="/login" className="auth-link">Have account?</Link>
        </div>

        <div className="auth-btn-row">
          <button
            className="auth-btn-outline"
            onClick={handleSubmit}
            disabled={loading}
          >
            {loading ? 'Signing up...' : 'Sign Up'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default SignUpPage;
